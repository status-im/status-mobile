(ns status-im.subs.wallet.saved-addresses-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.wallet.saved-addresses
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/saved-addresses
  [sub-name]
  (testing "returns a list of all addresses saved in database"
    (swap! rf-db/app-db assoc-in
      [:wallet :saved-addresses]
      [{:address 1} {:address 2}])
    (is (= [{:address 1} {:address 2}] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/address-saved?
  [sub-name]
  (testing "return true if a give string address is saved in the database, false otherwise"
    (swap! rf-db/app-db assoc-in
      [:wallet :saved-addresses]
      [{:address 1} {:address 2}])
    (is (true? (rf/sub [sub-name 1])))
    (is (false? (rf/sub [sub-name 24])))))

(h/deftest-sub :wallet/saved-address-by-address
  [sub-name]
  (testing "return all address info for a given string address"
    (swap! rf-db/app-db assoc-in
      [:wallet :saved-addresses]
      [{:address 1 :other :metadata1} {:address 2 :other :metadata2}])
    (is (= {:address 1 :other :metadata1} (rf/sub [sub-name 1])))))
