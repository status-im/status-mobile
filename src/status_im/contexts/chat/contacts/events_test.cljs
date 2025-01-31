(ns status-im.contexts.chat.contacts.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    matcher-combinators.test
    [status-im.constants :as constants]
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
           {:fx [[:json-rpc/call
                  [{:method      "wakuext_sendContactRequest"
                    :js-response true
                    :params      [{:id      contact-public-key
                                   :message (i18n/label :t/add-me-to-your-contacts)}]
                    :on-error    [:contacts/send-contact-request-error contact-public-key]
                    :on-success  [:transport/message-sent]}]]]}
           (chat.contacts/send-contact-request cofx [contact-public-key])))))

  (testing "creates contact request rpc with custom message"
    (let [profile-public-key "0x1"
          contact-public-key "0x2"
          custom-message     "Hey there!"
          cofx               {:db {:profile/profile {:public-key profile-public-key}}}]
      (is (match?
           {:fx [[:json-rpc/call
                  [{:method      "wakuext_sendContactRequest"
                    :js-response true
                    :params      [{:id      contact-public-key
                                   :message custom-message}]
                    :on-error    [:contacts/send-contact-request-error contact-public-key]
                    :on-success  [:transport/message-sent]}]]]}
           (chat.contacts/send-contact-request cofx [contact-public-key custom-message]))))))

(deftest remove-contact-test
  (testing "removes existing contact"
    (let [public-key  "0x2"
          initial-db  {:contacts/contacts
                       {public-key
                        {:added?                true
                         :active?               true
                         :contact-request-state constants/contact-request-state-mutual}}}
          expected-db {:contacts/contacts
                       {public-key
                        {:added?                false
                         :active?               false
                         :contact-request-state constants/contact-request-state-none}}}
          expected-fx [[:json-rpc/call
                        [{:method      "wakuext_retractContactRequest"
                          :params      [{:id public-key}]
                          :js-response true
                          :on-success  [:sanitize-messages-and-process-response]
                          :on-error    [:contacts/remove-contact-error public-key]}]]]]
      (is (match?
           {:db expected-db
            :fx expected-fx}
           (chat.contacts/remove-contact {:db initial-db}
                                         [{:public-key public-key}]))))))

(deftest update-nickname-test
  (testing "updates contact nickname"
    (let [public-key   "0x2"
          new-nickname "Joe"
          expected-fx  [[:json-rpc/call
                         [{:method      "wakuext_setContactLocalNickname"
                           :params      [{:id public-key :nickname new-nickname}]
                           :js-response true
                           :on-success  [:sanitize-messages-and-process-response]
                           :on-error    [:contacts/update-nickname-error public-key new-nickname]}]]]]
      (is (match?
           {:fx expected-fx}
           (chat.contacts/update-nickname {:db {}} [public-key new-nickname]))))))
