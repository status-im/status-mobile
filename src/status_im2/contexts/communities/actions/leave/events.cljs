(ns status-im2.contexts.communities.actions.leave.events
  (:require [legacy.status-im.ui.components.colors :as colors]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/cancel-request-to-join-success
 (fn [_ [response-js]]
   {:fx [[:dispatch [:sanitize-messages-and-process-response response-js]]
         [:dispatch
          [:toasts/upsert
           {:icon       :correct
            :icon-color (:positive-01 @colors/theme)
            :text       (i18n/label :t/you-canceled-the-request)}]]]}))

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
             {:icon       :correct
              :icon-color (:positive-01 @colors/theme)
              :text       (i18n/label :t/left-community {:community community-name})}]]
           [:dispatch [:activity-center.notifications/fetch-unread-count]]
           [:dispatch [:navigate-back]]]})))

(rf/reg-event-fx :communities/leave
 (fn [{:keys [db]} [community-id]]
   (let [community-chat-ids (map #(str community-id %)
                                 (keys (get-in db [:communities community-id :chats])))]
     {:effects/push-notifications-clear-message-notifications community-chat-ids
      :dispatch [:shell/close-switcher-card community-id]
      :json-rpc/call
      [{:method      "wakuext_leaveCommunity"
        :params      [community-id]
        :js-response true
        :on-success  #(rf/dispatch [:communities/left %])
        :on-error    #(log/error "failed to leave community" community-id %)}]})))
