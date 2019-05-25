(ns status-im.test.chat.db
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.db :as db]))

(deftest group-chat-name
  (testing "it prepends # if it's a public chat"
    (is (= "#withhash" (db/group-chat-name {:group-chat true
                                            :chat-id    "1"
                                            :public?    true
                                            :name       "withhash"}))))
  (testing "it leaves the name unchanged if it's a group chat"
    (is (= "unchanged" (db/group-chat-name {:group-chat true
                                            :chat-id    "1"
                                            :name       "unchanged"})))))

(deftest message-stream-tests
  (testing "messages with no interspersed datemarks"
    (let [m1       {:from     "1"
                    :datemark "a"
                    :outgoing false}
          m2       {:from     "2"
                    :datemark "a"
                    :outgoing true}
          m3       {:from     "2"
                    :datemark "a"
                    :outgoing true}
          dm1      {:type  :datemark
                    :value "a"}
          messages [m1 m2 m3 dm1]
          [actual-m1
           actual-m2
           actual-m3] (db/messages-stream messages)]
      (testing "it marks only the first message as :last?"
        (is (:last? actual-m1))
        (is (not (:last? actual-m2)))
        (is (not (:last? actual-m3))))
      (testing "it marks the first outgoing message as :last-outgoing?"
        (is (not (:last-outgoing? actual-m1)))
        (is (:last-outgoing? actual-m2))
        (is (not (:last-outgoing? actual-m3))))
      (testing "it marks the message with :same-direction? when from the same author"
        (is (not (:same-direction? actual-m1)))
        (is (not (:same-direction? actual-m2))
            (is (:same-direction? actual-m3))))
      (testing "it marks messages from the same author next to another with :first-in-group?"
        (is (:first-in-group? actual-m1))
        (is (not (:first-in-group? actual-m2)))
        (is (:first-in-group? actual-m3)))
      (testing "it marks messages with display-photo? when they are not outgoing and we are in a group chat"
        (is (:display-photo? actual-m1))
        (is (not (:display-photo? actual-m2)))
        (is (not (:display-photo? actual-m3))))
      (testing "it marks messages with display-username? when we display the photo and are the first in a group"
        (is (:display-username? actual-m1))
        (is (not (:display-username? actual-m2)))
        (is (not (:display-username? actual-m3))))
      (testing "it marks the last message from the same author with :last-in-group?"
        (is (:last-in-group? actual-m1))
        (is (:last-in-group? actual-m2))
        (is (not (:last-in-group? actual-m3))))))
  (testing "messages with interspersed datemarks"
    (let [m1       {:from      "2"                          ; first & last in group
                    :timestamp 63000
                    :outgoing  true}
          dm1      {:type  :datemark
                    :value "a"}
          m2       {:from      "2"                          ; first & last in group as more than 1 minute after previous message
                    :timestamp 62000
                    :outgoing  false}
          m3       {:from      "2"                          ; last in group
                    :timestamp 1
                    :outgoing  false}
          m4       {:from      "2"                          ; first in group
                    :timestamp 0
                    :outgoing  false}
          dm2      {:type  :datemark
                    :value "b"}
          messages [m1 dm1 m2 m3 m4 dm2]
          [actual-m1
           _
           actual-m2
           actual-m3
           actual-m4
           _] (db/messages-stream messages)]
      (testing "it marks the first outgoing message as :last-outgoing?"
        (is (:last-outgoing? actual-m1))
        (is (not (:last-outgoing? actual-m2)))
        (is (not (:last-outgoing? actual-m3)))
        (is (not (:last-outgoing? actual-m4))))
      (testing "it resets :same-direction? after a datemark"
        (is (not (:same-direction? actual-m1)))
        (is (not (:same-direction? actual-m2))
            (is (:same-direction? actual-m3)))
        (is (:same-direction? actual-m4)))
      (testing "it sets :first-in-group? after a datemark"
        (is (:first-in-group? actual-m1))
        (is (:first-in-group? actual-m4)))
      (testing "it sets :first-in-group? if more than 60s have passed since last message"
        (is (:first-in-group? actual-m2)))
      (testing "it sets :last-in-group? after a datemark"
        (is (:last-in-group? actual-m1))
        (is (:last-in-group? actual-m2))
        (is (:last-in-group? actual-m3))
        (is (not (:last-in-group? actual-m4))))))
  (testing "system-messages"
    (let [m1       {:from         "system"
                    :message-type :system-message
                    :datemark     "a"
                    :outgoing     false}
          messages [m1]
          [actual-m1] (db/messages-stream messages)]
      (testing "it does display the photo"
        (is (:display-photo? actual-m1))
        (testing "it does not display the username"
          (is (not (:display-username? actual-m1))))))))

(deftest active-chats-test
  (let [active-chat-1 {:is-active true :chat-id "1"}
        active-chat-2 {:is-active true :chat-id "2"}
        chats         {"1" active-chat-1
                       "2" active-chat-2
                       "3" {:is-active false :chat-id "3"}}]
    (testing "it returns only chats with is-active"
      (is (= #{"1" "2"}
             (set (keys (db/active-chats {} chats {}))))))))

