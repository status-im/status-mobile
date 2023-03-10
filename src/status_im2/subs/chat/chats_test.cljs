(ns status-im2.subs.chat.chats-test
  (:require [cljs.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [status-im2.constants :as constants]
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(def public-key "0xpk")
(def multiaccount {:public-key public-key})
(def chat-id "1")
(def chat {:chat-id chat-id})

(def private-group-chat
  (assoc
   chat
   :members    #{{:id public-key}}
   :group-chat true
   :chat-type  constants/private-group-chat-type))

(def one-to-one-chat
  (assoc
   chat
   :chat-type
   constants/one-to-one-chat-type))

(h/deftest-sub :chats/current-chat
  [sub-name]
  (testing "private group chat, user is a member"
    (let [chats {chat-id private-group-chat}]
      (swap! rf-db/app-db assoc
        :multiaccount    multiaccount
        :current-chat-id chat-id
        :chats           chats)
      (is (true? (:able-to-send-message? (rf/sub [sub-name]))))))
  (testing "private group chat, user is not member"
    (let [chats {chat-id (dissoc private-group-chat :members)}]
      (swap! rf-db/app-db assoc
        :multiaccount    multiaccount
        :current-chat-id chat-id
        :chats           chats)
      (is (not (:able-to-send-message? (rf/sub [sub-name]))))))
  (testing "one to one chat, mutual contacts"
    (let [chats {chat-id one-to-one-chat}]
      (swap! rf-db/app-db assoc
        :contacts/contacts {chat-id {:contact-request-state constants/contact-request-state-mutual}}
        :multiaccount      multiaccount
        :current-chat-id   chat-id
        :chats             chats)
      (is (:able-to-send-message? (rf/sub [sub-name])))))
  (testing "one to one chat, not a contact"
    (let [chats {chat-id one-to-one-chat}]
      (swap! rf-db/app-db assoc
        :contacts/contacts {chat-id {:contact-request-state constants/contact-request-state-sent}}
        :multiaccount      multiaccount
        :current-chat-id   chat-id
        :chats             chats)
      (is (not (:able-to-send-message? (rf/sub [sub-name])))))))
