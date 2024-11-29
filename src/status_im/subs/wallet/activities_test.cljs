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
                      {"acc1" {1 {:activity-type constants/wallet-activity-type-send
                                  :amount-out    "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc2"
                                  :timestamp     1588291200}
                               3 {:activity-type constants/wallet-activity-type-bridge
                                  :amount-out    "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc4"
                                  :timestamp     1588464000}
                               4 {:activity-type constants/wallet-activity-type-swap
                                  :amount-out    "0x1"
                                  :amount-in     "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc4"
                                  :timestamp     1588464100}
                               5 {:activity-type constants/wallet-activity-type-send
                                  :amount-out    "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc4"
                                  :timestamp     1588464050}}
                       "acc3" {6 {:activity-type constants/wallet-activity-type-receive
                                  :amount-in     "0x1"
                                  :sender        "acc4"
                                  :recipient     "acc3"
                                  :timestamp     1588464000}}})
            (assoc-in [:wallet :current-viewing-account-address] "acc1"))))
    (is
     (match?
      [{:title     "May 3, 2020"
        :timestamp 1588464100
        :data      [{:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :network-logo-out nil
                     :recipient        "acc4"
                     :tx-type          :swap
                     :network-name-out nil
                     :status           nil
                     :sender           "acc1"
                     :timestamp        1588464100}
                    {:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :network-logo-out nil
                     :recipient        "acc4"
                     :tx-type          :send
                     :network-name-out nil
                     :status           nil
                     :sender           "acc1"
                     :timestamp        1588464050}
                    {:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :network-logo-out nil
                     :recipient        "acc4"
                     :tx-type          :bridge
                     :network-name-out nil
                     :status           nil
                     :sender           "acc1"
                     :timestamp        1588464000}]}
       {:title     "May 1, 2020"
        :timestamp 1588291200
        :data      [{:relative-date    "May 1, 2020"
                     :amount-out       "0"
                     :network-logo-out nil
                     :recipient        "acc2"
                     :tx-type          :send
                     :network-name-out nil
                     :status           nil
                     :sender           "acc1"
                     :timestamp        1588291200}]}]
      (rf/sub [sub-name])))))
