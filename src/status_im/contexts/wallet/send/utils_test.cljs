(ns status-im.contexts.wallet.send.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.send.utils :as utils]
            [utils.map :as map]
            [utils.money :as money]))

(deftest amount-in-hex-test
  (testing "Test amount-in-hex function"
    (let [amount  1
          decimal 18]
      (is (= (utils/amount-in-hex amount decimal)
             "0xde0b6b3a7640000")))))

(def multichain-transacation
  {:id     61
   :hashes {:5   ["0x5"]
            :420 ["0x12" "0x11"]}})

(deftest map-multitransaction-by-ids-test
  (testing "test map-multitransaction-by-ids formats to right data structure"
    (let [{:keys [id hashes]} multichain-transacation]
      (is (= (utils/map-multitransaction-by-ids id hashes)
             {"0x5"  {:status   :pending
                      :id       61
                      :chain-id :5}
              "0x12" {:status   :pending
                      :id       61
                      :chain-id :420}
              "0x11" {:status   :pending
                      :id       61
                      :chain-id :420}})))))

(deftest network-amounts-by-chain-test
  (testing "Correctly calculates network amounts for transaction with native token"
    (let [route          [{:amount-out "0xde0b6b3a7640000"
                           :to         {:chain-id 1}}
                          {:amount-out "0xde0b6b3a7640000"
                           :to         {:chain-id 10}}]
          token-decimals 18
          native-token?  true
          receiver?      true
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :receiver?      receiver?})
          expected       {1  (money/bignumber "1")
                          10 (money/bignumber "1")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value)))))

  (testing
    "Correctly calculates network amounts for transaction with native token and multiple routes to same chain-id"
    (let [route          [{:amount-out "0xde0b6b3a7640000"
                           :to         {:chain-id 1}}
                          {:amount-out "0xde0b6b3a7640000"
                           :to         {:chain-id 1}}]
          token-decimals 18
          native-token?  true
          receiver?      true
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :receiver?      receiver?})
          expected       {1 (money/bignumber "2")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value)))))

  (testing "Correctly calculates network amounts for transaction with non-native token"
    (let [route          [{:amount-in "0x1e8480"
                           :from      {:chain-id 1}}
                          {:amount-in "0x1e8480"
                           :from      {:chain-id 10}}]
          token-decimals 6
          native-token?  false
          receiver?      false
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :receiver?      receiver?})
          expected       {1  (money/bignumber "2")
                          10 (money/bignumber "2")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value))))))

(deftest estimated-received-by-chain-test
  (testing "Correctly calculates the estimated received amount with native token"
    (let [chain-id       1
          route          [{:estimated-received "0x1bc16d674ec80000"
                           :to                 {:chain-id chain-id}}]
          token-decimals 18
          native-token?  true
          result         (utils/estimated-received-by-chain {:route          route
                                                             :token-decimals token-decimals
                                                             :native-token?  native-token?})
          expected       (money/bignumber "2")]
      (is (money/equal-to (get result chain-id) expected))))

  (testing "Correctly calculates the estimated received amount with non-native token"
    (let [chain-id       10
          route          [{:estimated-received "0x1bc16d674ec80000"
                           :to                 {:chain-id chain-id}}]
          token-decimals 18
          native-token?  false
          result         (utils/estimated-received-by-chain {:route          route
                                                             :token-decimals token-decimals
                                                             :native-token?  native-token?})
          expected       (money/bignumber "2")]
      (is (money/equal-to (get result chain-id) expected))))

  (testing
    "Correctly calculates the estimated received amount with multiple routes on different networks"
    (let [route          [{:estimated-received "0x1bc16d674ec80000"
                           :to                 {:chain-id 1}}
                          {:estimated-received "0xde0b6b3a7640000"
                           :to                 {:chain-id 10}}]
          token-decimals 18
          native-token?  false
          result         (utils/estimated-received-by-chain {:route          route
                                                             :token-decimals token-decimals
                                                             :native-token?  native-token?})
          expected       {10 (money/bignumber "1")
                          1  (money/bignumber "2")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value)))))

  (testing "Correctly calculates the estimated received amount with multiple routes on the same network"
    (let [chain-id       10
          route          [{:estimated-received "0x1bc16d674ec80000"
                           :to                 {:chain-id chain-id}}
                          {:estimated-received "0x1bc16d674ec80000"
                           :to                 {:chain-id chain-id}}]
          token-decimals 18
          native-token?  false
          result         (utils/estimated-received-by-chain {:route          route
                                                             :token-decimals token-decimals
                                                             :native-token?  native-token?})
          expected       (money/bignumber "4")]
      (is (money/equal-to (get result chain-id) expected)))))

