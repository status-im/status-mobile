(ns status-im.contexts.shell.activity-center.notification.contact-requests.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.shell.activity-center.notification.contact-requests.events :as
     contact-requests]))

(deftest accept-contact-request-test
  (testing "creates effect for accepting a contact request"
    (let [contact-id "0x2"
          cofx       {:db {}}]
      (is (match?
           {:fx [[:json-rpc/call
                  [{:method      "wakuext_acceptContactRequest"
                    :params      [{:id contact-id}]
                    :js-response true
                    :on-success  [:sanitize-messages-and-process-response]
                    :on-error    [:activity-center.contact-requests/accept-error contact-id]}]]]}
           (contact-requests/accept-contact-request cofx contact-id))))))

(deftest decline-contact-request-test
  (testing "creates effect for declining a contact request"
    (let [contact-id "0x2"
          cofx       {:db {}}]
      (is (match?
           {:fx [[:json-rpc/call
                  [{:method      "wakuext_declineContactRequest"
                    :params      [{:id contact-id}]
                    :js-response true
                    :on-success  [:sanitize-messages-and-process-response]
                    :on-error    [:activity-center.contact-requests/decline-error contact-id]}]]]}
           (contact-requests/decline-contact-request cofx contact-id))))))
