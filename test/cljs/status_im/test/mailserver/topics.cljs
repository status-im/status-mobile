(ns status-im.test.mailserver.topics
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.utils :as utils]
            [status-im.mailserver.constants :as c]
            [status-im.mailserver.topics :as t]
            [status-im.utils.random :as rand]))

(def now-s 100000)

(deftest test-merge-topic-basic-functionality
  (testing "a new topic"
    (let [old-topic {:chat-ids #{}}
          new-topic {:topic "a"
                     :chat-ids #{"a"}
                     :filter-ids #{"b" "c"}}
          expected-topic {:last-request (- now-s c/max-request-range)
                          :topic "a"
                          :filter-ids #{"b" "c"}
                          :discovery? false
                          :negotiated? false
                          :chat-ids #{"a"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s})))))
  (testing "an already existing topic"
    (let [old-topic {:filter-ids #{"a" "b"}
                     :chat-ids #{"a" "b"}}
          new-topic {:topic "a"
                     :filter-ids #{"a" "c"}
                     :chat-ids #{"a" "c"}}
          expected-topic {:last-request (- now-s c/max-request-range)
                          :topic "a"
                          :discovery? false
                          :negotiated? false
                          :filter-ids #{"a" "b" "c"}
                          :chat-ids #{"a" "b" "c"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s}))))))

(deftest test-merge-topic
  (testing "previous last request is nil and not discovery"
    (let [old-topic {:chat-ids #{}}
          new-topic {:chat-ids #{"a"}}
          expected-topic {:last-request (- now-s c/max-request-range)
                          :discovery? false
                          :negotiated? false
                          :topic nil
                          :filter-ids nil
                          :chat-ids #{"a"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s})))))
  (testing "previous last request is nil and discovery"
    (let [old-topic {:chat-ids #{}}
          new-topic {:discovery? true
                     :chat-ids #{"a"}}
          expected-topic {:last-request (- now-s 10)
                          :discovery? true
                          :negotiated? false
                          :topic nil
                          :filter-ids nil
                          :chat-ids #{"a"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s})))))
  (testing "previous last request is set to less then max range ago"
    (let [old-last-request (inc (- now-s c/max-request-range))
          old-topic {:last-request old-last-request
                     :chat-ids #{}}
          new-topic {:chat-ids #{"a"}}
          expected-topic {:last-request old-last-request
                          :discovery? false
                          :negotiated? false
                          :topic nil
                          :filter-ids nil
                          :chat-ids #{"a"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s})))))
  (testing "previous last request is set to less then max range ago"
    (let [old-last-request (- now-s (* 2 c/max-request-range))
          old-topic {:last-request old-last-request
                     :chat-ids #{}}
          new-topic {:chat-ids #{"a"}}
          expected-topic {:last-request (- now-s c/max-request-range)
                          :discovery? false
                          :negotiated? false
                          :topic nil
                          :filter-ids nil
                          :chat-ids #{"a"}}]
      (is (= expected-topic
             (t/merge-topic old-topic new-topic {:now-s now-s}))))))

(deftest new-chat-ids?
  (testing "new-chat-ids?"
    (is (not (t/new-chat-ids? {:chat-ids #{"a" "b" "c"}}
                              {:chat-ids #{"a"}})))
    (is (t/new-chat-ids? {:chat-ids #{"a" "b"}}
                         {:chat-ids #{"a" "b" "c"}}))))

(deftest topics-for-chat
  (testing "a public chat"
    (testing "the chat is in multiple topics"
      (is (= #{"a" "b"}
             (t/topics-for-chat {:chats {"chat-id-1" {:public? true}}
                                 :mailserver/topics {"a" {:chat-ids #{"chat-id-1" "chat-id-2"}}
                                                     "b" {:chat-ids #{"chat-id-1"}}
                                                     "c" {:discovery? true
                                                          :chat-ids #{"chat-id-2"}}}}
                                "chat-id-1"))))
    (testing "the chat is not there"
      (is (= #{}
             (t/topics-for-chat {:chats {"chat-id-3" {:public? true}}
                                 :mailserver/topics {"a" {:chat-ids #{"chat-id-1" "chat-id-2"}}
                                                     "b" {:chat-ids #{"chat-id-1"}}
                                                     "c" {:discovery? true
                                                          :chat-ids #{"chat-id-2"}}}}
                                "chat-id-3")))))
  (testing "a one to one"
    (is (= #{"a" "c"}
           (t/topics-for-chat {:mailserver/topics {"a" {:chat-ids #{"chat-id-1" "chat-id-2"}}
                                                   "b" {:chat-ids #{"chat-id-1"}}
                                                   "c" {:discovery? true}}}
                              "chat-id-2")))))

(deftest upsert-group-chat-test
  (testing "new group chat"
    (let [expected-topics {:modified [{:topic "2"
                                       :chat-ids #{"chat-id"}}
                                      {:topic "4"
                                       :chat-ids #{"chat-id"}}]
                           :removed []}]
      (is (= expected-topics
             (t/changed-for-group-chat [{:topic "1"
                                         :discovery? true
                                         :chat-ids #{}}
                                        {:topic "2"
                                         :chat-ids #{"a"}}
                                        {:topic "3"
                                         :chat-ids #{"b"}}
                                        {:topic "4"
                                         :chat-ids #{"c"}}]
                                       "chat-id"
                                       ["a" "c"])))))
  (testing "existing group chat"
    (let [expected-topics {:modified [{:topic "2"
                                       :chat-ids #{"chat-id"}}
                                      {:topic "4"
                                       :chat-ids #{"chat-id"}}]
                           :removed [{:topic "3"
                                      :chat-ids #{"b"}}]}]
      (is (= expected-topics
             (t/changed-for-group-chat [{:topic "1"
                                         :discovery? true
                                         :chat-ids #{}}
                                        {:topic "2"
                                         :chat-ids #{"a"}}
                                        {:topic "3"
                                         :chat-ids #{"chat-id" "b"}}
                                        {:topic "4"
                                         :chat-ids #{"c"}}]
                                       "chat-id"
                                       ["a" "c"]))))))
