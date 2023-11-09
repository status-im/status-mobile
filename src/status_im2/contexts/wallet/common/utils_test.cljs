(ns status-im2.contexts.wallet.common.utils-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im2.contexts.wallet.common.utils :as utils]))

(deftest test-calculate-raw-balance
  (is (= 500 (utils/calculate-raw-balance "50000" 2)))
  (is (= 0 (utils/calculate-raw-balance "abc" 2)))
  (is (= 0 (utils/calculate-raw-balance "50000" "abc"))))

(deftest test-calculate-fiat-change
  (is (= 5 (utils/calculate-fiat-change 105 5)))
  (is (= -10 (utils/calculate-fiat-change 90 -10))))
