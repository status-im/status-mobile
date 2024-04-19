(ns status-im.contexts.wallet.common.utils.send-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.common.utils.send :as utils]
            [utils.money :as money]))

(deftest test-calculate-gas-fee
  (testing "EIP-1559 transaction without L1 fee"
    (let [data            {:gas-amount "23487"
                           :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                        :eip-1559-enabled       true
                                        :l-1-gas-fee            "0"}}
          expected-result (money/bignumber "53063589834657")] ; This is in Wei
      (is (money/equal-to (utils/calculate-gas-fee data)
                          expected-result))))

  (testing "EIP-1559 transaction with L1 fee of 60,000 Gwei"
    (let [data            {:gas-amount "23487"
                           :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                        :eip-1559-enabled       true
                                        :l-1-gas-fee            "60000"}}
          expected-result (money/bignumber "113063589834657")] ; Added 60,000 Gwei in Wei to the
                                                               ; previous result
      (is (money/equal-to (utils/calculate-gas-fee data)
                          expected-result))))

  (testing "Non-EIP-1559 transaction with specified gas price"
    (let [data            {:gas-amount "23487"
                           :gas-fees   {:gas-price        "2.872721089"
                                        :eip-1559-enabled false
                                        :l-1-gas-fee      "0"}}
          expected-result (money/bignumber "67471600217343")] ; This is in Wei, for the specified
                                                              ; gas amount and price
      (is (money/equal-to (utils/calculate-gas-fee data)
                          expected-result)))))

(deftest test-calculate-full-route-gas-fee
  (testing "Route with a single EIP-1559 transaction, no L1 fees"
    (let [route           [{:gas-amount "23487"
                            :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                         :eip-1559-enabled       true
                                         :l-1-gas-fee            "0"}}]
          expected-result (money/bignumber "0.000053063589834657")] ; The Wei amount for the
                                                                    ; transaction, converted to
                                                                    ; Ether
      (is (money/equal-to (utils/calculate-full-route-gas-fee route)
                          expected-result))))

  (testing "Route with two EIP-1559 transactions, no L1 fees"
    (let [route           [{:gas-amount "23487"
                            :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                         :eip-1559-enabled       true
                                         :l-1-gas-fee            "0"}}
                           {:gas-amount "23487"
                            :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                         :eip-1559-enabled       true
                                         :l-1-gas-fee            "0"}}]
          expected-result (money/bignumber "0.000106127179669314")] ; Sum of both transactions' Wei
                                                                    ; amounts, converted to Ether
      (is (money/equal-to (utils/calculate-full-route-gas-fee route)
                          expected-result))))

  (testing "Route with two EIP-1559 transactions, one with L1 fee of 60,000 Gwei"
    (let [route           [{:gas-amount "23487"
                            :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                         :eip-1559-enabled       true
                                         :l-1-gas-fee            "0"}}
                           {:gas-amount "23487"
                            :gas-fees   {:max-fee-per-gas-medium "2.259274911"
                                         :eip-1559-enabled       true
                                         :l-1-gas-fee            "60000"}}]
          expected-result (money/bignumber "0.000166127179669314")] ; Added 60,000 Gwei in Wei to
                                                                    ; the previous total and
                                                                    ; converted to Ether
      (is (money/equal-to (utils/calculate-full-route-gas-fee route)
                          expected-result)))))

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
