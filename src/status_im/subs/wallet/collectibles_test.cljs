(ns status-im.subs.wallet.collectibles-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.collectibles
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def ^:private traits
  [{:trait-type   "Background"
    :value        "Gradient 5"
    :display-type ""
    :max-value    ""}
   {:trait-type   "Skin"
    :value        "Pale"
    :display-type ""
    :max-value    ""}
   {:trait-type   "Clothes"
    :value        "Naked"
    :display-type ""
    :max-value    ""}])

(def ^:private collectible-owner-wallet
  {:last-collectible-details {:ownership [{:address "0x1"}]}
   :accounts                 {"0x1" {:name  "account 1"
                                     :color "army"}}})

(h/deftest-sub :wallet/last-collectible-details-chain-id
  [sub-name]
  (testing "correct chain-id of the last collectible should be returned"
    (swap! rf-db/app-db #(assoc-in % [:wallet :last-collectible-details :id :contract-id :chain-id] "1"))
    (let [result (rf/sub [sub-name])]
      (is (= "1" result)))))

(h/deftest-sub :wallet/last-collectible-details-traits
  [sub-name]
  (testing "correct traits of the last collectible should be returned"
    (swap! rf-db/app-db #(assoc-in %
                          [:wallet :last-collectible-details :collectible-data :traits]
                          traits))
    (let [result (rf/sub [sub-name])]
      (is (= traits result)))))

(h/deftest-sub :wallet/last-collectible-details-owner
  [sub-name]
  (testing "correct owner of the last collectible should be returned"
    (swap! rf-db/app-db #(assoc-in % [:wallet] collectible-owner-wallet))
    (let [result (rf/sub [sub-name])]
      (is (= {:name  "account 1"
              :color "army"}
             result)))))
