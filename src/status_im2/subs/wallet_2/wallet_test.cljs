(ns status-im2.subs.wallet-2.wallet-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            status-im2.subs.root
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

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
  [{:address "0x1"
    :name    "Main account"
    :hidden  false
    :removed false}
   {:address "0x2"
    :name    "Secondary account"
    :hidden  false
    :removed false}])

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(h/deftest-sub :wallet-2/balances
  [sub-name]
  (testing "returns vector of maps containing :address and :balance"
    (swap! rf-db/app-db assoc
      :profile/wallet-accounts accounts
      :wallet-2/tokens         tokens)
    (is (= [{:address "0x1"
             :balance 3250}
            {:address "0x2"
             :balance 2100}]
           (rf/sub [sub-name])))))
