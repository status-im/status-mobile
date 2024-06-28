(ns status-im.subs.shell
  (:require
    [re-frame.core :as re-frame]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]
    [status-im.feature-flags :as ff]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]))

;; Helper Functions
(defn community-avatar
  [community]
  (let [images (:images community)]
    (when images
      {:uri (:uri (or (:thumbnail images)
                      (:large images)
                      (first images)))})))

(defn get-card-content
  [{:keys [chat communities group-chat? primary-name unviewed-messages-count unviewed-mentions-count]}]
  (let [{:keys [content-type content deleted? outgoing deleted-for-me?] :as last-message}
        (:last-message chat)
        unviewed-messages-count (or unviewed-messages-count (:unviewed-messages-count chat))
        unviewed-mentions-count (or unviewed-mentions-count (:unviewed-mentions-count chat))]
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

         ;; Currently mock image is used as placeholder, as last-message don't have image
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

         (#{constants/content-type-contact-request
            constants/content-type-system-message-mutual-event-removed
            constants/content-type-system-message-mutual-event-accepted}
          content-type)
         {:content-type constants/content-type-contact-request}))

     {:new-notifications?     (pos? unviewed-messages-count)
      :notification-indicator (if (or
                                   (= (:chat-type chat) constants/one-to-one-chat-type)
                                   (pos? unviewed-mentions-count))
                                :counter
                                :unread-dot)
      :counter-label          (if (= (:chat-type chat) constants/one-to-one-chat-type)
                                unviewed-messages-count
                                unviewed-mentions-count)})))

(defn one-to-one-chat-card
  [contact names profile-picture chat id communities profile-customization-color]
  (let [display-name (first names)]
    {:title                       display-name
     :avatar-params               {:full-name       display-name
                                   :profile-picture profile-picture}
     :customization-color         (or (:customization-color contact) :primary)
     :content                     (get-card-content
                                   {:chat        chat
                                    :communities communities})
     :id                          id
     :profile-customization-color profile-customization-color}))

(defn private-group-chat-card
  [chat id communities primary-name profile-customization-color]
  {:title                       (:chat-name chat)
   :avatar-params               {}
   :customization-color         (or (:color chat) :primary)
   :content                     (get-card-content
                                 {:chat         chat
                                  :communities  communities
                                  :group-chat?  true
                                  :primary-name primary-name})
   :id                          id
   :profile-customization-color profile-customization-color})

(defn community-card
  [community id profile-customization-color]
  (let [profile-picture (community-avatar community)]
    {:title                       (:name community)
     :banner                      {:uri (get-in (:images community) [:banner :uri])}
     :avatar-params               (if profile-picture
                                    {:source profile-picture}
                                    {:name (:name community)})
     :customization-color         (or (:color community) :primary)
     :content                     (merge
                                   {:community-info {:type :permission}}
                                   (get-card-content
                                    {:unviewed-messages-count (:unviewed-messages-count community)
                                     :unviewed-mentions-count (:unviewed-mentions-count community)}))
     :id                          id
     :profile-customization-color profile-customization-color}))

(defn community-channel-card
  [community community-id channel channel-id profile-customization-color]
  (merge
   (community-card community community-id profile-customization-color)
   {:content             (merge
                          {:community-channel {:emoji        (:emoji channel)
                                               :channel-name (str "# " (:name channel))}}
                          (get-card-content {:chat channel}))
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
    (re-frame/subscribe [:chats/photo-path id])
    (re-frame/subscribe [:chats/chat id])
    (re-frame/subscribe [:communities])
    (re-frame/subscribe [:profile/customization-color])])
 (fn [[contact names profile-picture chat communities profile-customization-color] [_ id]]
   (one-to-one-chat-card contact
                         names
                         profile-picture
                         chat
                         id
                         communities
                         profile-customization-color)))

(re-frame/reg-sub
 :shell/private-group-chat-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:chats/chat id])
    (re-frame/subscribe [:communities])
    (re-frame/subscribe [:contacts/contacts])
    (re-frame/subscribe [:profile/profile])
    (re-frame/subscribe [:profile/customization-color])])
 (fn [[chat communities contacts current-profile profile-customization-color] [_ id]]
   (let [from    (get-in chat [:last-message :from])
         contact (if from
                   (get contacts from {:public-key from})
                   current-profile)]
     (private-group-chat-card chat id communities (:primary-name contact) profile-customization-color))))

(re-frame/reg-sub
 :shell/community-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:communities/community id])
    (re-frame/subscribe [:communities/unviewed-counts id])
    (re-frame/subscribe [:profile/customization-color])])
 (fn [[community unviewed-counts profile-customization-color] [_ id]]
   (community-card (merge community unviewed-counts) id profile-customization-color)))

(re-frame/reg-sub
 :shell/community-channel-card
 (fn [[_ channel-id] _]
   [(re-frame/subscribe [:chats/chat channel-id])
    (re-frame/subscribe [:communities])
    (re-frame/subscribe [:profile/customization-color])])
 (fn [[channel communities profile-customization-color] [_ channel-id]]
   (let [community-id (:community-id channel)
         community    (get communities (:community-id channel))]
     (community-channel-card community community-id channel channel-id profile-customization-color))))

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
                   (= chat-type constants/private-group-chat-type))
              (-> acc
                  (update-in [:chats-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:chats-stack :unviewed-mentions-count] + unviewed-mentions-count))

              (and (not muted)
                   (= chat-type constants/one-to-one-chat-type))
              ;; Note - for 1-1 chats, all unread messages are counted as mentions and shown with
              ;; counter
              (-> acc
                  (update-in [:chats-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:chats-stack :unviewed-mentions-count] + unviewed-messages-count))

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
       :notification-indicator (if (pos? (:unviewed-mentions-count chats-stack))
                                 :counter
                                 :unread-dot)
       :counter-label          (:unviewed-mentions-count chats-stack)}})))

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
   (or (not (ff/enabled? ::ff/shell.jump-to))
       (get screens shell.constants/chat-screen))))
