(ns status-im.contexts.communities.actions.leave.events
  (:require [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/cancel-request-to-join-success
 (fn [_ [response-js]]
   {:fx [[:dispatch [:sanitize-messages-and-process-response response-js]]
         [:dispatch
          [:toasts/upsert
           {:type :positive
            :text (i18n/label :t/you-canceled-the-request)}]]]}))

(rf/reg-event-fx :communities/cancel-request-to-join
 (fn [_ [request-to-join-id]]
   {:json-rpc/call
    [{:method      "wakuext_cancelRequestToJoinCommunity"
      :params      [{:id request-to-join-id}]
      :on-success  #(rf/dispatch [:communities/cancel-request-to-join-success %])
      :js-response true
      :on-error    #(log/error "failed to cancel request to join community" request-to-join-id %)}]}))

(rf/reg-event-fx :communities/left
 (fn [_ [response-js]]
   (let [community-name (aget response-js "communities" 0 "name")]
     {:fx [[:dispatch [:sanitize-messages-and-process-response response-js]]
           [:dispatch
            [:toasts/upsert
             {:type :positive
              :text (i18n/label :t/left-community {:community community-name})}]]
           [:dispatch [:activity-center.notifications/fetch-unread-count]]
           [:dispatch [:hide-bottom-sheet]]
           [:dispatch [:navigate-back]]]})))

(rf/reg-event-fx :communities/leave
 (fn [{:keys [db]} [community-id on-success]]
   (let [community-chat-ids (map #(str community-id %)
                                 (keys (get-in db [:communities community-id :chats])))]
     {:effects/push-notifications-clear-message-notifications community-chat-ids
      :json-rpc/call
      [{:method      "wakuext_leaveCommunity"
        :params      [community-id]
        :js-response true
        :on-success  (fn [^js response]
                       (when (fn? on-success)
                         (on-success))
                       (rf/dispatch [:communities/left response]))
        :on-error    #(log/error "failed to leave community" community-id %)}]})))
