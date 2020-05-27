(ns status-im.chat.models.message-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message :as message]
            [status-im.chat.models.message-list :as models.message-list]
            [status-im.constants :as constants]
            [status-im.ui.screens.chat.state :as view.state]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]))

(deftest add-received-message-test
  (with-redefs [message/add-message (constantly :added)]
    (let [chat-id "chat-id"
          clock-value 10
          cursor-clock-value (dec clock-value)
          cursor (chat-loading/clock-value->cursor cursor-clock-value)
          cofx {:now 0
                :db {:loaded-chat-id chat-id
                     :current-chat-id chat-id
                     :all-loaded? true
                     :chats {chat-id {:cursor cursor
                                      :cursor-clock-value cursor-clock-value}}}}
          message {:chat-id chat-id
                   :clock-value clock-value}]
      (testing "not current-chat"
        (is (nil? (message/add-received-message
                   (update cofx :db dissoc :loaded-chat-id)
                   message))))
      ;; <- cursor
      ;; <- message
      ;; <- top of the chat
      (testing "there's no hidden item"
        (with-redefs [view.state/first-not-visible-item (atom nil)]
          (is (= :added (message/add-received-message
                         cofx
                         message)))))
      ;; <- cursor
      ;; <- first-hidden-item
      ;; <- message
      ;; <- top of the chat
      (testing "the hidden item has a clock value less than the current"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (dec clock-value)})]
          (is (= :added (message/add-received-message
                         cofx
                         message)))))
      ;; <- cursor
      ;; <- message
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls between the first-hidden-item and cursor"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (message/add-received-message
                        cofx
                        message)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :chats chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :chats chat-id :cursor-clock-value])))
              (is (= (chat-loading/clock-value->cursor clock-value) (get-in result [:db :chats chat-id :cursor])))))))
      ;; <- message
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls between the first-hidden-item and cursor is nil"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (message/add-received-message
                        (update-in cofx [:db :chats chat-id] dissoc :cursor :cursor-clock-value)
                        message)]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :chats chat-id :all-loaded?]))))
            (testing "it updates cursor-clock-value & cursor"
              (is (= clock-value (get-in result [:db :chats chat-id :cursor-clock-value])))
              (is (= (chat-loading/clock-value->cursor clock-value) (get-in result [:db :chats chat-id :cursor])))))))
      ;; <- message
      ;; <- cursor
      ;; <- first-hidden-item
      ;; <- top of the chat
      (testing "the message falls before both the first-hidden-item and cursor"
        (with-redefs [view.state/first-not-visible-item (atom {:clock-value (inc clock-value)})]
          (let [result (message/add-received-message
                        cofx
                        (update message :clock-value #(- % 2)))]
            (testing "it sets all-loaded? to false"
              (is (not (get-in result [:db :chats chat-id :all-loaded?]))))
            (testing "it does not update cursor-clock-value & cursor"
              (is (= cursor-clock-value (get-in result [:db :chats chat-id :cursor-clock-value])))
              (is (= cursor (get-in result [:db :chats chat-id :cursor]))))))))))

(deftest message-loaded?
  (testing "it returns false when it's not in loaded message"
    (is (not (#'status-im.chat.models.message/message-loaded? {:db {:chats {"a" {}}}}
                                                              {:message-id "message-id"
                                                               :from "a"
                                                               :clock-value 1
                                                               :chat-id "a"}))))
  (testing "it returns true when it's already in the loaded message"
    (is (#'status-im.chat.models.message/message-loaded? {:db
                                                          {:messages {"a" {"message-id" {}}}}}
                                                         {:message-id "message-id"
                                                          :from "a"
                                                          :clock-value 1
                                                          :chat-id "a"}))))
(deftest earlier-than-deleted-at?
  (testing "it returns true when the clock-value is the same as the deleted-clock-value in chat"
    (is (#'status-im.chat.models.message/earlier-than-deleted-at? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                                                                  {:message-id "message-id"
                                                                   :from "a"
                                                                   :clock-value 1
                                                                   :chat-id "a"})))
  (testing "it returns false when the clock-value is greater than the deleted-clock-value in chat"
    (is (not (#'status-im.chat.models.message/earlier-than-deleted-at? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                                                                       {:message-id "message-id"
                                                                        :from "a"
                                                                        :clock-value 2
                                                                        :chat-id "a"}))))
  (testing "it returns true when the clock-value is less than the deleted-clock-value in chat"
    (is (#'status-im.chat.models.message/earlier-than-deleted-at? {:db {:chats {"a" {:deleted-at-clock-value 1}}}}
                                                                  {:message-id "message-id"
                                                                   :from "a"
                                                                   :clock-value 0
                                                                   :chat-id "a"}))))

