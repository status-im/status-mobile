(ns status-im.contexts.wallet.router.router-test
  (:require
    [cljs.test :refer-macros [deftest testing is are]]
    [malli.generator :as malli.generator]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.router.core :as router]
    [status-im.contexts.wallet.router.schema :as router.schema]
    [utils.money :as money]))

;; NOTE: instead of manually creating the map for the route, we can leverage the malli schema
;; to generate a "valid" route with random data
(def default-route
  (malli.generator/generate router.schema/?route {:seed 1}))

(deftest transaction-fees-by-mode-test
  (testing "fails due to schema validation"
    (let [route (update-in default-route
                           [:suggested-levels-for-max-fees-per-gas]
                           dissoc
                           :low                   :medium
                           :high                  :low-estimated-time
                           :medium-estimated-time :high-estimated-time)]
      (is (thrown? js/Error (router/transaction-fees-by-mode route)))))

  (testing "fees and estimated time are correctly grouped by fee mode for EIP-1559"
    (let [route    (-> default-route
                       (assoc-in [:from-chain :eip-1559-enabled] true)
                       (update :suggested-levels-for-max-fees-per-gas
                               assoc
                               :low (money/to-hex 1500000000)
                               :medium (money/to-hex 2000000000)
                               :high (money/to-hex 3456789123)
                               :low-priority (money/to-hex 1500000000)
                               :medium-priority (money/to-hex 2000000000)
                               :high-priority (money/to-hex 3456789123)
                               :low-estimated-time 30
                               :medium-estimated-time 10
                               :high-estimated-time 5))
          expected {:tx-fee-mode/normal {:max-fees       "1.5"
                                         :estimated-time 30}
                    :tx-fee-mode/fast   {:max-fees       "2"
                                         :estimated-time 10}
                    :tx-fee-mode/urgent {:max-fees       "3.456789"
                                         :estimated-time 5}}]
      (is (match? expected (router/transaction-fees-by-mode route)))))

  (testing "fees and estimated time are correctly grouped by fee mode for non-EIP-1559"
    (let [route    (-> default-route
                       (assoc-in [:from-chain :eip-1559-enabled] false)
                       (update :suggested-non-eip-1559-fees
                               assoc
                               :gas-price (money/to-hex 500000000)
                               :estimated-time 5))
          expected {:tx-fee-mode/fast {:max-fees       "0.5"
                                       :estimated-time 5}}]
      (is (match? expected (router/transaction-fees-by-mode route))))))

(deftest transaction-fee-mode-test
  (testing "extracting the appropriate fee mode"
    (are [expected-fee-mode gas-rate]
     (match? expected-fee-mode
             (-> default-route
                 (assoc :tx-gas-fee-mode gas-rate)
                 router/transaction-fee-mode))
     :tx-fee-mode/normal constants/gas-rate-low
     :tx-fee-mode/fast   constants/gas-rate-medium
     :tx-fee-mode/urgent constants/gas-rate-high
     :tx-fee-mode/custom constants/gas-rate-custom)))

(deftest transaction-estimated-time-test
  (testing "returns the estimated time correctly"
    (is (match? 5
                (-> default-route
                    (assoc :tx-estimated-time 5)
                    router/transaction-estimated-time))))

  (testing "if estimated time is 0, falls back to the time estimation based on fee mode"
    (let [route (->
                  default-route
                  (assoc-in [:from-chain :eip-1559-enabled] true)
                  (assoc :tx-estimated-time 0
                         :tx-gas-fee-mode   1)
                  (update-in [:suggested-levels-for-max-fees-per-gas]
                             assoc
                             :low                   (money/to-hex 1500000000)
                             :medium                (money/to-hex 2000000000)
                             :high                  (money/to-hex 3456789123)
                             :low-priority          (money/to-hex 1500000000)
                             :medium-priority       (money/to-hex 2000000000)
                             :high-priority         (money/to-hex 3456789123)
                             :low-estimated-time    30
                             :medium-estimated-time 5
                             :high-estimated-time   5))]
      (is (match? 5
                  (router/transaction-estimated-time route))))))

(deftest approval-estimated-time-test
  (testing "returns the estimated time correctly"
    (is (match? 5
                (-> default-route
                    (assoc :approval-estimated-time 5)
                    router/approval-estimated-time)))))

(deftest transaction-gas-fees-test
  (testing "returns the gas fees for EIP-1559 correctly"
    (let [expected {:gas-price           "0"
                    :eip-1559-enabled    true
                    :base-fee            "0.0005"
                    :tx-priority-fee     "0.0001"
                    :l-1-gas-fee         "0.0002"
                    :tx-max-fees-per-gas "0.08"}]
      (is (match? expected
                  (-> default-route
                      (assoc-in [:from-chain :eip-1559-enabled] true)
                      (assoc :tx-base-fee         (money/to-hex 500000)
                             :tx-priority-fee     (money/to-hex 100000)
                             :tx-l-1-fee          (money/to-hex 200000)
                             :tx-max-fees-per-gas (money/to-hex 80000000)
                             :tx-gas-price        (money/to-hex 0))
                      router/transaction-gas-fees)))))

  (testing "returns the gas fees for non-EIP-1559 correctly"
    (let [expected {:gas-price           "0.5"
                    :eip-1559-enabled    false
                    :base-fee            "0"
                    :tx-priority-fee     "0"
                    :l-1-gas-fee         "0"
                    :tx-max-fees-per-gas "0"}]
      (is (match? expected
                  (-> default-route
                      (assoc-in [:from-chain :eip-1559-enabled] false)
                      (assoc :tx-base-fee         (money/to-hex 0)
                             :tx-priority-fee     (money/to-hex 0)
                             :tx-l-1-fee          (money/to-hex 0)
                             :tx-max-fees-per-gas (money/to-hex 0)
                             :tx-gas-price        (money/to-hex 500000000))
                      router/transaction-gas-fees))))))
