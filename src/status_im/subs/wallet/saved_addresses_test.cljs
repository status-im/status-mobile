(ns status-im.subs.wallet.saved-addresses-test
  (:require [clojure.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(def saved-addresses-data
  [{:address "0x0" :colorId "blue" :chainShortNames "eth:" :isTest false :name "Alice's address"}
   {:address "0x1" :colorId "purple" :chainShortNames "eth:" :isTest false :name "Bob's address"}])

(def grouped-saved-addresses-data
  [{:title "A"
    :data
    [{:address "0x0" :colorId "blue" :chainShortNames "eth:" :isTest false :name "Alice's address"}]}
   {:title "B"
    :data
    [{:address "0x1" :colorId "purple" :chainShortNames "eth:" :isTest false :name "Bob's address"}]}])

(h/deftest-sub :wallet/grouped-saved-addresses
  [sub-name]
  (testing "returns data with prod"
    (swap! rf-db/app-db assoc-in [:wallet :saved-addresses] saved-addresses-data)
    (is
     (= grouped-saved-addresses-data
        (rf/sub [sub-name])))))
