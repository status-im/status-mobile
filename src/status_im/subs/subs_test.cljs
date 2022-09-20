(ns status-im.subs.subs-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im.subs.wallet.transactions :as wallet.transactions]
            [status-im.subs.onboarding :as onboarding]
            [status-im.utils.money :as money]
            [status-im.subs.wallet.wallet :as wallet]))

(def transactions [{:timestamp "1505912551000"}
                   {:timestamp "1505764322000"}
                   {:timestamp "1505750000000"}])

(def grouped-transactions '({:title "20 Sep"
                             :key :20170920
                             :data
                             ({:timestamp "1505912551000"})}
                            {:title "18 Sep"
                             :key :20170918
                             :data
                             ({:timestamp "1505764322000"}
                              {:timestamp "1505750000000"})}))

(deftest group-transactions-by-date
  (testing "Check if transactions are sorted by date"
    (is (= (wallet.transactions/group-transactions-by-date transactions)
           grouped-transactions))))

(deftest login-ma-keycard-pairing
  (testing "returns nil when no :multiaccounts/login"
    (let [res (onboarding/login-ma-keycard-pairing
               {:multiaccounts/login nil
                :multiaccounts/multiaccounts
                {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
               {})]
      (is (nil? res))))

  (testing "returns :keycard-pairing when :multiaccounts/login is present"
    (let [res (onboarding/login-ma-keycard-pairing
               {:multiaccounts/login {:key-uid "0x1"}
                :multiaccounts/multiaccounts
                {"0x1" {:keycard-pairing "keycard-pairing-code"}}}
               {})]
      (is (= res "keycard-pairing-code")))))

(deftest test-balance-total-value
  (is (= (wallet/get-balance-total-value
          {:ETH (money/bignumber 1000000000000000000)
           :SNT (money/bignumber 100000000000000000000)
           :AST (money/bignumber 10000)}
          {:ETH {:USD {:from "ETH", :to "USD", :price 677.91, :last-day 658.688}}
           :SNT {:USD {:from "SNT", :to "USD", :price 0.1562, :last-day 0.15}}
           :AST {:USD {:from "AST", :to "USD", :price 4,      :last-day 3}}}
          :USD
          {:ETH 18
           :SNT 18
           :AST 4})
         697.53)))