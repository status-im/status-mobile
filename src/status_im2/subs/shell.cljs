(ns status-im2.subs.shell
  (:require [re-frame.core :as re-frame]
            [utils.datetime :as datetime]
            [status-im2.setup.constants :as constants]
            [status-im.react-native.resources :as resources]))

(defn community-avatar
  [community]
  (let [images (:images community)]
    (if (= (:id community) constants/status-community-id)
      (resources/get-image :status-logo)
      (when images
        {:uri (:uri (or (:thumbnail images)
                        (:large images)
                        (first images)))}))))

(defn get-card-content
  [chat communities]
  (let [last-message (:last-message chat)]
    (merge
     (when last-message
       (case (:content-type last-message)
         (constants/content-type-text
          constants/content-type-emoji)
         {:content-type constants/content-type-text
          :data         (get-in last-message [:content :text])}

         ;; Currently mock image is used as placeholder,
         ;; as last-message don't have image
         ;; https://github.com/status-im/status-mobile/issues/14625
         constants/content-type-image
         {:content-type constants/content-type-image
          :data         [{:source (resources/get-mock-image :photo2)}]}

         ;; Same for sticker, mock image is used
         constants/content-type-sticker
         {:content-type constants/content-type-sticker
          :data         {:source (resources/get-mock-image :sticker)}}

         ;; Mock Image
         constants/content-type-gif
         {:content-type constants/content-type-gif
          :data         {:source (resources/get-mock-image :gif)}}

         constants/content-type-audio
         {:content-type constants/content-type-audio
          :data         (datetime/ms-to-duration (:audio-duration-ms last-message))}

         constants/content-type-community
         (let [community (get communities (:community-id last-message))]
           {:content-type constants/content-type-community
            :data         {:avatar         (community-avatar community)
                           :community-name (:name community)}})

         {:content-type constants/content-type-text
          :data         "Todo: Implement"}))
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
     :avatar-params       {:full-name       (last names)
                           :profile-picture (when profile-picture
                                              (str profile-picture "&addRing=0"))}
     :customization-color (or (:customization-color contact) :primary)
     :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
     :on-press            #(re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 id true])
     :content             (get-card-content chat communities)}))

(defn private-group-chat-card
  [chat id communities]
  {:title               (:chat-name chat)
   :avatar-params       {}
   :customization-color (or (:customization-color chat) :primary)
   :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
   :on-press            #(re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 id true])
   :content             (get-card-content chat communities)})

(defn community-card
  [community id]
  (let [profile-picture (community-avatar community)]
    {:title               (:name community)
     :avatar-params       (if profile-picture
                            {:source profile-picture}
                            {:name (:name community)})
     :customization-color (or (:customization-color community) :primary)
     :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
     :on-press            #(re-frame/dispatch [:navigate-to-nav2 :community
                                               {:community-id id} true])
     :content             {:community-info {:type :permission}}}))

(defn community-channel-card
  [community community-id channel channel-id]
  (merge
   (community-card community community-id)
   {:content  {:community-channel {:emoji        (:emoji channel)
                                   :channel-name (str "# " (:name channel))}}
    :on-press (fn []
                (re-frame/dispatch [:navigate-to-nav2 :community {:community-id community-id}])
                (js/setTimeout
                 #(re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 channel-id true])
                 100))}))

(re-frame/reg-sub
 :shell/sorted-switcher-cards
 :<- [:shell/switcher-cards]
 (fn [stacks]
   (sort-by :clock > (map val stacks))))

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
    (re-frame/subscribe [:communities])])
 (fn [[chat communities] [_ id]]
   (private-group-chat-card chat id communities)))

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
