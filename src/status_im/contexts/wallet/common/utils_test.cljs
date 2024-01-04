(ns status-im.contexts.wallet.common.utils-test
  (:require [cljs.test :refer [deftest is testing]]
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
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/5") "m/44'/60'/0'/0/5"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/0") "m/44'/60'/0'/0/0"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/123") "m/44'/60'/0'/0/123"))))

(deftest test-get-formatted-derivation-path
  (testing "get-formatted-derivation-path function"
    (is (= (utils/get-formatted-derivation-path 5) "m/44'/60'/0'/0/5"))
    (is (= (utils/get-formatted-derivation-path 0) "m/44'/60'/0'/0/0"))
    (is (= (utils/get-formatted-derivation-path 123) "m/44'/60'/0'/0/123"))))

(deftest test-total-raw-balance-in-all-chains
  (testing "total-raw-balance-in-all-chains function"
    (let [balances-per-chain {1 {:raw-balance (money/bignumber 1000000000000)}
                              10 {:raw-balance (money/bignumber 2645130235566666)}
                              42161 {:raw-balance (money/bignumber 900000000000000)}}
          expected-result (money/bignumber 3546130235566666)]
      (is (= (utils/total-raw-balance-in-all-chains balances-per-chain) expected-result)))))

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
          token-units                0.005]
      (is (= (utils/get-standard-crypto-format {:market-values-per-currency market-values-per-currency}
                                               token-units)
             "<0.01"))))
  (let [market-values-per-currency {:usd {:price 0.005}}
        token-units                0.01]
    (is (= (utils/get-standard-crypto-format {:market-values-per-currency market-values-per-currency}
                                             token-units)
           "2.00"))))

(deftest test-total-token-units-in-all-chains
  (testing "total-token-units-in-all-chains function"
    (let [token {:balances-per-chain [{:raw-balance 100} {:raw-balance 200} {:raw-balance 300}]
                 :decimals           2}]
      (is (= (utils/total-token-units-in-all-chains token) 6.0)))
    (let [token {:balances-per-chain [{:raw-balance 0} {:raw-balance 0} {:raw-balance 0}]
                 :decimals           3}]
      (is (= (utils/total-token-units-in-all-chains token) 0.0)))))

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

(deftest test-calculate-raw-balance
  (testing "calculate-raw-balance function"
    (is (= (utils/calculate-raw-balance "100000000" "8") 1.0))
    (is (= (utils/calculate-raw-balance "50000000" "8") 0.5))
    (is (= (utils/calculate-raw-balance "123456789" "2") 1234567.89))
    (is (= (utils/calculate-raw-balance "0" "4") 0.0))))

(deftest test-token-value-in-chain
  (testing "token-value-in-chain function"
    (let [token {:balances-per-chain [{:raw-balance "100000000" :chain-id 1}
                                      {:raw-balance "50000000" :chain-id 2}
                                      {:raw-balance "123456789" :chain-id 3}]
                 :decimals           "8"}]
      (is (= (utils/token-value-in-chain token 1) 1.0)))

    (let [token {:balances-per-chain [{:raw-balance "100000000" :chain-id 1}
                                      {:raw-balance "50000000" :chain-id 2}
                                      {:raw-balance "123456789" :chain-id 3}]
                 :decimals           "8"}]
      (is (= (utils/token-value-in-chain token 2) 0.5)))

    (let [token {:balances-per-chain [{:raw-balance "100000000" :chain-id 1}
                                      {:raw-balance "50000000" :chain-id 2}
                                      {:raw-balance "123456789" :chain-id 3}]
                 :decimals           "8"}]
      (is (= (utils/token-value-in-chain token 3) 1234567.89)))

    (let [token {:balances-per-chain [{:raw-balance "0" :chain-id 1}
                                      {:raw-balance "0" :chain-id 2}
                                      {:raw-balance "0" :chain-id 3}]
                 :decimals           "4"}]
      (is (= (utils/token-value-in-chain token 1) 0.0)))))

