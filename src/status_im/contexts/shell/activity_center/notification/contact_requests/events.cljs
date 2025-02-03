(ns status-im.contexts.shell.activity-center.notification.contact-requests.events
  (:require
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn accept-contact-request
  {:events [:activity-center.contact-requests/accept]}
  [_ contact-id]
  {:fx [[:json-rpc/call
         [{:method      "wakuext_acceptContactRequest"
           :params      [{:id contact-id}]
           :js-response true
           :on-success  [:sanitize-messages-and-process-response]
           :on-error    [:activity-center.contact-requests/accept-error contact-id]}]]]})

(rf/defn accept-contact-request-error
  {:events [:activity-center.contact-requests/accept-error]}
  [_ contact-id error]
  (log/error "Failed to accept contact-request"
             {:error      error
              :event      :activity-center.contact-requests/accept
              :contact-id contact-id}))

(rf/defn decline-contact-request
  {:events [:activity-center.contact-requests/decline]}
  [_ contact-id]
  {:fx [[:json-rpc/call
         [{:method      "wakuext_declineContactRequest"
           :params      [{:id contact-id}]
           :js-response true
           :on-success  [:sanitize-messages-and-process-response]
           :on-error    [:activity-center.contact-requests/decline-error contact-id]}]]]})

(rf/defn decline-contact-request-error
  {:events [:activity-center.contact-requests/decline-error]}
  [_ contact-id error]
  (log/error "Failed to decline contact-request"
             {:error      error
              :event      :activity-center.contact-requests/decline
              :contact-id contact-id}))
