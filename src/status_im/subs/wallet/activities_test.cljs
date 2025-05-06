(ns status-im.subs.wallet.activities-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [status-im.subs.root]
    [status-im.subs.wallet.collectibles]
    [test-helpers.unit :as h]
    [tests.wallet-test-data :as test-data]
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
            (test-data/add-networks-to-db)
            (assoc-in [:wallet :tokens :by-symbols]
                      (list {:symbol "ETH" :decimals 18}
                            {:symbol "DAI" :decimals 18}
                            {:symbol "SNT" :decimals 18}
                            {:symbol "USDT" :decimals 6}))
            (assoc-in [:wallet :activities]
                      {"0x1" {1 {:activity-type constants/wallet-activity-type-send
                                 :amount-out    "0x1"
                                 :symbol-out    "ETH"
                                 :sender        "0x1"
                                 :recipient     "0x2"
                                 :chain-id-out  1
                                 :timestamp     1588291200}
                              3 {:activity-type constants/wallet-activity-type-bridge
                                 :amount-out    "0x1"
                                 :symbol-out    "ETH"
                                 :sender        "0x1"
                                 :recipient     "0x1"
                                 :chain-id-out  1
                                 :timestamp     1588464000}
                              4 {:activity-type constants/wallet-activity-type-swap
                                 :amount-out    "0x1"
                                 :symbol-out    "ETH"
                                 :amount-in     "0x1"
                                 :symbol-in     "SNT"
                                 :sender        "0x1"
                                 :recipient     "0x1"
                                 :chain-id-out  1
                                 :timestamp     1588464100}
                              5 {:activity-type constants/wallet-activity-type-send
                                 :amount-out    "0x1"
                                 :symbol-out    "ETH"
                                 :sender        "0x1"
                                 :recipient     "0x4"
                                 :chain-id-out  1
                                 :timestamp     1588464050}}
                       "0x3" {6 {:activity-type constants/wallet-activity-type-receive
                                 :amount-in     "0x1"
                                 :symbol-out    "ETH"
                                 :sender        "0x4"
                                 :recipient     "0x3"
                                 :chain-id-in   1
                                 :timestamp     1588464000}}})
            (assoc-in [:wallet :current-viewing-account-address] "0x1"))))
    (is
     (match?
      [{:title     "May 3, 2020"
        :timestamp 1588464100
        :data      [{:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :recipient        "0x1"
                     :token-id         nil
                     :amount-in        ""
                     :tx-type          :swap
                     :activity-type    3
                     :network-name-in  nil
                     :network-name-out "Mainnet"
                     :symbol-in        "SNT"
                     :symbol-out       "ETH"
                     :status           nil
                     :sender           "0x1"
                     :timestamp        1588464100}
                    {:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :recipient        "0x4"
                     :token-id         nil
                     :amount-in        nil
                     :tx-type          :send
                     :activity-type    0
                     :network-name-in  nil
                     :network-name-out "Mainnet"
                     :symbol-out       "ETH"
                     :status           nil
                     :sender           "0x1"
                     :timestamp        1588464050}
                    {:relative-date    "May 3, 2020"
                     :amount-out       "0"
                     :recipient        "0x1"
                     :token-id         nil
                     :amount-in        nil
                     :tx-type          :bridge
                     :activity-type    4
                     :network-name-in  nil
                     :network-name-out "Mainnet"
                     :symbol-out       "ETH"
                     :status           nil
                     :sender           "0x1"
                     :timestamp        1588464000}]}
       {:title     "May 1, 2020"
        :timestamp 1588291200
        :data      [{:relative-date    "May 1, 2020"
                     :amount-out       "0"
                     :recipient        "0x2"
                     :token-id         nil
                     :amount-in        nil
                     :tx-type          :send
                     :activity-type    0
                     :network-name-in  nil
                     :network-name-out "Mainnet"
                     :symbol-out       "ETH"
                     :status           nil
                     :sender           "0x1"
                     :timestamp        1588291200}]}]
      (rf/sub [sub-name])))))
