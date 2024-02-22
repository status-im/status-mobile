(ns status-im.contexts.wallet.common.utils.send-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.common.utils.send :as utils]
            [utils.money :as money]))

(deftest test-calculate-gas-fee
  (testing "Test calculate-gas-fee function with EIP-1559 enabled"
    (let [data-eip1559-enabled            {:gas-amount "23487"
                                           :gas-fees   {:base-fee                 "32.325296406"
                                                        :max-priority-fee-per-gas "0.011000001"
                                                        :eip1559-enabled          true}}
          expected-eip1559-enabled-result (money/bignumber 0.0007594826)]
      (is (money/equal-to (utils/calculate-gas-fee data-eip1559-enabled)
                          expected-eip1559-enabled-result)))

    (testing "Test calculate-gas-fee function with EIP-1559 disabled"
      (let [data-eip1559-disabled            {:gas-amount "23487"
                                              :gas-fees   {:gas-price       "32.375609968"
                                                           :eip1559-enabled false}}
            expected-eip1559-disabled-result (money/bignumber 0.000760406)]
        (is (money/equal-to (utils/calculate-gas-fee data-eip1559-disabled)
                            expected-eip1559-disabled-result))))))
