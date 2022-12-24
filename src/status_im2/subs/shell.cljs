(ns status-im2.subs.shell
  (:require
   [re-frame.core :as re-frame]
   [status-im.react-native.resources :as resources]
   [status-im2.common.constants :as status-constants]))

(defn get-card-content
  [chat]
  (let [last-message (:last-message chat)]
    (case (:content-type last-message)
      status-constants/content-type-text
      {:content-type :text
       :data         (get-in last-message [:content :text])}

      {:content-type :text
       :data         "Todo: Implement"})))

(defn one-to-one-chat-card
  [contact names chat id]
  (let [images          (:images contact)
        profile-picture (:uri (or (:thumbnail images) (:large images) (first images)))]
    {:title               (first names)
     :avatar-params       {:full-name       (last names)
                           :profile-picture (when profile-picture
                                              (str profile-picture "&addRing=0"))}
     :customization-color (or (:customization-color contact) :primary)
     :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
     :on-press            #(re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 id true])
     :content             (get-card-content chat)}))

(defn private-group-chat-card
  [chat id]
  {:title               (:chat-name chat)
   :avatar-params       {}
   :customization-color (or (:customization-color chat) :primary)
   :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
   :on-press            #(re-frame/dispatch [:chat.ui/navigate-to-chat-nav2 id true])
   :content             (get-card-content chat)})

(defn community-card
  [community id content]
  (let [images          (:images community)
        profile-picture (if (= id status-constants/status-community-id)
                          (resources/get-image :status-logo)
                          (when images
                            {:uri (:uri (or (:thumbnail images)
                                            (:large images)
                                            (first images)))}))]
    {:title               (:name community)
     :avatar-params       (if profile-picture
                            {:source profile-picture}
                            {:name (:name community)})
     :customization-color (or (:customization-color community) :primary)
     :on-close            #(re-frame/dispatch [:shell/close-switcher-card id])
     :on-press            #(re-frame/dispatch [:navigate-to-nav2 :community
                                               {:community-id id} true])
     :content             (or content
                              {:content-type :community-info
                               :data         {:type :permission}})}))

(defn community-channel-card
  [community community-id _ channel-id content]
  (merge
   (community-card community community-id content)
   {:on-press (fn []
                (re-frame/dispatch [:navigate-to :community {:community-id community-id}])
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
    (re-frame/subscribe [:chats/chat id])])
 (fn [[contact names chat] [_ id]]
   (one-to-one-chat-card contact names chat id)))

(re-frame/reg-sub
 :shell/private-group-chat-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:chats/chat id])])
 (fn [[chat] [_ id]]
   (private-group-chat-card chat id)))

(re-frame/reg-sub
 :shell/community-card
 (fn [[_ id] _]
   [(re-frame/subscribe [:communities/community id])])
 (fn [[community] [_ id]]
   (community-card community id nil)))

(re-frame/reg-sub
 :shell/community-channel-card
 (fn [[_ community-id channel-id _] _]
   [(re-frame/subscribe [:communities/community community-id])
    (re-frame/subscribe [:chats/chat channel-id])])
 (fn [[community channel] [_ community-id channel-id content]]
   (community-channel-card community community-id channel channel-id content)))
