(ns status-im.contexts.wallet.common.utils-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.money :as money]))

(deftest get-first-name-test
  (testing "get-first-name function"
    (is (= (utils/get-first-name "John Doe") "John"))
    (is (= (utils/get-first-name "Jane Smith xyz") "Jane"))))

(deftest prettify-balance-test
  (testing "prettify-balance function"
    (is (= (utils/prettify-balance "$" 100) "$100.00"))
    (is (= (utils/prettify-balance "$" 0.5) "$0.50"))
    (is (= (utils/prettify-balance "$" 0) "$0.00"))
    (is (= (utils/prettify-balance "$" nil) "$0.00"))
    (is (= (utils/prettify-balance "$" "invalid input") "$0.00"))))

(deftest get-derivation-path-test
  (testing "get-derivation-path function"
    (is (= (utils/get-derivation-path 5) "m/44'/60'/0'/0/5"))
    (is (= (utils/get-derivation-path 0) "m/44'/60'/0'/0/0"))
    (is (= (utils/get-derivation-path 123) "m/44'/60'/0'/0/123"))))

(deftest format-derivation-path-test
  (testing "format-derivation-path function"
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/5") "m / 44' / 60' / 0' / 0 / 5"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/0") "m / 44' / 60' / 0' / 0 / 0"))
    (is (= (utils/format-derivation-path "m/44'/60'/0'/0/123") "m / 44' / 60' / 0' / 0 / 123"))))

(deftest get-formatted-derivation-path-test
  (testing "get-formatted-derivation-path function"
    (is (= (utils/get-formatted-derivation-path 5) "m / 44' / 60' / 0' / 0 / 5"))
    (is (= (utils/get-formatted-derivation-path 0) "m / 44' / 60' / 0' / 0 / 0"))
    (is (= (utils/get-formatted-derivation-path 123) "m / 44' / 60' / 0' / 0 / 123"))))

(deftest total-raw-balance-in-all-chains-test
  (testing "total-raw-balance-in-all-chains function"
    (let [balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                              10    {:raw-balance (money/bignumber 200)}
                              42161 {:raw-balance (money/bignumber 300)}}]
      (is (money/equal-to (utils/total-raw-balance-in-all-chains balances-per-chain)
                          (money/bignumber 600))))))

(deftest extract-exponent-test
  (testing "extract-exponent function"
    (is (= (utils/extract-exponent "123.456") nil))
    (is (= (utils/extract-exponent "2.5e-2") "2"))
    (is (= (utils/extract-exponent "4.567e-10") "10"))))

(deftest calc-max-crypto-decimals-test
  (testing "calc-max-crypto-decimals function"
    (is (= (utils/calc-max-crypto-decimals 0.00323) 2))
    (is (= (utils/calc-max-crypto-decimals 0.00123) 3))
    (is (= (utils/calc-max-crypto-decimals 0.00000423) 5))
    (is (= (utils/calc-max-crypto-decimals 2.23e-6) 5))
    (is (= (utils/calc-max-crypto-decimals 1.13e-6) 6))))

(deftest get-standard-crypto-format-test
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

(deftest calculate-total-token-balance-test
  (testing "calculate-total-token-balance function"
    (let [token {:balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                                      10    {:raw-balance (money/bignumber 200)}
                                      42161 {:raw-balance (money/bignumber 300)}}
                 :decimals           2}]
      (is (money/equal-to (utils/calculate-total-token-balance token) 6.0)))))

(deftest get-account-by-address-test
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

(deftest get-wallet-qr-test
  (testing "Test get-wallet-qr function"
    (let [wallet-multichain  {:wallet-type       :multichain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}
          wallet-singlechain {:wallet-type       :singlechain
                              :selected-networks [:ethereum :optimism]
                              :address           "x000"}]

      (is (= (utils/get-wallet-qr wallet-multichain)
             "eth:oeth:x000"))

      (is (= (utils/get-wallet-qr wallet-singlechain)
             "x000")))))

(deftest prettify-percentage-change-test
  (testing "prettify-percentage-change function"
    (is (= (utils/prettify-percentage-change nil) "0.00"))
    (is (= (utils/prettify-percentage-change "") "0.00"))
    (is (= (utils/prettify-percentage-change 0.5) "0.50"))
    (is (= (utils/prettify-percentage-change 1.113454) "1.11"))
    (is (= (utils/prettify-percentage-change -0.35) "0.35"))
    (is (= (utils/prettify-percentage-change -0.78234) "0.78"))))

(deftest calculate-and-sort-tokens-test
  (testing "calculate-and-sort-tokens function"
    (let [mock-color           "blue"
          mock-currency        "USD"
          mock-currency-symbol "$"]

      (with-redefs [utils/calculate-token-value
                    (fn [{:keys [token]}]
                      (case (:symbol token)
                        "ETH" {:token "ETH" :values {:fiat-unformatted-value 5}}
                        "DAI" {:token "DAI" :values {:fiat-unformatted-value 10}}
                        "SNT" {:token "SNT" :values {:fiat-unformatted-value 1}}))]
        (testing "Standard case with different fiat-unformatted-values"
          (let [mock-tokens    [{:symbol             "ETH"
                                 :name               "Ethereum"
                                 :balances-per-chain {:mock-chain 5}
                                 :decimals           18}
                                {:symbol             "DAI"
                                 :name               "Dai"
                                 :balances-per-chain {:mock-chain 10}
                                 :decimals           18}
                                {:symbol             "SNT"
                                 :name               "Status Network Token"
                                 :balances-per-chain {:mock-chain 1}
                                 :decimals           18}]
                mock-input     {:tokens          mock-tokens
                                :color           mock-color
                                :currency        mock-currency
                                :currency-symbol mock-currency-symbol}
                sorted-tokens  (map :token (utils/calculate-and-sort-tokens mock-input))
                expected-order ["DAI" "ETH" "SNT"]]
            (is (= expected-order sorted-tokens))))))))

(deftest sort-tokens-test
  (testing "sort-tokens function"
    (let [mock-tokens    [{:symbol "ETH" :balance 5}
                          {:symbol "DAI" :balance 10}
                          {:symbol "SNT" :balance 1}]
          sorted-tokens  (map :symbol (utils/sort-tokens mock-tokens))
          expected-order ["DAI" "ETH" "SNT"]]
      (is (= expected-order sorted-tokens)))))



