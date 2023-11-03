(ns status-im2.subs.wallet.wallet-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            status-im2.subs.root
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(def tokens
  {:0x1 [{:decimals                1
          :symbol                  "ETH"
          :name                    "Ether"
          :balancesPerChain        {:1 {:rawBalance "20"
                                        :hasError   false}
                                    :2 {:rawBalance "10"
                                        :hasError   false}}
          :marketValuesPerCurrency {:USD {:price 1000}}} ;; total should be 3000
         {:decimals                2
          :symbol                  "DAI"
          :name                    "Dai Stablecoin"
          :balancesPerChain        {:1 {:rawBalance "100"
                                        :hasError   false}
                                    :2 {:rawBalance "150"
                                        :hasError   false}}
          :marketValuesPerCurrency {:USD {:price 100}}}] ;; total should be 250
   :0x2 [{:decimals                3
          :symbol                  "ETH"
          :name                    "Ether"
          :balancesPerChain        {:1 {:rawBalance "2500"
                                        :hasError   false}
                                    :2 {:rawBalance "3000"
                                        :hasError   false}
                                    :3 {:rawBalance "<nil>"
                                        :hasError   false}}
          :marketValuesPerCurrency {:USD {:price 200}}} ;; total should be 1100
         {:decimals                10
          :symbol                  "DAI"
          :name                    "Dai Stablecoin"
          :balancesPerChain        {:1 {:rawBalance "10000000000"
                                        :hasError   false}
                                    :2 {:rawBalance "0"
                                        :hasError   false}
                                    :3 {:rawBalance "<nil>"
                                        :hasError   false}}
          :marketValuesPerCurrency {:USD {:price 1000}}}]}) ;; total should be 1000

(def accounts
  {"0x1" {:path                     "m/44'/60'/0'/0/0"
          :emoji                    "😃"
          :key-uid                  "0x2f5ea39"
          :address                  "0x1"
          :wallet                   false
          :name                     "Account One"
          :type                     :generated
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :customization-color      :blue
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 0
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 "fully"
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x04371e2d9d66b82f056bc128064"
          :removed                  false}
   "0x2" {:path                     "m/44'/60'/0'/0/1"
          :emoji                    "💎"
          :key-uid                  "0x2f5ea39"
          :address                  "0x2"
          :wallet                   false
          :name                     "Account Two"
          :type                     :generated
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :customization-color      :purple
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 1
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 "fully"
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x04371e2d9d66b82f056bc128064"
          :removed                  false}})

(h/deftest-sub :wallet/balances
  [sub-name]
  (testing "returns seq of maps containing :address and :balance"
    (swap! rf-db/app-db #(-> %
                             (assoc-in [:wallet :accounts] accounts)
                             (assoc :wallet/tokens tokens)))
    (is (= `({:address "0x1"
              :balance 3250}
             {:address "0x2"
              :balance 2100})
           (rf/sub [sub-name])))))

(h/deftest-sub :wallet/accounts
  [sub-name]
  (testing "returns all accounts without balance"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc :wallet/tokens tokens)))
    (is
     (= `({:path                     "m/44'/60'/0'/0/0"
           :emoji                    "😃"
           :key-uid                  "0x2f5ea39"
           :address                  "0x1"
           :wallet                   false
           :name                     "Account One"
           :type                     :generated
           :chat                     false
           :test-preferred-chain-ids #{5 420 421613}
           :customization-color      :blue
           :hidden                   false
           :prod-preferred-chain-ids #{1 10 42161}
           :position                 0
           :clock                    1698945829328
           :created-at               1698928839000
           :operable                 "fully"
           :mixedcase-address        "0x7bcDfc75c431"
           :public-key               "0x04371e2d9d66b82f056bc128064"
           :removed                  false}
          {:path                     "m/44'/60'/0'/0/1"
           :emoji                    "💎"
           :key-uid                  "0x2f5ea39"
           :address                  "0x2"
           :wallet                   false
           :name                     "Account Two"
           :type                     :generated
           :chat                     false
           :test-preferred-chain-ids #{5 420 421613}
           :customization-color      :purple
           :hidden                   false
           :prod-preferred-chain-ids #{1 10 42161}
           :position                 1
           :clock                    1698945829328
           :created-at               1698928839000
           :operable                 "fully"
           :mixedcase-address        "0x7bcDfc75c431"
           :public-key               "0x04371e2d9d66b82f056bc128064"
           :removed                  false})
        (rf/sub [sub-name])))))

(h/deftest-sub :wallet/current-viewing-account
  [sub-name]
  (testing "returns current account with balance base"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc :wallet/tokens tokens)))
    (is
     (= {:path                     "m/44'/60'/0'/0/0"
         :emoji                    "😃"
         :key-uid                  "0x2f5ea39"
         :address                  "0x1"
         :wallet                   false
         :name                     "Account One"
         :type                     :generated
         :chat                     false
         :test-preferred-chain-ids #{5 420 421613}
         :customization-color      :blue
         :hidden                   false
         :prod-preferred-chain-ids #{1 10 42161}
         :position                 0
         :clock                    1698945829328
         :created-at               1698928839000
         :operable                 "fully"
         :mixedcase-address        "0x7bcDfc75c431"
         :public-key               "0x04371e2d9d66b82f056bc128064"
         :removed                  false
         :balance                  3250}
        (rf/sub [sub-name])))))
