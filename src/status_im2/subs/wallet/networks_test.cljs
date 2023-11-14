(ns status-im2.subs.wallet.networks-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im2.subs.root
    status-im2.subs.wallet.networks
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def network-data
  {:test [{:test?            true
           :short-name       "eth"
           :network-name     :ethereum
           :related-chain-id 1}
          {:test?            true
           :short-name       "arb1"
           :related-chain-id 42161}
          {:test?            true
           :short-name       "opt"
           :related-chain-id 10}]
   :prod [{:test?      false
           :short-name "eth"
           :chain-id   1}
          {:test?      false
           :short-name "arb1"
           :chain-id   42161}
          {:test?      false
           :short-name "opt"
           :chain-id   10}]})

(h/deftest-sub :wallet/network-details
  [sub-name]
  (testing "returns data with prod"
    (swap! rf-db/app-db assoc :wallet/networks network-data)
    (is (= [{:network-name :ethereum
             :short-name   "eth"
             :chain-id     1}
            {:network-name :arbitrum
             :short-name   "arb1"
             :chain-id     42161}
            {:network-name :optimism
             :short-name   "opt"
             :chain-id     10}]
           (map #(dissoc % :source :related-chain-id) (rf/sub [sub-name]))))))
