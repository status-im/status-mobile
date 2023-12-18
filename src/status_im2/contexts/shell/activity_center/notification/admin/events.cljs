(ns status-im2.contexts.shell.activity-center.notification.admin.events
  (:require [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/request-to-join-result
 (fn [{:keys [db]} [community-id request-id response-js]]
   {:db         (update-in db [:communities/requests-to-join community-id] dissoc request-id)
    :dispatch-n [[:sanitize-messages-and-process-response response-js]
                 [:activity-center.notifications/mark-as-read request-id]]}))

(rf/reg-event-fx :communities/decline-request-to-join-pressed
 (fn [_ [community-id request-id]]
   {:json-rpc/call
    [{:method      "wakuext_declineRequestToJoinCommunity"
      :params      [{:id request-id}]
      :js-response true
      :on-success  #(rf/dispatch [:communities/request-to-join-result community-id request-id %])
      :on-error    #(log/error "failed to decline " community-id request-id)}]}))

(rf/reg-event-fx :communities/accept-request-to-join-pressed
 (fn [_ [community-id request-id]]
   {:json-rpc/call
    [{:method      "wakuext_acceptRequestToJoinCommunity"
      :params      [{:id request-id}]
      :js-response true
      :on-success  #(rf/dispatch [:communities/request-to-join-result community-id request-id %])
      :on-error    #(log/error "failed to accept requests-to-join" community-id request-id %)}]}))
