(ns status-im.subs.wallet.activities-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [status-im.subs.root]
    [status-im.subs.wallet.collectibles]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/all-activities
  [sub-name]
  (testing "Return the activities list from wallet data"
    (swap! rf-db/app-db assoc-in
      [:wallet :activities]
      [{:id 1 :name "Transaction1"}
       {:id 2 :name "Transaction2"}])
    (is (match? [{:id 1 :name "Transaction1"} {:id 2 :name "Transaction2"}] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/activities-for-current-viewing-account
  [sub-name]
  (testing "Return activities filtered and grouped by account and dates"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :activities]
                      {"acc1" [{:activity-type constants/wallet-activity-type-send
                                :amount-out    "0x1"
                                :sender        "acc1"
                                :recipient     "acc2"
                                :timestamp     1588291200}
                               {:activity-type constants/wallet-activity-type-receive
                                :amount-in     "0x1"
                                :sender        "acc2"
                                :recipient     "acc1"
                                :timestamp     1588377600}
                               {:activity-type constants/wallet-activity-type-send
                                :amount-out    "0x1"
                                :sender        "acc1"
                                :recipient     "acc4"
                                :timestamp     1588464000}]
                       "acc3" [{:activity-type constants/wallet-activity-type-receive
                                :amount-in     "0x1"
                                :sender        "acc4"
                                :recipient     "acc3"
                                :timestamp     1588464000}]})
            (assoc-in [:wallet :current-viewing-account-address] "acc1"))))
    (is
     (match? [{:title     "May 3, 2020"
               :timestamp 1588464000
               :data      [{:relative-date "May 3, 2020"
                            :amount        "0"
                            :network-logo  nil
                            :recipient     "acc4"
                            :transaction   :send
                            :token         nil
                            :network-name  nil
                            :status        nil
                            :sender        "acc1"
                            :timestamp     1588464000}]}
              {:title     "May 2, 2020"
               :timestamp 1588377600
               :data      [{:relative-date "May 2, 2020"
                            :amount        "0"
                            :network-logo  nil
                            :recipient     "acc1"
                            :transaction   :receive
                            :token         nil
                            :network-name  nil
                            :status        nil
                            :sender        "acc2"
                            :timestamp     1588377600}]}
              {:title     "May 1, 2020"
               :timestamp 1588291200
               :data      [{:relative-date "May 1, 2020"
                            :amount        "0"
                            :network-logo  nil
                            :recipient     "acc2"
                            :transaction   :send
                            :token         nil
                            :network-name  nil
                            :status        nil
                            :sender        "acc1"
                            :timestamp     1588291200}]}]
             (rf/sub [sub-name])))))
