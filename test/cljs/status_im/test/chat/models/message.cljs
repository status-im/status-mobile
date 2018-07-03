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
                  (assoc-in db [:db :view-id] :home))))))))

(deftest group-messages
  (let [cofx         {:db {:chats {"chat-id" {:messages {0 {:message-id  0
                                                            :content     "a"
                                                            :clock-value 0
                                                            :timestamp   0}
                                                         1 {:message-id  1
                                                            :content     "b"
                                                            :clock-value 1
                                                            :timestamp   1}
                                                         2 {:message-id  2
                                                            :content     "c"
                                                            :clock-value 2
                                                            :timestamp   2}
                                                         3 {:message-id  3
                                                            :content     "d"
                                                            :clock-value 3
                                                            :timestamp   3}}}}}}
        new-messages [{:message-id  1
                       :content     "b"
                       :clock-value 1
                       :timestamp   1
                       :show?       false}
                      {:message-id  2
                       :content     "c"
                       :clock-value 2
                       :timestamp   2
                       :show?       true}
                      {:message-id  3
                       :content     "d"
                       :clock-value 3
                       :timestamp   3
                       :show?       true}]]
    (testing "New messages are grouped/sorted correctly, hidden messages are not grouped"
      (is (= '(2 3)
             (map :message-id
                  (-> (get-in (message/group-messages "chat-id" new-messages cofx)
                              [:db :chats "chat-id" :message-groups])
                      first
                      second)))))))

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
