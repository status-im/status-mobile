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
           :network-name     :mainnet
           :abbreviated-name "Eth."
           :full-name        "Mainnet"
           :related-chain-id 1
           :chain-id         3
           :layer            1}
          {:test?            true
           :short-name       "arb1"
           :related-chain-id 42161
           :chain-id         4
           :layer            2}
          {:test?            true
           :short-name       "oeth"
           :related-chain-id 10
           :chain-id         5
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
           :short-name "oeth"
           :chain-id   10
           :layer      2}]})

(h/deftest-sub :wallet/network-details
  [sub-name]
  (testing "returns data with prod"
    (swap! rf-db/app-db assoc-in [:wallet :networks] network-data)
    (is
     (= [{:network-name     :mainnet
          :short-name       "eth"
          :chain-id         1
          :abbreviated-name "Eth."
          :full-name        "Mainnet"
          :layer            1}
         {:network-name     :arbitrum
          :short-name       "arb1"
          :abbreviated-name "Arb1."
          :full-name        "Arbitrum"
          :chain-id         42161
          :layer            2}
         {:network-name     :optimism
          :short-name       "oeth"
          :abbreviated-name "Oeth."
          :full-name        "Optimism"
          :chain-id         10
          :layer            2}]
        (map #(dissoc % :source :related-chain-id) (rf/sub [sub-name]))))))

(h/deftest-sub :wallet/network-details-by-network-name
  [sub-name]
  (testing "returns the prod network data that is accessible by the network name"
    (swap! rf-db/app-db assoc-in [:wallet :networks] network-data)
    (is
     (match?
      {:mainnet  {:network-name     :mainnet
                  :short-name       "eth"
                  :chain-id         1
                  :abbreviated-name "Eth."
                  :full-name        "Mainnet"
                  :layer            1}
       :arbitrum {:network-name     :arbitrum
                  :short-name       "arb1"
                  :abbreviated-name "Arb1."
                  :full-name        "Arbitrum"
                  :chain-id         42161
                  :layer            2}
       :optimism {:network-name     :optimism
                  :short-name       "oeth"
                  :abbreviated-name "Oeth."
                  :full-name        "Optimism"
                  :chain-id         10
                  :layer            2}}
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/account-address
  [sub-name]
  (testing
    "returns the address with prefixes when an address and less than 3 network preferences are passed"
    (is
     (match? "eth:0x01" (rf/sub [sub-name "0x01" [:ethereum]]))))
  (testing
    "returns the address without the prefixes when an address and equal or more than 3 network preferences are passed"
    (is
     (match? "0x01" (rf/sub [sub-name "0x01" [:ethereum :optimism :arbitrum]])))))

(h/deftest-sub :wallet/network-values
  [sub-name]
  (testing "network values for the from account are returned correctly"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :send]
      {:from-values-by-chain {1 100}
       :to-values-by-chain   {42161 100}
       :token-display-name   "ETH"})
    (is
     (match? {:ethereum {:amount "100" :token-symbol "ETH"}} (rf/sub [sub-name false]))))

  (testing "network values for the to account are returned correctly"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :send]
      {:from-values-by-chain {1 100}
       :to-values-by-chain   {42161 100}
       :token-display-name   "ARB1"})
    (is
     (match? {:arbitrum {:amount "100" :token-symbol "ARB1"}} (rf/sub [sub-name true])))))
