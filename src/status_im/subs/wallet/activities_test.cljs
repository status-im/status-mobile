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
  (testing
    "Return the activities list from wallet data"
    (swap! rf-db/app-db assoc-in
      [:wallet :activities]
      [{:id 1 :name "Transaction1"}
       {:id 2 :name "Transaction2"}])
    (let [result (rf/sub [sub-name])]
      (is (= [{:id 1 :name "Transaction1"} {:id 2 :name "Transaction2"}] @result)))))

(h/deftest-sub :wallet/activities-for-current-viewing-account
  [sub-name]
  (testing
    "Return activities filtered and grouped by account and dates"
    (swap! rf-db/app-db assoc
      {:wallet/all-activities                  [{:sender "acc1" :recipient "acc2" :timestamp 1588291200}
                                                {:sender "acc2" :recipient "acc1" :timestamp 1588377600}
                                                {:sender "acc3" :recipient "acc4" :timestamp 1588464000}]
       :wallet/current-viewing-account-address "acc1"})
    (let [result (rf/sub [sub-name])]
      (is (= [{:title "May 1" :data [{:sender "acc1" :recipient "acc2" :timestamp 1588291200}]}
              {:title "May 2" :data [{:sender "acc2" :recipient "acc1" :timestamp 1588377600}]}]
             @result)))))



