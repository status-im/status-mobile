(ns status-im.subs.wallet.networks-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.networks
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def network-data
  {:test [{:test?            true
           :short-name       "eth"
           :network-name     :ethereum
           :related-chain-id 1
           :layer            1}
          {:test?            true
           :short-name       "arb1"
           :related-chain-id 42161
           :layer            2}
          {:test?            true
           :short-name       "opt"
           :related-chain-id 10
           :layer            2}]
   :prod [{:test?      false
           :short-name "eth"
           :chain-id   1
           :layer      1}
          {:test?      false
           :short-name "arb1"
           :chain-id   42161
           :layer      2}
          {:test?      false
           :short-name "opt"
           :chain-id   10
           :layer      2}]})

(h/deftest-sub :wallet/network-details
  [sub-name]
  (testing "returns data with prod"
    (swap! rf-db/app-db assoc :wallet/networks network-data)
    (is (= [{:network-name :ethereum
             :short-name   "eth"
             :chain-id     1
             :layer        1}
            {:network-name :arbitrum
             :short-name   "arb1"
             :chain-id     42161
             :layer        2}
            {:network-name :optimism
             :short-name   "opt"
             :chain-id     10
             :layer        2}]
           (map #(dissoc % :source :related-chain-id) (rf/sub [sub-name]))))))
