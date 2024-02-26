(ns status-im.contexts.chat.contacts.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.chat.contacts.events :as chat.contacts]
    [utils.i18n :as i18n]))

(deftest send-contact-request-test
  (testing "creates nothing when attempting send contact request to self"
    (let [profile-public-key "0x1"
          cofx               {:db {:profile/profile {:public-key profile-public-key}}}]
      (is (match? nil (chat.contacts/send-contact-request cofx [profile-public-key])))))

  (testing "creates contact request rpc with default message"
    (let [profile-public-key "0x1"
          contact-public-key "0x2"
          cofx               {:db {:profile/profile {:public-key profile-public-key}}}]
      (is (match?
           {:json-rpc/call
            [{:method      "wakuext_sendContactRequest"
              :js-response true
              :params      [{:id      contact-public-key
                             :message (i18n/label :t/add-me-to-your-contacts)}]
              :on-error    [:contact.ui/send-contact-request-failure contact-public-key]
              :on-success  [:contact.ui/send-contact-request-success]}]}
           (chat.contacts/send-contact-request cofx [contact-public-key])))))

  (testing "creates contact request rpc with custom message"
    (let [profile-public-key "0x1"
          contact-public-key "0x2"
          custom-message     "Hey there!"
          cofx               {:db {:profile/profile {:public-key profile-public-key}}}]
      (is (match?
           {:json-rpc/call
            [{:method      "wakuext_sendContactRequest"
              :js-response true
              :params      [{:id      contact-public-key
                             :message custom-message}]
              :on-error    [:contact.ui/send-contact-request-failure contact-public-key]
              :on-success  [:contact.ui/send-contact-request-success]}]}
           (chat.contacts/send-contact-request cofx [contact-public-key custom-message]))))))
