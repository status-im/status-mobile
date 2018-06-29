(ns status-im.test.transport.filters
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.filters :as t]
            [status-im.constants :as constants]))

(def pk "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f")

(def expected-users 40000)
(def average-number-of-contacts 100)
;; Minimum number of identical personal topics
(def min-number-of-clashes 1000)
;; Maximum share of traffic that each users will be receiving
(def max-share-of-traffic (/ 1 250))
(def partitioned-topic "0xed2fdbad")
(def discovery-topic "0xf8946aac")

(deftest partition-topic
  (testing "it returns a seeded topic based on an input string"
    (is (= partitioned-topic (t/partition-topic pk))))
  (testing "same input same output"
    (is (= (t/partition-topic "a") (t/partition-topic "a")))))

(deftest discovery-topics
  (testing "it returns a partition topic & the discovery topic"
    (is (= [partitioned-topic discovery-topic]
           (t/discovery-topics pk)))))

(deftest minimum-number-of-clashes-test
  (testing (str "it needs to be greater than " min-number-of-clashes)
    (is (<= min-number-of-clashes (t/expected-number-of-collisions expected-users)))))

(deftest max-avg-share-of-traffic-test
  (testing (str "it needs to be less than " max-share-of-traffic)
    (is (>= max-share-of-traffic
            ;; we always listen to our personal topic +
            ;; we listen to contact topics - collissions
            (/ (+ 1 (- average-number-of-contacts
                       (t/expected-number-of-collisions average-number-of-contacts)))
               expected-users)))))
