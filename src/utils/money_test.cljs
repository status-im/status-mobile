(ns utils.money-test
  (:require [cljs.test :refer-macros [deftest is are]]
            [utils.money :as money]))

(deftest normalize
  (is (= nil (money/normalize nil)))
  (is (= "1" (money/normalize "  1 ")))
  (is (= "1.1" (money/normalize "1.1")))
  (is (= "1.1" (money/normalize "1,1"))))

(deftest format-amount
  (are [amount expected]
   (= expected (money/format-amount amount))
   nil       nil
   0         "0"
   1         "1"
   10        "10"
   100       "100"
   999       "999"
   1000      "1K"
   1001      "1K"
   10000     "10K"
   100000    "100K"
   999999    "1000K"
   1000000   "1M"
   1000000   "1M"
   1000001   "1M"
   10000000  "10M"
   100000000 "100M"))