(deftest network-values-for-ui-test
  (testing "Sanitizes values correctly for display"
    (let [amounts  {1     (money/bignumber "0")
                    10    (money/bignumber "2.5")
                    42161 (money/bignumber "0.005")}
          result   (utils/network-values-for-ui amounts)
          expected {1     "<0.01"
                    10    (money/bignumber "2.5")
                    42161 (money/bignumber "0.005")}]
      (doseq [[chain-id exp-value] expected]
        (is #(or (= (get result chain-id) exp-value)
                 (money/equal-to (get result chain-id) exp-value)))))))

(deftest calculate-gas-fee-test
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

(deftest calculate-full-route-gas-fee-test
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

(deftest token-available-networks-for-suggested-routes-test
  (testing "Excludes disabled chain-ids correctly"
    (let [balances-per-chain {1     {:chain-id 1 :balance 100}
                              10    {:chain-id 10 :balance 200}
                              42161 {:chain-id 42161 :balance 300}}
          disabled-chain-ids [10]
          expected           [1 42161]]
      (is (= expected
             (utils/token-available-networks-for-suggested-routes {:balances-per-chain balances-per-chain
                                                                   :disabled-chain-ids
                                                                   disabled-chain-ids})))))

  (testing "Returns all chains when no disabled chains are specified"
    (let [balances-per-chain {1     {:chain-id 1 :balance 100}
                              10    {:chain-id 10 :balance 200}
                              42161 {:chain-id 42161 :balance 300}}
          disabled-chain-ids []
          expected           [1 10 42161]]
      (is (= expected
             (utils/token-available-networks-for-suggested-routes {:balances-per-chain balances-per-chain
                                                                   :disabled-chain-ids
                                                                   disabled-chain-ids})))))

  (testing "Returns empty list when all chains are disabled"
    (let [balances-per-chain {1     {:chain-id 1 :balance 100}
                              10    {:chain-id 10 :balance 200}
                              42161 {:chain-id 42161 :balance 300}}
          disabled-chain-ids [1 10 42161]
          expected           []]
      (is (= expected
             (utils/token-available-networks-for-suggested-routes {:balances-per-chain balances-per-chain
                                                                   :disabled-chain-ids
                                                                   disabled-chain-ids})))))

  (testing "Handles non-existent chain-ids gracefully"
    (let [balances-per-chain {59144 {:chain-id 59144 :balance 400}}
          disabled-chain-ids [1 10 42161]
          expected           [59144]]
      (is (= expected
             (utils/token-available-networks-for-suggested-routes {:balances-per-chain balances-per-chain
                                                                   :disabled-chain-ids
                                                                   disabled-chain-ids}))))))

(deftest reset-loading-network-amounts-to-zero-test
  (testing "Correctly resets loading network amounts to zero and changes type to default"
    (let [network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}
                           {:chain-id 10 :total-amount (money/bignumber "200") :type :default}]
          expected        [{:chain-id 1 :total-amount (money/bignumber "0") :type :default}
                           {:chain-id 10 :total-amount (money/bignumber "200") :type :default}]
          result          (utils/reset-loading-network-amounts-to-zero network-amounts)
          comparisons     (map #(map/deep-compare %1 %2)
                               expected
                               result)]
      (is (every? identity comparisons))))

  (testing "Leaves non-loading types unchanged"
    (let [network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :default}
                           {:chain-id 10 :total-amount (money/bignumber "0") :type :disabled}]
          expected        [{:chain-id 1 :total-amount (money/bignumber "100") :type :default}
                           {:chain-id 10 :total-amount (money/bignumber "0") :type :disabled}]
          result          (utils/reset-loading-network-amounts-to-zero network-amounts)
          comparisons     (map #(map/deep-compare %1 %2)
                               expected
                               result)]
      (is (every? identity comparisons))))

  (testing "Processes an empty list without error"
    (let [network-amounts []
          expected        []
          result          (utils/reset-loading-network-amounts-to-zero network-amounts)
          comparisons     (map #(map/deep-compare %1 %2)
                               expected
                               result)]
      (is (every? identity comparisons))))

  (testing "Applies transformations to multiple loading entries"
    (let [network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}
                           {:chain-id 10 :total-amount (money/bignumber "200") :type :loading}]
          expected        [{:chain-id 1 :total-amount (money/bignumber "0") :type :default}
                           {:chain-id 10 :total-amount (money/bignumber "0") :type :default}]
          result          (utils/reset-loading-network-amounts-to-zero network-amounts)
          comparisons     (map #(map/deep-compare %1 %2)
                               expected
                               result)]
      (is (every? identity comparisons))))

  (testing "Mix of loading and non-loading types"
    (let [network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}
                           {:chain-id 10 :total-amount (money/bignumber "200") :type :default}
                           {:chain-id 42161 :total-amount (money/bignumber "300") :type :loading}
                           {:chain-id 59144 :total-amount (money/bignumber "0") :type :disabled}]
          expected        [{:chain-id 1 :total-amount (money/bignumber "0") :type :default}
                           {:chain-id 10 :total-amount (money/bignumber "200") :type :default}
                           {:chain-id 42161 :total-amount (money/bignumber "0") :type :default}
                           {:chain-id 59144 :total-amount (money/bignumber "0") :type :disabled}]
          result          (utils/reset-loading-network-amounts-to-zero network-amounts)
          comparisons     (map #(map/deep-compare %1 %2)
                               expected
                               result)]
      (is (every? identity comparisons)))))

