(ns status-im2.subs.wallet-2.wallet-test 
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            status-im2.subs.wallet-2.wallet
            status-im2.subs.wallet.wallet
            [utils.re-frame :as rf]))

(def mock-data
  {:0x1 ({:balancesPerChain {:1 {:rawBalance "0"}
                             :42161 {:rawBalance "<nil>"}}
          :marketValuesPerCurrency {:USD {:price 0.02333}
                                    :usd {:price 0.02333}}}
         {:balancesPerChain {:1 {:rawBalance "0"}
                             :42161 {:rawBalance "<nil>"}}
          :marketValuesPerCurrency {:USD {:price 0.02333}
                                    :usd {:price 0.02333}}})
   :0x2 ({:balancesPerChain {:1 {:rawBalance "0"}
                             :42161 {:rawBalance "<nil>"}}
          :marketValuesPerCurrency {:USD {:price 0.02333}
                                    :usd {:price 0.02333}}}
         {:balancesPerChain {:1 {:rawBalance "0"}
                             :42161 {:rawBalance "<nil>"}}
          :marketValuesPerCurrency {:USD {:price 0.02333}
                                    :usd {:price 0.02333}}})})

(use-fixtures :each
  {:before #(reset! rf-db/app-db {})})

(h/deftest-sub :wallet-2/balances
  [sub-name]
  (testing "returns vector of maps containing :address and :balance"
      (swap! rf-db/app-db assoc 
             :wallet-2/tokens mock-data)
      (println (rf/sub [sub-name]) "mmilad")
      (is (= ({:address :0x1
               :balance 0} 
              {:address :0x2
               :balance 0}) (rf/sub [sub-name])))))
