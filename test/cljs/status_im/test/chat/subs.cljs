(ns status-im.test.chat.subs
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.subs :as s]))


(defn messages-ordered? [messages]
  (let [clock-values (map :clock-value messages)]
    (= (-> clock-values sort reverse) clock-values)))

(deftest test-message-datemark-groups
  (testing "it orders a map of messages by clock-values when all on the same day (by sender timestamp)"
    (let [datemark "Jan 1, 1970"
          message-1 {:show? true
                     :timestamp 0
                     :clock-value 1}
          message-2 {:show? true
                     :timestamp 0
                     :clock-value 2}
          message-3 {:show? true
                     :timestamp 0
                     :clock-value 3}
          unordered-messages {2 message-2
                              1 message-1
                              3 message-3}
          [[actual-datemark actual-messages]] (s/message-datemark-groups unordered-messages)]

      (is (= datemark actual-datemark))
      (is (= 3 (count actual-messages)))
      (is (messages-ordered? actual-messages))))

  (testing "it mantains the order even when timestamps are across days"
    (let [datemark-day-1 "Jan 1, 2000"
          datemark-day-2 "Dec 31, 1999"
          message-1 {:show? true
                     :timestamp 946641600000 ; 1999
                     :clock-value 1}
          message-2 {:show? true
                     :timestamp 946728000000 ; 2000 this will displayed in 1999
                     :clock-value 2}
          message-3 {:show? true
                     :timestamp 946641600000 ; 1999
                     :clock-value 3}
          message-4 {:show? true
                     :timestamp 946728000000 ; 2000
                     :clock-value 4}
          unordered-messages {2 message-2
                              1 message-1
                              4 message-4
                              3 message-3}
          [[actual-dm-1 actual-msg-1]
           [actual-dm-2 actual-msg-2]] (s/message-datemark-groups unordered-messages)]

      (is (= datemark-day-1 actual-dm-1))
      (is (= datemark-day-2 actual-dm-2))
      (is (= 1 (count actual-msg-1)))
      (is (= 3 (count actual-msg-2)))
      (is (messages-ordered? (concat actual-msg-1 actual-msg-2))))))