(deftest test-calculate-balance-for-account
  (testing "calculate-balance-for-account function"
    (let [account  {:tokens [{:balances-per-chain [{:raw-balance "100000000" :chain-id 1}
                                                   {:raw-balance "50000000" :chain-id 2}
                                                   {:raw-balance "123456789" :chain-id 3}]
                              :decimals           "8"}
                             {:balances-per-chain [{:raw-balance "200000000" :chain-id 1}
                                                   {:raw-balance "100000000" :chain-id 2}
                                                   {:raw-balance "987654321" :chain-id 3}]
                              :decimals           "8"}]}
          currency {:market-values-per-currency {:usd {:price 1}}}]
      (is (= (utils/calculate-balance-for-account currency account) 9876543.21)))

    (let [account  {:tokens [{:balances-per-chain [{:raw-balance "0" :chain-id 1}
                                                   {:raw-balance "0" :chain-id 2}
                                                   {:raw-balance "0" :chain-id 3}]
                              :decimals           "4"}
                             {:balances-per-chain [{:raw-balance "0" :chain-id 1}
                                                   {:raw-balance "0" :chain-id 2}
                                                   {:raw-balance "0" :chain-id 3}]
                              :decimals           "4"}]}
          currency {:market-values-per-currency {:usd {:price 1}}}]
      (is (= (utils/calculate-balance-for-account currency account) 0.0)))))

(deftest test-calculate-balance-for-token
  (testing "calculate-balance-for-token function"
    (let [token {:balances-per-chain         [{:raw-balance "100000000" :chain-id 1}
                                              {:raw-balance "50000000" :chain-id 2}
                                              {:raw-balance "123456789" :chain-id 3}]
                 :decimals                   "8"
                 :market-values-per-currency {:usd {:price 1}}}]
      (is (= (utils/calculate-balance-for-token token) 2234567.89)))

    (let [token {:balances-per-chain         [{:raw-balance "0" :chain-id 1}
                                              {:raw-balance "0" :chain-id 2}
                                              {:raw-balance "0" :chain-id 3}]
                 :decimals                   "4"
                 :market-values-per-currency {:usd {:price 1}}}]
      (is (= (utils/calculate-balance-for-token token) 0.0)))))

(deftest test-calculate-balance
  (testing "test calculate-balance function"
    (let [tokens-in-account [{:balances-per-chain         [{:raw-balance "100000000" :chain-id 1}
                                                           {:raw-balance "50000000" :chain-id 2}
                                                           {:raw-balance "123456789" :chain-id 3}]
                              :decimals                   "8"
                              :market-values-per-currency {:usd {:price 1}}}
                             {:balances-per-chain         [{:raw-balance "200000000" :chain-id 1}
                                                           {:raw-balance "100000000" :chain-id 2}
                                                           {:raw-balance "987654321" :chain-id 3}]
                              :decimals                   "8"
                              :market-values-per-currency {:usd {:price 1}}}]]
      (is (= (utils/calculate-balance tokens-in-account) 10234567.89)))

    (let [tokens-in-account [{:balances-per-chain         [{:raw-balance "0" :chain-id 1}
                                                           {:raw-balance "0" :chain-id 2}
                                                           {:raw-balance "0" :chain-id 3}]
                              :decimals                   "4"
                              :market-values-per-currency {:usd {:price 1}}}
                             {:balances-per-chain         [{:raw-balance "0" :chain-id 1}
                                                           {:raw-balance "0" :chain-id 2}
                                                           {:raw-balance "0" :chain-id 3}]
                              :decimals                   "4"
                              :market-values-per-currency {:usd {:price 1}}}]]
      (is (= (utils/calculate-balance tokens-in-account) 0.0)))))

(deftest test-network-list
  (testing "network-list function"
    (let [balances-per-chain {:chain-1 {:balance 100}
                              :chain-2 {:balance 200}
                              :chain-3 {:balance 300}}
          networks           [{:chain-id :chain-1 :name "Network A"}
                              {:chain-id :chain-2 :name "Network B"}
                              {:chain-id :chain-3 :name "Network C"}]]
      (is (= (utils/network-list {:balances-per-chain balances-per-chain} networks)
             #{:chain-1 :chain-2 :chain-3})))

    (let [balances-per-chain {:chain-1 {:balance 100}
                              :chain-2 {:balance 200}
                              :chain-3 {:balance 300}}
          networks           [{:chain-id 1 :name "Network A"}
                              {:chain-id 3 :name "Network C"}]]
      (is (= (utils/network-list {:balances-per-chain balances-per-chain} networks)
             #{1 3})))))

(deftest test-calculate-fiat-change
  (testing "calculate-fiat-change function"
    (is (= (utils/calculate-fiat-change 100 10) 10))
    (is (= (utils/calculate-fiat-change 50 10) 5))
    (is (= (utils/calculate-fiat-change 85 -15) -15))))

(deftest test-get-wallet-qr
  (testing "Test get-wallet-qr function"
    (let [wallet-multichain  {:wallet-type       :wallet-multichain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}
          wallet-singlechain {:wallet-type       :wallet-singlechain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}]

      (is (= (utils/get-wallet-qr wallet-multichain)
             "eth:opt:x000"))

      (is (= (utils/get-wallet-qr wallet-singlechain)
             "x000")))))
