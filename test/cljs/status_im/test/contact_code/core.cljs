(ns status-im.test.contact-code.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.contact-code.core :as contact-code]))

(def me "me")
(def member-1 "member-1")
(def member-1-topic "member-1-contact-code")
(def member-2 "member-2")
(def member-2-topic "member-2-contact-code")
(def chat-id "chat-id")
(def chat-id-topic "chat-id-contact-code")

(deftest listen-to-chat
  (testing "an inactive chat"
    (testing "it does nothing"
      (is (not (contact-code/listen-to-chat {:db {}} chat-id)))))
  (testing "an active 1-to-1 chat"
    (testing "it listen to the topic"
      (is (get-in
           (contact-code/listen-to-chat {:db {:chats {chat-id {:is-active true}}}}
                                        chat-id)
           [:db :transport/chats chat-id-topic]))))
  (testing "an active group chat"
    (testing "it listen to any member"
      (let [transport (get-in
                       (contact-code/listen-to-chat {:db {:chats {chat-id
                                                                  {:is-active true
                                                                   :group-chat true
                                                                   :members #{member-1
                                                                              member-2}}}}}
                                                    chat-id)
                       [:db :transport/chats])]
        (is (not (get transport chat-id-topic)))
        (is (get transport member-1-topic))
        (is (get transport member-2-topic))))))

(deftest stop-listening
  (testing "the user is in our contacts"
    (testing "it does not remove transport"
      (is (not (contact-code/stop-listening {:db {:contacts/contacts
                                                  {chat-id {:pending? false}}}}
                                            chat-id)))))
  (testing "the user is not in our contacts"
    (testing "the user is not in any group chats or 1-to1-"
      (testing "it removes the transport"
        (let [transport (contact-code/stop-listening {:db {:transport/chats
                                                           {chat-id-topic {}}}}
                                                     chat-id)]
          (is transport)
          (is (not (get transport chat-id-topic))))))
    (testing "the user is still in some group chats"
      (testing "we joined, and group chat is active it does not remove transport"
        (let [transport (contact-code/stop-listening {:db {:account/account {:public-key me}
                                                           :chats
                                                           {chat-id {:is-active true
                                                                     :members-joined #{me}
                                                                     :members #{member-1}}}
                                                           :transport/chats
                                                           {member-1-topic {}}}}
                                                     member-1)]
          (is (not transport))))
      (testing "we didn't join, it removes transport"
        (let [transport (contact-code/stop-listening {:db {:account/account {:public-key me}
                                                           :chats
                                                           {chat-id {:is-active true
                                                                     :members-joined #{member-1}
                                                                     :members #{member-1}}}
                                                           :transport/chats
                                                           {member-1-topic {}}}}
                                                     member-1)]
          (is transport)
          (is (not (get transport member-1-topic))))))
    (testing "we have a 1-to-1 chat with the user"
      (testing "it does not remove transport"
        (let [transport (contact-code/stop-listening {:db {:chats
                                                           {member-1 {:is-active true}}}}
                                                     member-1)]
          (is (not transport)))))))

(deftest load-fx-test
  (testing "there's already a contact code"
    (is (not (::contact-code/load-contact-code
              (contact-code/load-fx
               {:db {:contact-codes/contact-codes {"1" true}}}
               "1")))))
  (testing "there's no contact-code"
    (is (::contact-code/load-contact-code
         (contact-code/load-fx
          {:db {}}
          "1")))))

(deftest add-contact-code-test
  (testing "adding a contact-code"
    (is
     (= {:db {:contact-codes/contact-codes
              {"1" "contact-code"}}}
        (contact-code/add-contact-code
         {:db {}}
         "1"
         "contact-code")))))