(deftest messages-with-datemarks-and-statuses
  (testing "empty state"
    (is (empty?
         (db/messages-with-datemarks-and-statuses
          nil
          nil
          nil
          nil
          nil
          nil
          false
          false))))
  (testing "empty state pub-chat"
    (is (=
         [{:type       :gap
           :value      ":first-gap"
           :first-gap? true}]
         (db/messages-with-datemarks-and-statuses
          nil
          nil
          nil
          nil
          nil
          {:lowest-request-from 10
           :highest-request-to  30}
          true
          true))))
  (testing "simple case"
    (is (=
         '({:whisper-timestamp 40
            :timestamp         40
            :content           nil
            :timestamp-str     "14:00"
            :user-statuses     nil
            :datemark          "today"}
           {:whisper-timestamp 30
            :timestamp         30
            :content           nil
            :timestamp-str     "13:00"
            :user-statuses     nil
            :datemark          "today"}
           {:value             "today"
            :type              :datemark
            :whisper-timestamp 30
            :timestamp         30}
           {:whisper-timestamp 20
            :timestamp         20
            :content           nil
            :timestamp-str     "12:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:whisper-timestamp 10
            :timestamp         10
            :content           nil
            :timestamp-str     "11:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:value             "yesterday"
            :type              :datemark
            :whisper-timestamp 10
            :timestamp         10})
         (db/messages-with-datemarks-and-statuses
          {"yesterday"
           (list
            {:message-id        :m1
             :timestamp-str     "11:00"
             :whisper-timestamp 10
             :timestamp         10}
            {:message-id        :m2
             :timestamp-str     "12:00"
             :whisper-timestamp 20
             :timestamp         20})
           "today"
           (list
            {:message-id        :m3
             :timestamp-str     "13:00"
             :whisper-timestamp 30
             :timestamp         30}
            {:message-id        :m4
             :timestamp-str     "14:00"
             :whisper-timestamp 40
             :timestamp         40})}
          {:m1 {:whisper-timestamp 10
                :timestamp         10}
           :m2 {:whisper-timestamp 20
                :timestamp         20}
           :m3 {:whisper-timestamp 30
                :timestamp         30}
           :m4 {:whisper-timestamp 40
                :timestamp         40}}
          nil
          nil
          nil))))
  (testing "simple case with gap"
    (is (=
         '({:whisper-timestamp 40
            :timestamp         40
            :content           nil
            :timestamp-str     "14:00"
            :user-statuses     nil
            :datemark          "today"}
           {:type  :gap
            :value ":gapid1"
            :gaps  {:ids [:gapid1]}}
           {:whisper-timestamp 30
            :timestamp         30
            :content           nil
            :timestamp-str     "13:00"
            :user-statuses     nil
            :datemark          "today"}
           {:value             "today"
            :type              :datemark
            :whisper-timestamp 30
            :timestamp         30}
           {:whisper-timestamp 20
            :timestamp         20
            :content           nil
            :timestamp-str     "12:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:whisper-timestamp 10
            :timestamp         10
            :content           nil
            :timestamp-str     "11:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:value             "yesterday"
            :type              :datemark
            :whisper-timestamp 10
            :timestamp         10})
         (db/messages-with-datemarks-and-statuses
          {"yesterday"
           (list
            {:message-id        :m1
             :timestamp-str     "11:00"
             :whisper-timestamp 10
             :timestamp         10}
            {:message-id        :m2
             :timestamp-str     "12:00"
             :whisper-timestamp 20
             :timestamp         20})
           "today"
           (list
            {:message-id        :m3
             :timestamp-str     "13:00"
             :whisper-timestamp 30
             :timestamp         30}
            {:message-id        :m4
             :timestamp-str     "14:00"
             :whisper-timestamp 40
             :timestamp         40})}
          {:m1 {:whisper-timestamp 10
                :timestamp         10}
           :m2 {:whisper-timestamp 20
                :timestamp         20}
           :m3 {:whisper-timestamp 30
                :timestamp         30}
           :m4 {:whisper-timestamp 40
                :timestamp         40}}
          nil
          nil
          [{:from 25
            :to   30
            :id   :gapid1}]))))
  (testing "simple case with gap after all messages"
    (is (=
         '({:type  :gap
            :value ":gapid1"
            :gaps  {:ids (:gapid1)}}
           {:whisper-timestamp 40
            :timestamp         40
            :content           nil
            :timestamp-str     "14:00"
            :user-statuses     nil
            :datemark          "today"}
           {:whisper-timestamp 30
            :timestamp         30
            :content           nil
            :timestamp-str     "13:00"
            :user-statuses     nil
            :datemark          "today"}
           {:value             "today"
            :type              :datemark
            :whisper-timestamp 30
            :timestamp         30}
           {:whisper-timestamp 20
            :timestamp         20
            :content           nil
            :timestamp-str     "12:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:whisper-timestamp 10
            :timestamp         10
            :content           nil
            :timestamp-str     "11:00"
            :user-statuses     nil
            :datemark          "yesterday"}
           {:value             "yesterday"
            :type              :datemark
            :whisper-timestamp 10
            :timestamp         10})
         (db/messages-with-datemarks-and-statuses
          {"yesterday"
           (list
            {:message-id        :m1
             :timestamp-str     "11:00"
             :whisper-timestamp 10
             :timestamp         10}
            {:message-id        :m2
             :timestamp-str     "12:00"
             :whisper-timestamp 20
             :timestamp         20})
           "today"
           (list
            {:message-id        :m3
             :timestamp-str     "13:00"
             :whisper-timestamp 30
             :timestamp         30}
            {:message-id        :m4
             :timestamp-str     "14:00"
             :whisper-timestamp 40
             :timestamp         40})}
          {:m1 {:whisper-timestamp 10
                :timestamp         10}
           :m2 {:whisper-timestamp 20
                :timestamp         20}
           :m3 {:whisper-timestamp 30
                :timestamp         30}
           :m4 {:whisper-timestamp 40
                :timestamp         40}}
          nil
          nil
          [{:from 100
            :to   110
            :id   :gapid1}])))))
