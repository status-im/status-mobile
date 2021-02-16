(ns status-im.chat.models.message-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models.message :as message]
            [status-im.chat.models.message-list :as models.message-list]
            [status-im.chat.models.loading :as loading]
            [status-im.utils.datetime :as time]
            [status-im.ui.screens.chat.state :as view.state]))

(deftest add-received-message-test
  (with-redefs [message/add-message #(identity %1)]
    (let [chat-id "chat-id"
          clock-value 10
          cursor-clock-value (dec clock-value)
          cursor (loading/clock-value->cursor cursor-clock-value)
          cofx {:now 0
                :db {:current-chat-id chat-id
                     :pagination-info {chat-id {:messages-initialized? true
                                                :cursor cursor
                                                :cursor-clock-value cursor-clock-value}}}}
          message #js {:localChatId chat-id
                       :clock       clock-value
                       :alias       "alias"
                       :name        "name"
                       :identicon   "identicon"
                       :from        "from"}]
      ;; <- cursor
      ;; <- message
      ;; <- top of the chat
      (testing "there's no hidden item"
        (with-redefs [view.state/first-not-visible-item (atom nil)]
          (is (= {:db {:current-chat-id "chat-id",
                       :pagination-info
                       {"chat-id"
                        {:messages-initialized? true
                         :cursor
                         "00000000000000000000000000000000000000000000000000090x0000000000000000000000000000000000000000000000000000000000000000",
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
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (dec clock-value)})]
          (is (= {:db {:current-chat-id "chat-id",
                       :pagination-info
                       {"chat-id"
                        {:messages-initialized? true
                         :cursor
                         "00000000000000000000000000000000000000000000000000090x0000000000000000000000000000000000000000000000000000000000000000",
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
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (dissoc (message/receive-many
                                cofx
                                #js {:messages (to-array [message])})
                               :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= (loading/clock-value->cursor clock-value) (get-in result [:db :pagination-info chat-id :cursor])))))))
      ;; <- message
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls between the first-hidden-item and cursor is nil"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (dissoc (message/receive-many
                                (update-in cofx [:db :pagination-info chat-id] dissoc :cursor :cursor-clock-value)
                                #js {:messages (to-array [message])})
                               :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= (loading/clock-value->cursor clock-value) (get-in result [:db :pagination-info chat-id :cursor])))))))
      ;; <- message
      ;; <- cursor
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls before both the first-hidden-item and cursor"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [message #js {:localChatId chat-id
                             :clock       (- clock-value 2)
                             :alias       "alias"
                             :name        "name"
                             :identicon   "identicon"
                             :from        "from"}
                result (dissoc (message/receive-many
                                cofx
                                #js {:messages (to-array [message])})
                               :utils/dispatch-later)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :pagination-info chat-id :all-loaded?]))))
            (testing "it does not update cursor-clock-value & cursor"
              (is (= cursor-clock-value (get-in result [:db :pagination-info chat-id :cursor-clock-value])))
              (is (= cursor (get-in result [:db :pagination-info chat-id :cursor]))))))))))

(deftest message-loaded?
  (testing "it returns false when it's not in loaded message"
    (is (not (#'status-im.chat.models.message/message-loaded? {:messages {"a" {}}} "a" "message-id"))))
  (testing "it returns true when it's already in the loaded message"
    (is (#'status-im.chat.models.message/message-loaded? {:messages {"a" {"message-id" {}}}} "a" "message-id"))))

(deftest earlier-than-deleted-at?
  (testing "it returns true when the clock-value is the same as the deleted-clock-value in chat"
    (is (#'status-im.chat.models.message/earlier-than-deleted-at?
         {:chats {"a" {:deleted-at-clock-value 1}}}
         "a"
         1)))
  (testing "it returns false when the clock-value is greater than the deleted-clock-value in chat"
    (is (not (#'status-im.chat.models.message/earlier-than-deleted-at?
              {:chats {"a" {:deleted-at-clock-value 1}}}
              "a"
              2))))

  (testing "it returns true when the clock-value is less than the deleted-clock-value in chat"
    (is (#'status-im.chat.models.message/earlier-than-deleted-at?
         {:chats {"a" {:deleted-at-clock-value 1}}}
         "a"
         0))))

(deftest delete-message
  (with-redefs [time/day-relative (constantly "day-relative")
                time/timestamp->time (constantly "timestamp")]
    (let [cofx1     {:db {:messages      {"chat-id" {0 {:message-id  0
                                                        :content     "a"
                                                        :clock-value 0
                                                        :whisper-timestamp 0
                                                        :timestamp   0}
                                                     1 {:message-id  1
                                                        :content     "b"
                                                        :clock-value 1
                                                        :whisper-timestamp 1
                                                        :timestamp   1}}}
                          :message-lists {"chat-id" [{:something :something}]}
                          :chats {"chat-id" {}}}}
          cofx2     {:db {:messages   {"chat-id"   {0 {:message-id  0
                                                       :content     "a"
                                                       :clock-value 0
                                                       :whisper-timestamp 1
                                                       :timestamp   1}}}
                          :message-list {"chat-id" [{:something :something}]}
                          :chats {"chat-id" {}}}}
          fx1       (message/delete-message cofx1 "chat-id" 1)
          fx2       (message/delete-message cofx2 "chat-id" 0)]
      (testing "Deleting message deletes it along with all references"
        (is (= '(0)
               (keys (get-in fx1 [:db :messages "chat-id"]))))
        (is (= [{:one-to-one? false
                 :message-id 0
                 :whisper-timestamp 0
                 :type :message
                 :display-photo? true
                 :system-message? false
                 :last-in-group? true
                 :datemark "day-relative"
                 :clock-value 0
                 :first-in-group? true
                 :from nil
                 :first-outgoing? false
                 :outgoing-seen? false
                 :timestamp-str "timestamp"
                 :first? true
                 :display-username? true
                 :outgoing false}]
               (models.message-list/->seq
                (get-in fx1 [:db :message-lists "chat-id"]))))
        (is (= {}
               (get-in fx2 [:db :messages "chat-id"])))
        (is (= nil
               (get-in fx2 [:db :message-lists "chat-id"])))))))
