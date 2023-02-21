(ns status-im2.contexts.activity-center.notification.contact-requests.events
  (:require [utils.re-frame :as rf]))

(rf/defn accept-contact-request
  {:events [:activity-center.contact-requests/accept-request]}
  [{:keys [db]} id]
  {:json-rpc/call [{:method      "wakuext_acceptContactRequest"
                    :params      [{:id id}]
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])}]})

(rf/defn decline-contact-request
  {:events [:activity-center.contact-requests/decline-request]}
  [{:keys [db]} id]
  {:json-rpc/call [{:method      "wakuext_declineContactRequest"
                    :params      [{:id id}]
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])}]})

(rf/defn cancel-outgoing-contact-request
  {:events [:activity-center.contact-requests/cancel-outgoing-request]}
  [{:keys [db]} id]
  {:json-rpc/call [{:method      "wakuext_cancelOutgoingContactRequest"
                    :params      [{:id id}]
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])}]})
