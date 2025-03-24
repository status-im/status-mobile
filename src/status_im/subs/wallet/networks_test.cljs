(ns status-im.subs.wallet.networks-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.networks.config :as networks.config]
    status-im.subs.root
    status-im.subs.wallet.networks
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def mainnet-chain-id networks.config/ethereum-chain-id)
(def optimism-chain-id networks.config/optimism-chain-id)
(def arbitrum-chain-id networks.config/arbitrum-chain-id)
(def sepolia-chain-id networks.config/sepolia-chain-id)
(def arbitrum-sepolia-chain-id networks.config/arbitrum-sepolia-chain-id)
(def optimism-sepolia-chain-id networks.config/optimism-sepolia-chain-id)
(def mainnet-name :mainnet)
(def optimism-name :optimism)
(def arbitrum-name :arbitrum)

(def mainnet
  {:test?        false
   :short-name   "eth"
   :network-name mainnet-name
   :chain-id     mainnet-chain-id
   :layer        1})

(def arbitrum
  {:test?        false
   :short-name   "arb1"
   :network-name arbitrum-name
   :chain-id     arbitrum-chain-id
   :layer        2})

(def optimism
  {:test?        false
   :short-name   "oeth"
   :network-name optimism-name
   :chain-id     optimism-chain-id
   :layer        2})

(def sepolia
  (assoc mainnet
         :test?    true
         :chain-id sepolia-chain-id))

(def arbitrum-sepolia
  (assoc mainnet
         :test?    true
         :chain-id arbitrum-sepolia-chain-id))

(def optimism-sepolia
  (assoc mainnet
         :test?    true
         :chain-id optimism-sepolia-chain-id))

(def network-data
  {:prod [mainnet
          arbitrum
          optimism]
   :test [sepolia
          arbitrum-sepolia
          optimism-sepolia]})

(def network-data-by-id
  {mainnet-chain-id          mainnet
   optimism-chain-id         optimism
   arbitrum-chain-id         arbitrum
   sepolia-chain-id          sepolia
   arbitrum-sepolia-chain-id arbitrum-sepolia
   optimism-sepolia-chain-id optimism-sepolia})

(h/deftest-sub :wallet/network-details
  [sub-name]
  (testing "returns data with prod"
    (swap! rf-db/app-db assoc-in [:wallet :networks] network-data)
    (is
     (match? (get network-data :prod)
             (rf/sub [sub-name])))))

(h/deftest-sub :wallet/network-details-by-network-name
  [sub-name]
  (testing "returns the prod network data that is accessible by the network name"
    (swap! rf-db/app-db assoc-in [:wallet :networks] network-data)
    (is
     (match?
      {:mainnet  mainnet
       :arbitrum arbitrum
       :optimism optimism}
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/network-values
  [sub-name]
  (testing "network values for the from account are returned correctly"
    (swap! rf-db/app-db #(-> %
                             (assoc-in [:wallet :networks-by-id] network-data-by-id)
                             (assoc-in
                              [:wallet :ui :send]
                              {:from-values-by-chain {mainnet-chain-id 100}
                               :to-values-by-chain   {arbitrum-chain-id 100}
                               :token-display-name   "ETH"})))
    (is
     (match? {:mainnet {:amount "100" :token-symbol "ETH"}} (rf/sub [sub-name false]))))

  (testing "network values for the to account are returned correctly"
    (swap! rf-db/app-db #(-> %
                             (assoc-in [:wallet :networks-by-id] network-data-by-id)
                             (assoc-in
                              [:wallet :ui :send]
                              {:from-values-by-chain {mainnet-chain-id 100}
                               :to-values-by-chain   {arbitrum-chain-id 100}
                               :token-display-name   "ARB1"})))
    (is
     (match? {:arbitrum {:amount "100" :token-symbol "ARB1"}} (rf/sub [sub-name true])))))
