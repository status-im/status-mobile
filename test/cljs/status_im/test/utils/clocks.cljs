(ns status-im.test.utils.clocks
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.clocks :as clocks]))

(deftest safe-timestamp
  (testing "it caps the timestamp when a value too large is provided"
    (is (< (clocks/receive js/Number.MAX_SAFE_INTEGER 0)
           js/Number.MAX_SAFE_INTEGER))))

(deftest safe-timestamp?-test
  (testing "it returns false for a high number"
    (is (not (clocks/safe-timestamp? js/Number.MAX_SAFE_INTEGER))))
  (testing "it returns true for a normal timestamp number"
    (is (clocks/safe-timestamp? (clocks/send 0)))))
