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

(deftest test-find-affordable-networks
  (testing "All networks affordable and selected, none disabled"
    (let [balances-per-chain {"1" {:balance "50.0" :chain-id "1"}
                              "2" {:balance "40.0" :chain-id "2"}}
          input-value        20
          selected-networks  ["1" "2"]
          disabled-chain-ids []
          expected           ["1" "2"]]
      (is (= (set (utils/find-affordable-networks {:balances-per-chain balances-per-chain
                                                   :input-value        input-value
                                                   :selected-networks  selected-networks
                                                   :disabled-chain-ids disabled-chain-ids}))
             (set expected)))))

  (testing "No networks affordable"
    (let [balances-per-chain {"1" {:balance "5.0" :chain-id "1"}
                              "2" {:balance "1.0" :chain-id "2"}}
          input-value        10
          selected-networks  ["1" "2"]
          disabled-chain-ids []
          expected           []]
      (is (= (set (utils/find-affordable-networks {:balances-per-chain balances-per-chain
                                                   :input-value        input-value
                                                   :selected-networks  selected-networks
                                                   :disabled-chain-ids disabled-chain-ids}))
             (set expected)))))

  (testing "Selected networks subset, with some disabled"
    (let [balances-per-chain {"1" {:balance "100.0" :chain-id "1"}
                              "2" {:balance "50.0" :chain-id "2"}
                              "3" {:balance "20.0" :chain-id "3"}}
          input-value        15
          selected-networks  ["1" "2" "3"]
          disabled-chain-ids ["2"]
          expected           ["1" "3"]]
      (is (= (set (utils/find-affordable-networks {:balances-per-chain balances-per-chain
                                                   :input-value        input-value
                                                   :selected-networks  selected-networks
                                                   :disabled-chain-ids disabled-chain-ids}))
             (set expected))))))
