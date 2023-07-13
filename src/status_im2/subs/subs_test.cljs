(ns status-im2.subs.subs-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.subs.wallet.transactions :as wallet.transactions]))

(def transactions
  [{:timestamp "1505912551000"}
   {:timestamp "1505764322000"}
   {:timestamp "1505750000000"}])

(def grouped-transactions
  '({:title "20 Sep"
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
