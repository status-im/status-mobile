(ns status-im.utils.number-test
  (:require [cljs.test :refer-macros [deftest testing are]]
            [status-im.utils.number :as number]))

(deftest format-number-test
  (testing "Positive cases"
    (are [input-number precision expected] (= (number/format-number input-number precision) expected)
      "100"        0 "100"
      "1000"       0 "1k"
      "10000"      0 "10k"
      "11000"      1 "11.0k"
      "11000"      0 "11k"
      "11010"      3 "11.010k"
      "5200000"    3 "5.200m"
      "1000000000" 0 "1b"
      "1110000000" 2 "1.11b"
      "9000000"    0 "9m"))
  (testing "Negative cases"
    (are [input-number precision] (thrown-with-msg? js/Error #"Invalid Number" (number/format-number input-number precision))
      js/undefined 0
      nil          0
      "-1"         0
      ""           0
      js/NaN       0
      "1e3"        0
      "10,2"       0
      "6hello"     0)))
