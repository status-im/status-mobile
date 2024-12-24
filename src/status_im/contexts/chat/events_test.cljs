(ns status-im.contexts.chat.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.chat.events :as chat]
    [utils.datetime :as datetime]))

(deftest clear-history-test
  (let [chat-id "1"
        cofx    {:db {:message-lists {chat-id [{:something "a"}]}
                      :chats         {chat-id {:last-message            {:clock-value 10}
                                               :unviewed-messages-count 1}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/clear-history cofx chat-id true)]
        (is (= {} (get-in actual [:db :messages chat-id])))))
    (testing "it deletes all the message groups"
      (let [actual (chat/clear-history cofx chat-id true)]
        (is (= nil (get-in actual [:db :message-lists chat-id])))))
    (testing "it deletes unviewed messages set"
      (let [actual (chat/clear-history cofx chat-id true)]
        (is (= 0 (get-in actual [:db :chats chat-id :unviewed-messages-count])))))
    (testing "it sets a deleted-at-clock-value equal to the last message clock-value"
      (let [actual (chat/clear-history cofx chat-id true)]
        (is (= 10 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it does not override the deleted-at-clock-value when there are no messages"
      (let [actual (chat/clear-history (update-in cofx
                                                  [:db :chats chat-id]
                                                  assoc
                                                  :last-message           nil
                                                  :deleted-at-clock-value 100)
                                       chat-id
                                       true)]
        (is (= 100 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it set the deleted-at-clock-value to now the chat has no messages nor previous deleted-at"
      (with-redefs [datetime/timestamp (constantly 42)]
        (let [actual (chat/clear-history (update-in cofx
                                                    [:db :chats chat-id]
                                                    assoc
                                                    :last-message
                                                    nil)
                                         chat-id
                                         true)]
          (is (= 42 (get-in actual [:db :chats chat-id :deleted-at-clock-value]))))))))

(deftest remove-chat-test
  (let [chat-id "1"
        cofx    {:db {:current-chat-id chat-id
                      :chats-home-list #{chat-id}}}]
    (testing "it removes the chat with given id"
      (let [actual (chat/close-and-remove-chat cofx chat-id)]
        (is (= nil (get-in actual [:db :chats-home-list chat-id])))))))

(deftest multi-user-chat?-test
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/multi-user-chat? cofx chat-id))))
    (testing "it returns true if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (chat/multi-user-chat? cofx chat-id))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/multi-user-chat? cofx chat-id)))))))

(deftest group-chat?-test
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/group-chat? cofx chat-id))))
    (testing "it returns false if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (not (chat/group-chat? cofx chat-id)))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/group-chat? cofx chat-id)))))))

(deftest navigate-to-chat-test
  (let [chat-id "test_chat"
        db      {:pagination-info {chat-id {:all-loaded? true}}}]
    (testing "Pagination info should be reset on navigation"
      (let [res (chat/navigate-to-chat {:db db} chat-id nil)]
        (is (nil? (get-in res [:db :pagination-info chat-id :all-loaded?])))))))
