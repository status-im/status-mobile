(ns status-im.test.chat.subs
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.subs :as s]))

(deftest test-message-datemark-groups
  (testing "it orders a map of messages by clock-values desc, breaking ties by message-id asc and removing hidden messages"
    (let [message-1 {:show? true
                     :message-id "doesn't matter 1"
                     :clock-value 1}
          message-2 {:show? true
                     :message-id "doesn't matter 2"
                     :clock-value 2}
          message-3 {:show? true
                     :message-id "does matter 2"
                     :clock-value 3}
          message-4 {:show? true
                     :message-id "does matter 1"
                     :clock-value 3}
          hidden-message {:show? false
                          :clock-value 1}
          unordered-messages (->> [message-1
                                   message-2
                                   message-3
                                   message-4
                                   hidden-message]
                                  (map (juxt :message-id identity))
                                  shuffle ; clojure maps are sorted for n <= 32
                                  (into {}))]
      (is (= [message-4
              message-3
              message-2
              message-1] (s/sort-messages unordered-messages))))))

(deftest intersperse-datemarks
  (testing "it mantains the order even when timestamps are across days"
    (let [message-1 {:timestamp 946641600000} ; 1999}
          message-2 {:timestamp 946728000000} ; 2000 this will displayed in 1999
          message-3 {:timestamp 946641600000} ; 1999
          message-4 {:timestamp 946728000000} ; 2000
          ordered-messages [message-4
                            message-3
                            message-2
                            message-1]
          [m1 d1 m2 m3 m4 d2] (s/intersperse-datemarks ordered-messages)]
      (is (= "Jan 1, 2000"
             (:datemark m1)))
      (is (= {:type :datemark
              :value "Jan 1, 2000"} d1))
      (is (= "Dec 31, 1999"
             (:datemark m2)
             (:datemark m3)
             (:datemark m4)))
      (is (= {:type :datemark
              :value "Dec 31, 1999"} d2)))))

(deftest message-stream-tests
  (testing "messages with no interspersed datemarks"
    (let [m1 {:from "1"
              :datemark "a"
              :outgoing false}
          m2   {:from "2"
                :datemark "a"
                :outgoing true}
          m3    {:from "2"
                 :datemark "a"
                 :outgoing true}
          dm1  {:type :datemark
                :value "a"}
          messages [m1 m2 m3 dm1]
          [actual-m1
           actual-m2
           actual-m3] (s/messages-stream messages)]
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
      (testing "it marks the last message from the same author with :last-in-group?"
        (is (:last-in-group? actual-m1))
        (is (:last-in-group? actual-m2))
        (is (not (:last-in-group? actual-m3))))))
  (testing "messages with interspersed datemarks"
    (let [m1 {:from "2"          ; first & last in group
              :timestamp 63000
              :outgoing true}
          dm1 {:type :datemark
               :value "a"}
          m2  {:from "2"         ; first & last in group as more than 1 minute after previous message
               :timestamp 62000
               :outgoing false}
          m3  {:from "2"         ; last in group
               :timestamp 1
               :outgoing false}
          m4  {:from "2"         ; first in group
               :timestamp 0
               :outgoing false}
          dm2 {:type :datemark
               :value "b"}
          messages [m1 dm1 m2 m3 m4 dm2]
          [actual-m1
           _
           actual-m2
           actual-m3
           actual-m4
           _] (s/messages-stream messages)]
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
        (is (not (:last-in-group? actual-m4)))))))
