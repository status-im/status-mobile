(ns status-im2.subs.shell
  (:require [utils.i18n :as i18n]
            [re-frame.core :as re-frame]
            [utils.datetime :as datetime]
            [status-im2.config :as config]
            [status-im2.constants :as constants]
            [status-im2.common.resources :as resources]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]))

;; Helper Functions
(defn community-avatar
  [community]
  (let [images (:images community)]
    (when images
      {:uri (:uri (or (:thumbnail images)
                      (:large images)
                      (first images)))})))

(defn get-card-content
  [{:keys [chat communities group-chat? primary-name]}]
  (let [{:keys [content-type content deleted? outgoing deleted-for-me?] :as last-message}
        (:last-message chat)]
    (merge
     (when last-message
       (cond
         (or deleted-for-me? deleted?)
         {:content-type constants/content-type-text
          :data         {:text
                         (if (or deleted-for-me? outgoing)
                           (i18n/label :t/you-deleted-a-message)
                           (if (and group-chat? primary-name)
                             (i18n/label :t/user-deleted-a-message {:user primary-name})
                             (i18n/label :t/this-message-was-deleted)))}}

         (#{constants/content-type-text
            constants/content-type-emoji}
          content-type)
         {:content-type constants/content-type-text
          :data         content}

         ;; Currently mock image is used as placeholder,
         ;; as last-message don't have image
         ;; https://github.com/status-im/status-mobile/issues/14625
         (= content-type constants/content-type-image)
         {:content-type constants/content-type-image
          :data         [{:source (resources/get-mock-image :photo2)}]}

         ;; Same for sticker, mock image is used
         (= content-type constants/content-type-sticker)
         {:content-type constants/content-type-sticker
          :data         {:source (resources/get-mock-image :sticker)}}

         ;; Mock Image
         (= content-type constants/content-type-gif)
         {:content-type constants/content-type-gif
          :data         {:source (resources/get-mock-image :gif)}}

         (= content-type constants/content-type-audio)
         {:content-type constants/content-type-audio
          :data         (datetime/ms-to-duration (:audio-duration-ms last-message))}

         (= content-type constants/content-type-community)
         (let [community (get communities (:community-id last-message))]
           {:content-type constants/content-type-community
            :data         {:avatar         (community-avatar community)
                           :community-name (:name community)}})

         (= content-type constants/content-type-contact-request)
         {:content-type constants/content-type-contact-request}))

     {:new-notifications?     (pos? (:unviewed-messages-count chat))
      :notification-indicator (if (pos? (:unviewed-mentions-count chat))
                                :counter
                                :unread-dot)
      :counter-label          (:unviewed-mentions-count chat)})))

(defn one-to-one-chat-card
  [contact names chat id communities]
  (let [images          (:images contact)
        profile-picture (:uri (or (:thumbnail images) (:large images) (first images)))]
    {:title               (first names)
     :avatar-params       {:full-name       (first names)
                           :profile-picture (when profile-picture
                                              (str profile-picture "&addRing=0"))}
     :customization-color (or (:customization-color contact) :primary)
     :content             (get-card-content
                           {:chat        chat
                            :communities communities})
     :id                  id}))

(defn private-group-chat-card
  [chat id communities primary-name]
  {:title               (:chat-name chat)
   :avatar-params       {}
   :customization-color (or (:color chat) :primary)
   :content             (get-card-content
                         {:chat         chat
                          :communities  communities
                          :group-chat?  true
                          :primary-name primary-name})
   :id                  id})

(defn community-card
  [community id]
  (let [profile-picture (community-avatar community)]
    {:title               (:name community)
     :banner              {:uri (get-in (:images community) [:banner :uri])}
     :avatar-params       (if profile-picture
                            {:source profile-picture}
                            {:name (:name community)})
     :customization-color (or (:color community) :primary)
     :content             {:community-info {:type :permission}}
     :id                  id}))

(defn community-channel-card
  [community community-id channel channel-id]
  (merge
   (community-card community community-id)
   {:content             {:community-channel {:emoji        (:emoji channel)
                                              :channel-name (str "# " (:name channel))}}
    :customization-color (or (:color channel) :primary)
    :channel-id          channel-id}))

