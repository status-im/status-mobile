(ns status-im.test.utils.mixpanel
  (:require [cljs.test :refer-macros [deftest is testing async]]
            [status-im.utils.mixpanel :as mixpanel]))

(deftest events
  (is (not (nil? mixpanel/events))))

(deftest matches?
  (is (true? (mixpanel/matches? [:key] [:key])))
  (is (false? (mixpanel/matches? [:key1] [:key2])))
  (is (true? (mixpanel/matches? [:key :subkey] [:key])))
  (is (false? (mixpanel/matches? [:key] [:key :subkey]))))

(def definitions {[:key] {:trigger [:key]} [:key :subkey] {:trigger [:key :subkey]}})

(deftest matching-event
  (is (empty? (mixpanel/matching-events [:non-existing] definitions)))
  (is (= 1 (count (mixpanel/matching-events [:key] definitions))))
  (is (= 2 (count (mixpanel/matching-events [:key :subkey] definitions))))
  (is (empty? (mixpanel/matching-events [:key1 :another-subkey] definitions))))
