(ns status-im.test.chat.models.message
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.chat.models.message :as message]))

(deftest add-to-chat?
  (testing "it returns true when it's not in loaded message"
    (is (message/add-to-chat? {:db {:chats {"a" {}}}}
                              {:message-id "message-id"
                               :from "a"
                               :chat-id "a"})))
  (testing "it returns false when from is the same as pk"
    (is (not (message/add-to-chat? {:db {:current-public-key "pk"
                                         :chats {"a" {}}}}
                                   {:message-id "message-id"
                                    :from "pk"
                                    :chat-id "a"}))))
  (testing "it returns false when it's already in the loaded message"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:messages {"message-id" {}}}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :chat-id "a"}))))
  (testing "it returns false when it's already in the not-loaded-message-ids"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:not-loaded-message-ids #{"message-id"}}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :chat-id "a"})))))

(deftest receive-send-seen
  (let [db           {:db {:chats {"chat-id" {}}
                           :account/account {:public-key "a"}
                           :current-chat-id "chat-id"
                           :view-id :chat}}
        message      {:chat-id "chat-id"
                      :from "a"
                      :message-id "1"}
        extract-seen (comp :payload :message first :shh/post)]
    (testing "it send a seen message when the chat is 1-to-1 and is open"
      (is (instance? protocol/MessagesSeen
                     (extract-seen (message/receive message db))))
      (is (= #{"1"} (:message-ids (extract-seen (message/receive message db))))))
    (testing "it does not send any when the chat is public"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :chats "chat-id" :public?] true))))))
    (testing "it does not send any when we are in a different chat"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :current-chat-id] :different))))))
    (testing "it does not send any when we are not in a chat view"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :view-id] :home))))))
    (testing "it does not send any when no public key is in account"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :account/account :public-key] nil))))))))