;;;; Subscriptions
(def memo-shell-cards (atom nil))

(re-frame/reg-sub
 :shell/sorted-switcher-cards
 :<- [:shell/switcher-cards]
 :<- [:view-id]
 (fn [[stacks view-id]]
   (if (or (empty? @memo-shell-cards) (= view-id :shell))
     (let [sorted-shell-cards (sort-by :clock > (map val stacks))]
       (reset! memo-shell-cards sorted-shell-cards)
       sorted-shell-cards)
     @memo-shell-cards)))

(re-frame/reg-sub
 :shell/shell-pass-through?
 :<- [:shell/switcher-cards]
 (fn [stacks]
   (> (count stacks) 6)))

;; Switcher Cards
(re-frame/reg-sub
 :shell/one-to-one-chat-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:contacts/contact-by-identity id])
    (re-frame/subscribe [:contacts/contact-two-names-by-identity id])
    (re-frame/subscribe [:chats/chat id])
    (re-frame/subscribe [:communities])])
 (fn [[contact names chat communities] [_ id]]
   (one-to-one-chat-card contact names chat id communities)))

(re-frame/reg-sub
 :shell/private-group-chat-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:chats/chat id])
    (re-frame/subscribe [:communities])
    (re-frame/subscribe [:contacts/contacts])
    (re-frame/subscribe [:profile/profile])])
 (fn [[chat communities contacts current-multiaccount] [_ id]]
   (let [from         (get-in chat [:last-message :from])
         contact      (when from (multiaccounts/contact-by-identity contacts from))
         primary-name (when from
                        (first (multiaccounts/contact-two-names-by-identity
                                contact
                                current-multiaccount
                                from)))]
     (private-group-chat-card chat id communities primary-name))))

(re-frame/reg-sub
 :shell/community-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:communities/community id])])
 (fn [[community] [_ id]]
   (community-card community id)))

(re-frame/reg-sub
 :shell/community-channel-card
 (fn [[_ channel-id] _]
   [(re-frame/subscribe [:chats/chat channel-id])
    (re-frame/subscribe [:communities])])
 (fn [[channel communities] [_ channel-id]]
   (let [community-id (:community-id channel)
         community    (get communities (:community-id channel))]
     (community-channel-card community community-id channel channel-id))))

;; Bottom tabs
(re-frame/reg-sub
 :shell/bottom-tabs-notifications-data
 :<- [:chats/chats]
 (fn [chats]
   (let [{:keys [chats-stack community-stack]}
         (reduce
          (fn [acc [_ {:keys [unviewed-messages-count unviewed-mentions-count chat-type muted]}]]
            (cond
              (and (not muted)
                   (= chat-type constants/community-chat-type))
              (-> acc
                  (update-in [:community-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:community-stack :unviewed-mentions-count] + unviewed-mentions-count))

              (and (not muted)
                   (#{constants/private-group-chat-type constants/one-to-one-chat-type} chat-type))
              (-> acc
                  (update-in [:chats-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:chats-stack :unviewed-mentions-count] + unviewed-mentions-count))
              :else
              acc))
          {:chats-stack     {:unviewed-messages-count 0 :unviewed-mentions-count 0}
           :community-stack {:unviewed-messages-count 0 :unviewed-mentions-count 0}}
          chats)]
     {:communities-stack
      {:new-notifications?     (pos? (:unviewed-messages-count community-stack))
       :notification-indicator (if (pos? (:unviewed-mentions-count community-stack))
                                 :counter
                                 :unread-dot)
       :counter-label          (:unviewed-mentions-count community-stack)}
      :chats-stack
      {:new-notifications?     (pos? (:unviewed-messages-count chats-stack))
       :notification-indicator :counter
       :counter-label          (:unviewed-messages-count chats-stack)}})))

;; Floating screens
(re-frame/reg-sub
 :shell/floating-screen
 :<- [:shell/floating-screens]
 (fn [screens [_ screen-id]]
   (get screens screen-id)))

(re-frame/reg-sub
 :shell/chat-screen-loaded?
 :<- [:shell/loaded-screens]
 (fn [screens]
   (or config/shell-navigation-disabled?
       (get screens shell.constants/chat-screen))))
