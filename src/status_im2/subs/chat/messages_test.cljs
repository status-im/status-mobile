(ns status-im2.subs.chat.messages-test
  (:require [cljs.test :refer [deftest is testing]]
            [re-frame.db :as rf-db]
            [status-im2.constants :as constants]
            [status-im2.subs.chat.messages :as messages]
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(def messages-state
  [{:message-id "0x111" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
   {:message-id "0x222" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
   {:message-id "0x333" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
   {:message-id "0x444" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}])

(def messages-albumized-state
  [{:album [{:message-id "0x444" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
            {:message-id "0x333" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
            {:message-id "0x222" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}
            {:message-id "0x111" :album-id "abc" :albumize? true :from :xyz :timestamp-str "14:00"}]
    :album-id "abc"
    :albumize? true
    :message-id "0x444"
    :deleted? nil
    :deleted-for-me? nil
    :deleted-by nil
    :from :xyz
    :timestamp-str "14:00"
    :content-type constants/content-type-album}])

(deftest albumize-messages
  (testing "Finding albums in the messages list"
    (is (= (messages/albumize-messages messages-state) messages-albumized-state))))

(deftest intersperse-datemarks
  (testing "it mantains the order even when timestamps are across days"
    (let [message-1           {:datemark          "Dec 31, 1999"
                               :whisper-timestamp 946641600000} ; 1999}
          message-2           {:datemark          "Jan 1, 2000"
                               :whisper-timestamp 946728000000} ; 2000 this will displayed in 1999
          message-3           {:datemark          "Dec 31, 1999"
                               :whisper-timestamp 946641600000} ; 1999
          message-4           {:datemark          "Jan 1, 2000"
                               :whisper-timestamp 946728000000} ; 2000
          ordered-messages    [message-4
                               message-3
                               message-2
                               message-1]
          [m1 d1 m2 m3 m4 d2] (messages/add-datemarks ordered-messages)]
      (is (= "Jan 1, 2000"
             (:datemark m1)))
      (is (= {:type  :datemark
              :value "Jan 1, 2000"}
             d1))
      (is (= "Dec 31, 1999"
             (:datemark m2)
             (:datemark m3)
             (:datemark m4)))
      (is (= {:type  :datemark
              :value "Dec 31, 1999"}
             d2)))))

(def pinned-messages-state
  {:0xChat {:0x2 {:chat-id    :0xChat
                  :message-id :0x2
                  :pinned-at  2000
                  :pinned-by  :test-user}
            :0x1 {:chat-id    :0xChat
                  :message-id :0x1
                  :pinned-at  1000
                  :pinned-by  :test-user}
            :0x3 {:chat-id    :0xChat
                  :message-id :0x3
                  :pinned-at  3000
                  :pinned-by  :test-user}}})

(def pinned-messages-state-with-1-new-local-message
  {:0xChat {:0x3 {:chat-id    :0xChat
                  :message-id :0x3
                  :pinned-at  nil
                  :pinned-by  :test-user}
            :0x2 {:chat-id    :0xChat
                  :message-id :0x2
                  :pinned-at  3000
                  :pinned-by  :test-user}
            :0x1 {:chat-id    :0xChat
                  :message-id :0x1
                  :pinned-at  2000
                  :pinned-by  :test-user}}})

(def pinned-messages-state-with-2-new-local-messages
  {:0xChat {:0x3 {:chat-id    :0xChat
                  :message-id :0x3
                  :pinned-at  nil
                  :pinned-by  :test-user}
            :0x4 {:chat-id    :0xChat
                  :message-id :0x4
                  :pinned-at  nil
                  :pinned-by  :test-user}
            :0x2 {:chat-id    :0xChat
                  :message-id :0x2
                  :pinned-at  3000
                  :pinned-by  :test-user}
            :0x1 {:chat-id    :0xChat
                  :message-id :0x1
                  :pinned-at  2000
                  :pinned-by  :test-user}}})

(h/deftest-sub :chats/pinned-sorted-list
  [sub-name]
  (testing "It sorts three messages with pinned-at property"
    (swap! rf-db/app-db assoc :pin-messages pinned-messages-state)
    (is
     (= [{:chat-id         :0xChat
          :message-id      :0x1
          :pinned-at       1000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x2
          :pinned-at       2000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x3
          :pinned-at       3000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}]
        (rf/sub [sub-name :0xChat]))))
  (testing "It sorts messages from backend with pinned-at property and 1 new local pinned message"
    (swap! rf-db/app-db assoc :pin-messages pinned-messages-state-with-1-new-local-message)
    (is
     (= [{:chat-id         :0xChat
          :message-id      :0x1
          :pinned-at       2000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x2
          :pinned-at       3000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x3
          :pinned-at       nil
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}]
        (rf/sub [sub-name :0xChat]))))
  (testing "It sorts messages from backend with pinned-at property and 2 new local pinned messages"
    (swap! rf-db/app-db assoc :pin-messages pinned-messages-state-with-2-new-local-messages)
    (is
     (= [{:chat-id         :0xChat
          :message-id      :0x1
          :pinned-at       2000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x2
          :pinned-at       3000
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x3
          :pinned-at       nil
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}
         {:chat-id         :0xChat
          :message-id      :0x4
          :pinned-at       nil
          :pinned-by       :test-user
          :pinned          true
          :deleted?        nil
          :deleted-for-me? nil
          :deleted-by      nil}]
        (rf/sub [sub-name :0xChat])))))
