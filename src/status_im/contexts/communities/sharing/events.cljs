(ns status-im.contexts.communities.sharing.events
  (:require [legacy.status-im.data-store.chats :as data-store.chats]
            [react-native.platform :as platform]
            [react-native.share :as share]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/invite-people-pressed
 (fn [{:keys [db]} [id]]
   {:db (assoc db :communities/community-id-input id)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:open-modal :invite-people-community {:id id}]]]}))

(rf/reg-event-fx :communities/share-community-pressed
 (fn [{:keys [db]} [id]]
   {:db (assoc db :communities/community-id-input id)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:open-modal :legacy-invite-people-community {:id id}]]]}))

(rf/reg-event-fx :communities/share-community-confirmation-pressed
 (fn [_ [users-public-keys community-id]]
   {:fx [[:json-rpc/call
          [{:method      "wakuext_shareCommunity"
            :params      [{:communityId community-id
                           :users       users-public-keys}]
            :js-response true
            :on-success  [:sanitize-messages-and-process-response]
            :on-error    (fn [err]
                           (log/error {:message      "failed to share community"
                                       :community-id community-id
                                       :err          err}))}]]]}))

(rf/reg-event-fx :communities/share-community-channel-url-qr-code
 (fn [_ [chat-id]]
   (let [on-success #(rf/dispatch [:open-modal :share-community-channel
                                   {:chat-id chat-id
                                    :url     %}])]
     {:fx [[:dispatch [:communities/get-community-channel-share-data chat-id on-success]]]})))

(rf/reg-event-fx :communities/share-community-url-with-data
 (fn [_ [community-id]]
   (let [title      (i18n/label :t/community-on-status)
         on-success (fn [url]
                      (share/open
                       (if platform/ios?
                         {:activityItemSources [{:placeholderItem {:type    "text"
                                                                   :content title}
                                                 :item            {:default {:type    "url"
                                                                             :content url}}
                                                 :linkMetadata    {:title title}}]}
                         {:title     title
                          :subject   title
                          :message   url
                          :url       url
                          :isNewTask true})))]
     {:fx [[:dispatch [:communities/get-community-share-data community-id on-success]]]})))

(rf/reg-event-fx :communities/get-community-channel-share-data
 (fn [_ [chat-id on-success]]
   (let [{:keys [community-id channel-id]} (data-store.chats/decode-chat-id chat-id)]
     {:json-rpc/call
      [{:method     "wakuext_shareCommunityChannelURLWithData"
        :params     [{:CommunityID community-id :ChannelID channel-id}]
        :on-success on-success
        :on-error   (fn [err]
                      (log/error "failed to retrieve community channel url with data"
                                 {:error   err
                                  :chat-id chat-id
                                  :event   :communities/get-community-channel-share-data}))}]})))

(rf/reg-event-fx :communities/get-community-share-data
 (fn [_ [community-id on-success]]
   {:json-rpc/call
    [{:method     "wakuext_shareCommunityURLWithData"
      :params     [community-id]
      :on-success on-success
      :on-error   (fn [err]
                    (log/error "failed to retrieve community url with data"
                               {:error        err
                                :community-id community-id
                                :event        :communities/get-community-share-data}))}]}))

(rf/reg-event-fx :communities/share-community-channel-url-with-data
 (fn [_ [chat-id]]
   (let [title      (i18n/label :t/channel-on-status)
         on-success (fn [url]
                      (share/open
                       (if platform/ios?
                         {:activityItemSources [{:placeholderItem {:type    "text"
                                                                   :content title}
                                                 :item            {:default {:type    "url"
                                                                             :content url}}
                                                 :linkMetadata    {:title title}}]}
                         {:title     title
                          :subject   title
                          :message   url
                          :url       url
                          :isNewTask true})))]
     {:fx [[:dispatch [:communities/get-community-channel-share-data chat-id on-success]]]})))
