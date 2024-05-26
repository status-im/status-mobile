(ns status-im.subs.wallet.activities-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.collectibles
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/all-activities
  [sub-name]
  (testing "Return the activities list from wallet data"
    (swap! rf-db/app-db assoc-in
      [:wallet :activities]
      [{:id 1 :name "Transaction1"}
       {:id 2 :name "Transaction2"}])
    (is (= [{:id 1 :name "Transaction1"} {:id 2 :name "Transaction2"}] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/activities-for-current-viewing-account
  [sub-name]
  (testing "Return activities filtered and grouped by account and dates"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :activities]
                      [{:sender "acc1" :recipient "acc2" :timestamp 1588291200}
                       {:sender "acc2" :recipient "acc1" :timestamp 1588377600}
                       {:sender "acc3" :recipient "acc4" :timestamp 1588464000}])
            (assoc-in [:wallet :current-viewing-account-address] "acc1"))))
    (is (= [{:title     "May 1, 2020"
             :data      [{:sender "acc1" :recipient "acc2" :timestamp 1588291200}]
             :timestamp 1588291200}]
           (rf/sub [sub-name])))))
