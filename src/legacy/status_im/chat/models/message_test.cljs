(ns legacy.status-im.chat.models.message-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.chat.models.loading :as loading]
    [legacy.status-im.chat.models.message :as message]
    [status-im2.contexts.chat.messages.list.state :as list.state]))

(deftest add-received-message-test
  (with-redefs [message/add-message #(identity %1)]
    (let [chat-id            "chat-id"
          clock-value        10
          cursor-clock-value (dec clock-value)
          cursor             (loading/clock-value->cursor cursor-clock-value)
          cofx               {:now 0
                              :db  {:current-chat-id chat-id
                                    :pagination-info {chat-id {:messages-initialized? true
                                                               :cursor cursor
                                                               :cursor-clock-value cursor-clock-value}}}}
          message            #js
                              {:localChatId chat-id
                               :clock       clock-value
                               :alias       "alias"
                               :name        "name"
                               :from        "from"}]
      ;; <- cursor
      ;; <- message
      ;; <- top of the chat
      (testing "there's no hidden item"
        (with-redefs [list.state/first-not-visible-item (atom nil)]
          (is
           (=
            {:db
             {:current-chat-id "chat-id"
              :pagination-info
              {"chat-id"
               {:messages-initialized? true
                :cursor
                "00000000000000000000000000000000000000000000000000090x0000000000000000000000000000000000000000000000000000000000000000"
                :cursor-clock-value 9}}}}
            (dissoc (message/receive-many
                     cofx
                     #js {:messages (to-array [message])})
             :utils/dispatch-later)))))
      ;; <- cursor
      ;; <- first-hidden-item
      ;; <- message
      ;; <- top of the chat
      (testing "the hidden item has a clock value less than the current"
        (with-redefs [list.state/first-not-visible-item (atom {:clock-value (dec clock-value)})]
          (is
           (=
            {:db
             {:current-chat-id "chat-id"
              :pagination-info
              {"chat-id"
               {:messages-initialized? true
                :cursor
                "00000000000000000000000000000000000000000000000000090x0000000000000000000000000000000000000000000000000000000000000000"
                :cursor-clock-value 9}}}}
            (dissoc (message/receive-many
                     cofx
                     #js {:messages (to-array [message])})
             :utils/dispatch-later)))))
      ;; <- cursor
      ;; <- message
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls between the first-hidden-item and cursor"
        (with-redefs [list.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (dissoc (message/receive-many
                                cofx
                                #js {:messages (to-array [message])})
                        :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= (loading/clock-value->cursor clock-value)
                     (get-in result [:db :pagination-info chat-id :cursor])))))))
      ;; <- message
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls between the first-hidden-item and cursor is nil"
        (with-redefs [list.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (dissoc (message/receive-many
                                (update-in cofx
                                           [:db :pagination-info chat-id]
                                           dissoc
                                           :cursor
                                           :cursor-clock-value)
                                #js {:messages (to-array [message])})
                        :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= (loading/clock-value->cursor clock-value)
                     (get-in result [:db :pagination-info chat-id :cursor])))))))
      ;; <- message
      ;; <- cursor
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls before both the first-hidden-item and cursor"
        (with-redefs [list.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [message #js
                         {:localChatId chat-id
                          :clock       (- clock-value 2)
                          :alias       "alias"
                          :name        "name"
                          :from        "from"}
                result  (dissoc (message/receive-many
                                 cofx
                                 #js {:messages (to-array [message])})
                         :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it does not update cursor-clock-value & cursor"
              (is (= cursor-clock-value
                     (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= cursor (get-in result [:db :pagination-info chat-id :cursor]))))))))))

(deftest message-loaded?
  (testing "it returns false when it's not in loaded message"
    (is
     (not
      (#'legacy.status-im.chat.models.message/message-loaded? {:messages {"a" {}}} "a" "message-id"))))
  (testing "it returns true when it's already in the loaded message"
    (is (#'legacy.status-im.chat.models.message/message-loaded?
         {:messages {"a" {"message-id" {}}}}
         "a"
         "message-id"))))

(deftest earlier-than-deleted-at?
  (testing "it returns true when the clock-value is the same as the deleted-clock-value in chat"
    (is (#'legacy.status-im.chat.models.message/earlier-than-deleted-at?
         {:chats {"a" {:deleted-at-clock-value 1}}}
         "a"
         1)))
  (testing "it returns false when the clock-value is greater than the deleted-clock-value in chat"
    (is (not (#'legacy.status-im.chat.models.message/earlier-than-deleted-at?
              {:chats {"a" {:deleted-at-clock-value 1}}}
              "a"
              2))))

  (testing "it returns true when the clock-value is less than the deleted-clock-value in chat"
    (is (#'legacy.status-im.chat.models.message/earlier-than-deleted-at?
         {:chats {"a" {:deleted-at-clock-value 1}}}
         "a"
         0))))
