(ns status-im.contexts.wallet.common.utils-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.money :as money]))

(deftest test-get-first-name
  (testing "get-first-name function"
    (is (= (utils/get-first-name "John Doe") "John"))
    (is (= (utils/get-first-name "Jane Smith xyz") "Jane"))))

(deftest test-prettify-balance
  (testing "prettify-balance function"
    (is (= (utils/prettify-balance "$" 100) "$100.00"))
    (is (= (utils/prettify-balance "$" 0.5) "$0.50"))
    (is (= (utils/prettify-balance "$" 0) "$0.00"))
    (is (= (utils/prettify-balance "$" nil) "$0.00"))
    (is (= (utils/prettify-balance "$" "invalid input") "$0.00"))))

(deftest test-get-derivation-path
  (testing "get-derivation-path function"
    (is (= (utils/get-derivation-path 5) "m/44'/60'/0'/0/5"))
    (is (= (utils/get-derivation-path 0) "m/44'/60'/0'/0/0"))
    (is (= (utils/get-derivation-path 123) "m/44'/60'/0'/0/123"))))

(deftest test-format-derivation-path
  (testing "format-derivation-path function"
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/5") "m / 44' / 60' / 0' / 0 / 5"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/0") "m / 44' / 60' / 0' / 0 / 0"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/123") "m / 44' / 60' / 0' / 0 / 123"))))

(deftest test-get-formatted-derivation-path
  (testing "get-formatted-derivation-path function"
    (is (= (utils/get-formatted-derivation-path 5) "m / 44' / 60' / 0' / 0 / 5"))
    (is (= (utils/get-formatted-derivation-path 0) "m / 44' / 60' / 0' / 0 / 0"))
    (is (= (utils/get-formatted-derivation-path 123) "m / 44' / 60' / 0' / 0 / 123"))))

(deftest test-total-raw-balance-in-all-chains
  (testing "total-raw-balance-in-all-chains function"
    (let [balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                              10    {:raw-balance (money/bignumber 200)}
                              42161 {:raw-balance (money/bignumber 300)}}]
      (is (money/equal-to (utils/total-raw-balance-in-all-chains balances-per-chain)
                          (money/bignumber 600))))))

(deftest test-extract-exponent
  (testing "extract-exponent function"
    (is (= (utils/extract-exponent "123.456") nil))
    (is (= (utils/extract-exponent "2.5e-2") "2"))
    (is (= (utils/extract-exponent "4.567e-10") "10"))))

(deftest test-calc-max-crypto-decimals
  (testing "calc-max-crypto-decimals function"
    (is (= (utils/calc-max-crypto-decimals 0.00323) 2))
    (is (= (utils/calc-max-crypto-decimals 0.00123) 3))
    (is (= (utils/calc-max-crypto-decimals 0.00000423) 5))
    (is (= (utils/calc-max-crypto-decimals 2.23e-6) 5))
    (is (= (utils/calc-max-crypto-decimals 1.13e-6) 6))))

(deftest test-get-standard-crypto-format
  (testing "get-standard-crypto-format function"
    (let [market-values-per-currency {:usd {:price 100}}
          token-units                (money/bignumber 0.005)]
      (is (= (utils/get-standard-crypto-format {:market-values-per-currency market-values-per-currency}
                                               token-units)
             "0.005")))
    (let [market-values-per-currency {:usd {:price 0.005}}
          token-units                (money/bignumber 0.01)]
      (is (= (utils/get-standard-crypto-format {:market-values-per-currency market-values-per-currency}
                                               token-units)
             "<2")))))

(deftest test-calculate-total-token-balance
  (testing "calculate-total-token-balance function"
    (let [token {:balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                                      10    {:raw-balance (money/bignumber 200)}
                                      42161 {:raw-balance (money/bignumber 300)}}
                 :decimals           2}]
      (is (money/equal-to (utils/calculate-total-token-balance token) 6.0)))))

(deftest test-get-account-by-address
  (testing "get-account-by-address function"
    (let [accounts        [{:address "0x123"}
                           {:address "0x456"}
                           {:address "0x789"}]
          address-to-find "0x456"]
      (is (= (utils/get-account-by-address accounts address-to-find) {:address "0x456"})))

    (let [accounts        [{:address "0x123"}
                           {:address "0x456"}
                           {:address "0x789"}]
          address-to-find "0x999"]
      (is (= (utils/get-account-by-address accounts address-to-find) nil)))))

(deftest test-get-wallet-qr
  (testing "Test get-wallet-qr function"
    (let [wallet-multichain  {:wallet-type       :multichain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}
          wallet-singlechain {:wallet-type       :singlechain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}]

      (is (= (utils/get-wallet-qr wallet-multichain)
             "eth:opt:x000"))

      (is (= (utils/get-wallet-qr wallet-singlechain)
             "x000")))))

(deftest test-prettify-percentage-change
  (testing "prettify-percentage-change function"
    (is (= (utils/prettify-percentage-change nil) "0.00"))
    (is (= (utils/prettify-percentage-change "") "0.00"))
    (is (= (utils/prettify-percentage-change 0.5) "0.50"))
    (is (= (utils/prettify-percentage-change 1.113454) "1.11"))
    (is (= (utils/prettify-percentage-change -0.35) "0.35"))
    (is (= (utils/prettify-percentage-change -0.78234) "0.78"))))
