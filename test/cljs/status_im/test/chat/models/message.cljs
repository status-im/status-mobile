(ns status-im.test.chat.models.message
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.chat.models.message :as message]
            [status-im.utils.datetime :as time]))

(deftest add-to-chat?
  (testing "it returns true when it's not in loaded message"
    (is (message/add-to-chat? {:db {:chats {"a" {}}}}
                              {:message-id "message-id"
                               :from "a"
                               :clock-value 1
                               :chat-id "a"})))
  (testing "it returns false when it's already in the loaded message"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:messages {"message-id" {}}}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :clock-value 1
                                    :chat-id "a"}))))
  (testing "it returns false when it's already in the not-loaded-message-ids"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:not-loaded-message-ids #{"message-id"}}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :clock-value 1
                                    :chat-id "a"}))))
  (testing "it returns false when the clock-value is the same as the deleted-clock-value in chat"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :clock-value 1
                                    :chat-id "a"}))))
  (testing "it returns true when the clock-value is greater than the deleted-clock-value in chat"
    (is (message/add-to-chat? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                              {:message-id "message-id"
                               :from "a"
                               :clock-value 2
                               :chat-id "a"})))
  (testing "it returns false when the clock-value is less than the deleted-clock-value in chat"
    (is (not (message/add-to-chat? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                                   {:message-id "message-id"
                                    :from "a"
                                    :clock-value 0
                                    :chat-id "a"})))))

(deftest add-own-received-message
  (let [db {:current-public-key "me"
            :view-id :chat
            :current-chat-id "chat-id"
            :chats {"chat-id" {:messages {}}}}]
    (testing "a message coming from you!"
      (let [actual (message/receive
                    {:from "me"
                     :message-id "id"
                     :chat-id "chat-id"
                     :content "b"
                     :clock-value 1}
                    {:db db})
            message (get-in actual [:db :chats "chat-id" :messages "id"])
            status  (get-in actual [:db :chats "chat-id" :message-statuses "id" "me" :status])]
        (testing "it adds the message"
          (is message))
        (testing "it marks the message as outgoing"
          (is (= true (:outgoing message))))
        (testing "it confirm the message as processed"
          (is (:confirm-messages-processed actual)))
        (testing "it stores the message"
          (is (:data-store/tx actual)))
        (testing "it does not send a seen confirmation"
          (is (not (:shh/post actual))))
        (testing "it marks it as sent"
          (is (= :sent status)))))))

(deftest receive-send-seen
  (let [db           {:db {:chats {"chat-id" {}}
                           :account/account {:public-key "a"}
                           :current-chat-id "chat-id"
                           :view-id :chat}}
        message      {:chat-id     "chat-id"
                      :from        "a"
                      :message-id  "1"
                      :clock-value 0
                      :timestamp   0}
        extract-seen (comp :payload :message first :shh/post)]
    (testing "it send a seen message when the chat is 1-to-1 and is open"
      (is (instance? protocol/MessagesSeen
                     (extract-seen (message/receive message db))))
      (is (= #{"1"} (:message-ids (extract-seen (message/receive message db))))))
    (testing "it does not send any when the chat is a group-chat"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :chats "chat-id" :group-chat] true))))))
    (testing "it does not send any when we are in a different chat"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :current-chat-id] :different))))))
    (testing "it does not send any when we are not in a chat view"
      (is (nil? (extract-seen
                 (message/receive
                  message
                  (assoc-in db [:db :view-id] :home))))))))

(deftest delete-message
  (let [timestamp (time/now)
        cofx1     {:db {:chats {"chat-id" {:messages      {0 {:message-id  0
                                                              :content     "a"
                                                              :clock-value 0
                                                              :timestamp   (- timestamp 1)}
                                                           1 {:message-id  1
                                                              :content     "b"
                                                              :clock-value 1
                                                              :timestamp   timestamp}}
                                           :message-groups {"datetime-today" '({:message-id 1}
                                                                               {:message-id 0})}}}}}
        cofx2     {:db {:chats {"chat-id" {:messages      {0 {:message-id  0
                                                              :content     "a"
                                                              :clock-value 0
                                                              :timestamp   timestamp}}
                                           :message-groups {"datetime-today" '({:message-id 0})}}}}}
        fx1       (message/delete-message "chat-id" 1 cofx1)
        fx2       (message/delete-message "chat-id" 0 cofx2)]
    (testing "Deleting message deletes it along with all references"
      (is (= '(0)
             (keys (get-in fx1 [:db :chats "chat-id" :messages]))))
      (is (= {"datetime-today" '({:message-id 0})}
             (get-in fx1 [:db :chats "chat-id" :message-groups])))
      (is (= {}
             (get-in fx2 [:db :chats "chat-id" :messages])))
      (is (= {}
             (get-in fx2 [:db :chats "chat-id" :message-groups]))))))
