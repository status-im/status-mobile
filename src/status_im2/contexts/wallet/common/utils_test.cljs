(ns status-im2.contexts.wallet.common.utils-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im2.contexts.wallet.common.utils :as utils]
            [utils.money :as money]))

(deftest test-calculate-fiat-change
  (is (= (money/bignumber 5) (utils/calculate-fiat-change 105 5)))
  (is (= (money/bignumber -10) (utils/calculate-fiat-change 90 -10))))
