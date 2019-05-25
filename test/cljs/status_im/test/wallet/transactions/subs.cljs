(ns status-im.test.wallet.transactions.subs
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as re-frame]
            [day8.re-frame.test :refer [run-test-sync]]
            status-im.ui.screens.db
            status-im.subs
            [status-im.ui.screens.events :as events]
            [status-im.subs :as transactions-subs]))

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
  "Check if transactions are sorted by date"
  (is (= (transactions-subs/group-transactions-by-date transactions)
         grouped-transactions)))
