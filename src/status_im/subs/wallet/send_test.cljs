(ns status-im.subs.wallet.send-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.send
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/send-tab
  [sub-name]
  (testing "returns active tab for selecting address"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :select-address-tab] :tabs/recent)
    (is (= :tabs/recent (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-transaction-ids
  [sub-name]
  (testing "returns the transaction ids attached the last send flow"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123" "0x321"])
    (is (= ["0x123" "0x321"] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-transaction-progress
  [sub-name]
  (testing "returns transaction data for a transaction with multiple transactions"
    (swap! rf-db/app-db assoc-in
      [:wallet :transactions]
      {"0x123" {:status   :pending
                :id       240
                :chain-id 5}
       "0x321" {:status   :pending
                :id       240
                :chain-id 1}})
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123" "0x321"])
    (is (= {"0x123" {:status   :pending
                     :id       240
                     :chain-id 5}
            "0x321" {:status   :pending
                     :id       240
                     :chain-id 1}}
           (rf/sub [sub-name]))))

  (testing "returns transaction data for a transaction with a single transaction"
    (swap! rf-db/app-db assoc-in
      [:wallet :transactions]
      {"0x123" {:status   :pending
                :id       100
                :chain-id 5}
       "0x321" {:status   :pending
                :id       240
                :chain-id 1}})
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123"])
    (is (= {"0x123" {:status   :pending
                     :id       100
                     :chain-id 5}}
           (rf/sub [sub-name])))))

(h/deftest-sub :wallet/recent-recipients
  [sub-name]
  (testing "returns recent tab for selecting address"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :activities]
                      [{:sender "acc1" :recipient "acc2" :timestamp 1588291200}
                       {:sender "acc2" :recipient "acc1" :timestamp 1588377600}
                       {:sender "acc3" :recipient "acc4" :timestamp 1588464000}])
            (assoc-in [:wallet :current-viewing-account-address] "acc1"))))
    (is (= #{"acc2"} (rf/sub [sub-name])))))