(deftest add-own-received-message
  (let [db {:multiaccount {:public-key "me"}
            :view-id :chat
            :loaded-chat-id "chat-id"
            :current-chat-id "chat-id"
            :messages {"chat-id" {}}
            :chats {"chat-id" {}}}]
    (testing "a message coming from you!"
      (let [actual (message/receive-one {:db db}
                                        {:from "me"
                                         :message-type constants/message-type-one-to-one
                                         :timestamp 0
                                         :whisper-timestamp 0
                                         :message-id "id"
                                         :chat-id "chat-id"
                                         :outgoing true
                                         :content "b"
                                         :clock-value 1})
            message (get-in actual [:db :messages "chat-id" "id"])]
        (testing "it adds the message"
          (is message))))))

(deftest receive-group-chats
  (let [cofx                 {:db {:chats {"chat-id" {:contacts #{"present"}
                                                      :members-joined #{"a"}}}
                                   :multiaccount {:public-key "a"}
                                   :loaded-chat-id "chat-id"
                                   :current-chat-id "chat-id"
                                   :view-id :chat}}
        cofx-without-member  (update-in cofx [:db :chats "chat-id" :members-joined] disj "a")
        valid-message        {:chat-id     "chat-id"
                              :from        "present"
                              :message-type constants/message-type-private-group
                              :message-id  "1"
                              :clock-value 1
                              :whisper-timestamp 0
                              :timestamp   0}
        bad-chat-id-message  {:chat-id     "bad-chat-id"
                              :from        "present"
                              :message-type constants/message-type-private-group
                              :message-id  "1"
                              :clock-value 1
                              :whisper-timestamp 0
                              :timestamp   0}
        bad-from-message     {:chat-id     "chat-id"
                              :from        "not-present"
                              :message-type constants/message-type-private-group
                              :message-id  "1"
                              :clock-value 1
                              :whisper-timestamp 0
                              :timestamp   0}]
    (testing "a valid message"
      (is (get-in (message/receive-one cofx valid-message) [:db :messages "chat-id" "1"])))
    (testing "a message from someone not in the list of participants"
      (is (not (message/receive-one cofx bad-from-message))))
    (testing "a message with non existing chat-id"
      (is (not (message/receive-one cofx bad-chat-id-message))))
    (testing "a message from a delete chat"
      (is (not (message/receive-one cofx-without-member valid-message))))))

(deftest receive-public-chats
  (let [cofx                 {:db {:chats {"chat-id" {:public? true}}
                                   :multiaccount {:public-key "a"}
                                   :loaded-chat-id "chat-id"
                                   :current-chat-id "chat-id"
                                   :view-id :chat}}
        valid-message        {:chat-id     "chat-id"
                              :from        "anyone"
                              :message-type constants/message-type-public-group
                              :message-id  "1"
                              :clock-value 1
                              :whisper-timestamp 0
                              :timestamp   0}
        bad-chat-id-message  {:chat-id     "bad-chat-id"
                              :from        "present"
                              :message-type constants/message-type-public-group
                              :message-id  "1"
                              :clock-value 1
                              :whisper-timestamp 0
                              :timestamp   0}]
    (testing "a valid message"
      (is (get-in (message/receive-one cofx valid-message) [:db :messages "chat-id" "1"])))
    (testing "a message with non existing chat-id"
      (is (not (message/receive-one cofx bad-chat-id-message))))))

(deftest receive-one-to-one
  (with-redefs [gfycat/generate-gfy (constantly "generated")
                identicon/identicon (constantly "generated")]

    (let [cofx                 {:db {:chats {"matching" {}}
                                     :multiaccount {:public-key "me"}
                                     :current-chat-id "matching"
                                     :loaded-chat-id "matching"
                                     :view-id :chat}}
          valid-message        {:chat-id     "matching"
                                :from        "matching"
                                :message-type constants/message-type-one-to-one
                                :message-id  "1"
                                :clock-value 1
                                :whisper-timestamp 0
                                :timestamp   0}
          own-message          {:chat-id     "matching"
                                :from        "me"
                                :message-type constants/message-type-one-to-one
                                :message-id  "1"
                                :clock-value 1
                                :whisper-timestamp 0
                                :timestamp   0}

          bad-chat-id-message  {:chat-id     "bad-chat-id"
                                :from        "not-matching"
                                :message-type constants/message-type-one-to-one
                                :message-id  "1"
                                :clock-value 1
                                :whisper-timestamp 0
                                :timestamp   0}]
      (testing "a valid message"
        (is (get-in (message/receive-one cofx valid-message) [:db :messages "matching" "1"])))
      (testing "our own message"
        (is (get-in (message/receive-one cofx own-message) [:db :messages "matching" "1"])))
      (testing "a message with non matching chat-id"
        (is (not (get-in (message/receive-one cofx bad-chat-id-message) [:db :messages "not-matching" "1"])))))))

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