(deftest network-amounts-test
  (testing "Handles disabled and receiver networks correctly when receiver? is true"
    (let [network-values     {10 (money/bignumber "200")}
          disabled-chain-ids [1]
          receiver-networks  [10]
          token-networks-ids [1 10 42161]
          receiver?          true
          expected           [{:chain-id     10
                               :total-amount (money/bignumber "200")
                               :type         :default}
                              {:type :edit}]
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing "Adds default amount for non-disabled non-receiver networks when receiver? is false"
    (let [network-values     {1 (money/bignumber "100")}
          disabled-chain-ids [10]
          receiver-networks  []
          token-networks-ids [1 10 42161]
          receiver?          false
          expected           [{:chain-id     1
                               :total-amount (money/bignumber "100")
                               :type         :default}
                              {:chain-id     10
                               :total-amount (money/bignumber "0")
                               :type         :disabled}]
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing "Handles empty inputs correctly"
    (let [network-values     {}
          disabled-chain-ids []
          receiver-networks  []
          token-networks-ids []
          receiver?          true
          expected           []
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (= expected result))))

  (testing "Processes case with multiple network interactions"
    (let [network-values     {1     (money/bignumber "300")
                              10    (money/bignumber "400")
                              42161 (money/bignumber "500")}
          disabled-chain-ids [1 42161]
          receiver-networks  [10]
          token-networks-ids [1 10 42161]
          receiver?          true
          expected           [{:chain-id     1
                               :total-amount (money/bignumber "300")
                               :type         :default}
                              {:chain-id     10
                               :total-amount (money/bignumber "400")
                               :type         :default}
                              {:chain-id     42161
                               :total-amount (money/bignumber "500")
                               :type         :default}
                              {:type :edit}]
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing "Does not assign :not-available type when receiver? is false"
    (let [network-values     {1 (money/bignumber "100")}
          disabled-chain-ids [10]
          receiver-networks  [1]
          token-networks-ids [1 10]
          receiver?          false
          expected           [{:chain-id     1
                               :total-amount (money/bignumber "100")
                               :type         :default}
                              {:chain-id     10
                               :total-amount (money/bignumber "0")
                               :type         :disabled}]
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing
    "Assigns :not-available type to networks not available in token-networks-ids when receiver? is true"
    (let [network-values     {1 (money/bignumber "100")}
          disabled-chain-ids []
          receiver-networks  [1 10]
          token-networks-ids [1]
          receiver?          false
          expected           [{:chain-id     1
                               :total-amount (money/bignumber "100")
                               :type         :default}
                              {:chain-id     10
                               :total-amount nil
                               :type         :not-available}]
          tx-type            :tx/send
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing
    "Handles disabled and receiver networks correctly when receiver? is false and tx-type is :tx/bridge"
    (let [network-values     {10 (money/bignumber "200")}
          disabled-chain-ids [1]
          receiver-networks  [10]
          token-networks-ids [1 10]
          tx-type            :tx/bridge
          receiver?          false
          expected           [{:chain-id     1
                               :total-amount (money/bignumber "0")
                               :type         :disabled}
                              {:chain-id     10
                               :total-amount (money/bignumber "200")
                               :type         :default}]
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result)))))

  (testing
    "Handles disabled and receiver networks correctly when receiver? is true and tx-type is :tx/bridge"
    (let [network-values     {10 (money/bignumber "200")}
          disabled-chain-ids [1]
          receiver-networks  [10]
          token-networks-ids [1 10]
          tx-type            :tx/bridge
          receiver?          true
          expected           [{:chain-id     10
                               :total-amount (money/bignumber "200")
                               :type         :default}]
          result             (utils/network-amounts {:network-values     network-values
                                                     :disabled-chain-ids disabled-chain-ids
                                                     :receiver-networks  receiver-networks
                                                     :token-networks-ids token-networks-ids
                                                     :tx-type            tx-type
                                                     :receiver?          receiver?})]
      (is (every? identity (map #(map/deep-compare %1 %2) expected result))))))

(deftest loading-network-amounts-test
  (testing "Assigns :loading type to valid networks except for disabled ones"
    (let [valid-networks     [1 10 42161]
          disabled-chain-ids [42161]
          receiver-networks  [1 10]
          token-networks-ids [1 10 42161]
          receiver?          true
          expected           [{:chain-id 1 :type :loading}
                              {:chain-id 10 :type :loading}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing "Assigns :disabled type with zero total-amount to disabled networks when receiver? is false"
    (let [valid-networks     [1 10 42161]
          disabled-chain-ids [10 42161]
          receiver-networks  [1]
          token-networks-ids [1 10 42161]
          receiver?          false
          expected           [{:chain-id 1 :type :loading}
                              {:chain-id 10 :type :disabled :total-amount (money/bignumber "0")}
                              {:chain-id 42161 :type :disabled :total-amount (money/bignumber "0")}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing "Filters out networks not in receiver networks when receiver? is true"
    (let [valid-networks     [1 10 42161 59144]
          disabled-chain-ids [10]
          receiver-networks  [1 42161]
          token-networks-ids [1 10 42161]
          receiver?          true
          expected           [{:chain-id 1 :type :loading}
                              {:chain-id 42161 :type :loading}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing
    "Appends :edit type if receiver network count is less than available networks and receiver? is true"
    (let [valid-networks     [1 10 42161]
          disabled-chain-ids [10]
          receiver-networks  [1]
          token-networks-ids [1 10 42161]
          receiver?          true
          expected           [{:chain-id 1 :type :loading}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing
    "Assigns :not-available type to networks not available in token-networks-ids when receiver? is false"
    (let [valid-networks     [42161]
          disabled-chain-ids []
          receiver-networks  [1]
          token-networks-ids [42161]
          receiver?          false
          expected           [{:chain-id 42161 :type :loading}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing
    "Assigns :not-available type to networks not available in token-networks-ids when receiver? is true"
    (let [valid-networks     [42161]
          disabled-chain-ids []
          receiver-networks  [1]
          token-networks-ids [42161]
          receiver?          true
          expected           [{:chain-id 1 :type :not-available}]
          tx-type            :tx/send
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing
    "Assigns :loading type to valid networks and :disabled for disabled ones when tx-type is :tx/bridge and receiver? false"
    (let [valid-networks     [1 10]
          disabled-chain-ids [10]
          receiver-networks  []
          token-networks-ids [1 10]
          tx-type            :tx/bridge
          receiver?          false
          expected           [{:chain-id 1 :type :loading}
                              {:chain-id 10 :type :disabled :total-amount (money/bignumber "0")}]
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons))))

  (testing
    "Assigns :loading type to valid networks, ignore disabled ones and do not add {:type :edit} when tx-type is :tx/bridge and receiver? true"
    (let [valid-networks     [1]
          disabled-chain-ids [10]
          receiver-networks  []
          token-networks-ids [1 10]
          tx-type            :tx/bridge
          receiver?          true
          expected           [{:chain-id 1 :type :loading}]
          result             (utils/loading-network-amounts {:valid-networks     valid-networks
                                                             :disabled-chain-ids disabled-chain-ids
                                                             :receiver-networks  receiver-networks
                                                             :token-networks-ids token-networks-ids
                                                             :tx-type            tx-type
                                                             :receiver?          receiver?})
          comparisons        (map #(map/deep-compare %1 %2)
                                  expected
                                  result)]
      (is (every? identity comparisons)))))

(deftest network-links-test
  (testing "Calculates position differences correctly"
    (let [route                [{:from {:chain-id 1} :to {:chain-id 42161}}
                                {:from {:chain-id 10} :to {:chain-id 1}}
                                {:from {:chain-id 42161} :to {:chain-id 10}}]
          from-values-by-chain [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
          to-values-by-chain   [{:chain-id 42161} {:chain-id 1} {:chain-id 10}]
          expected             [{:from-chain-id 1 :to-chain-id 42161 :position-diff 0}
                                {:from-chain-id 10 :to-chain-id 1 :position-diff 0}
                                {:from-chain-id 42161 :to-chain-id 10 :position-diff 0}]
          result               (utils/network-links route from-values-by-chain to-values-by-chain)]
      (is (= expected result))))

  (testing "Handles cases with no position difference"
    (let [route                [{:from {:chain-id 1} :to {:chain-id 1}}]
          from-values-by-chain [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
          to-values-by-chain   [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
          expected             [{:from-chain-id 1 :to-chain-id 1 :position-diff 0}]
          result               (utils/network-links route from-values-by-chain to-values-by-chain)]
      (is (= expected result))))

  (testing "Handles empty route"
    (let [route                []
          from-values-by-chain []
          to-values-by-chain   []
          expected             []
          result               (utils/network-links route from-values-by-chain to-values-by-chain)]
      (is (= expected result))))

  (testing "Verifies negative position differences"
    (let [route                [{:from {:chain-id 1} :to {:chain-id 42161}}]
          from-values-by-chain [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
          to-values-by-chain   [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
          expected             [{:from-chain-id 1 :to-chain-id 42161 :position-diff -2}]
          result               (utils/network-links route from-values-by-chain to-values-by-chain)]
      (is (= expected result)))))
