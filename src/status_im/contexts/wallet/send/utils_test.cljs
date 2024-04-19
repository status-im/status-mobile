(ns status-im.contexts.wallet.send.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.contexts.wallet.send.utils :as utils]
            [utils.money :as money]))

(deftest test-amount-in-hex
  (testing "Test amount-in-hex function"
    (let [amount  1
          decimal 18]
      (is (= (utils/amount-in-hex amount decimal)
             "0xde0b6b3a7640000")))))

(def multichain-transacation
  {:id     61
   :hashes {:5   ["0x5"]
            :420 ["0x12" "0x11"]}})

(deftest test-map-multitransaction-by-ids
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

(deftest test-network-amounts-by-chain
  (testing "Correctly calculates network amounts for transaction with native token"
    (let [route          [{:amount-in "0xde0b6b3a7640000"
                           :to        {:chain-id "1"}}
                          {:amount-in "0xde0b6b3a7640000"
                           :to        {:chain-id "2"}}]
          token-decimals 18
          native-token?  true
          to?            true
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :to?            to?})
          expected       {"1" (money/bignumber "1")
                          "2" (money/bignumber "1")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value)))))

  (testing
    "Correctly calculates network amounts for transaction with native token and multiple routes to same chain-id"
    (let [route          [{:amount-in "0xde0b6b3a7640000"
                           :to        {:chain-id "1"}}
                          {:amount-in "0xde0b6b3a7640000"
                           :to        {:chain-id "1"}}]
          token-decimals 18
          native-token?  true
          to?            true
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :to?            to?})
          expected       {"1" (money/bignumber "2")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value)))))

  (testing "Correctly calculates network amounts for transaction with non-native token"
    (let [route          [{:amount-out "0x1e8480"
                           :from       {:chain-id "1"}}
                          {:amount-out "0x1e8480"
                           :from       {:chain-id "2"}}]
          token-decimals 6
          native-token?  false
          to?            false
          result         (utils/network-amounts-by-chain {:route          route
                                                          :token-decimals token-decimals
                                                          :native-token?  native-token?
                                                          :to?            to?})
          expected       {"1" (money/bignumber "2")
                          "2" (money/bignumber "2")}]
      (doseq [[chain-id exp-value] expected]
        (is (money/equal-to (get result chain-id) exp-value))))))

(deftest test-network-values-for-ui
  (testing "Sanitizes values correctly for display"
    (let [amounts  {"1" (money/bignumber "0")
                    "2" (money/bignumber "2.5")
                    "3" (money/bignumber "0.005")}
          result   (utils/network-values-for-ui amounts)
          expected {"1" "<0.01"
                    "2" (money/bignumber "2.5")
                    "3" (money/bignumber "0.005")}]
      (doseq [[chain-id exp-value] expected]
        (is #(or (= (get result chain-id) exp-value)
                 (money/equal-to (get result chain-id) exp-value)))))))
