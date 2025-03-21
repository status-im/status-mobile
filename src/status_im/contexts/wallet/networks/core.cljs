(ns status-im.contexts.wallet.networks.core
  (:require
    [malli.core]
    [malli.error]
    [status-im.contexts.wallet.networks.chains.arbitrum :as arbitrum]
    [status-im.contexts.wallet.networks.chains.base :as base]
    [status-im.contexts.wallet.networks.chains.mainnet :as mainnet]
    [status-im.contexts.wallet.networks.chains.optimism :as optimism]
    [status-im.contexts.wallet.networks.chains.status :as status]
    [status-im.contexts.wallet.networks.validation :as validation]))

;; NOTE: to add a new chain:
;; 1. add new ns in `chains` with the chain details
;; 2. add them to `networks` and `sepolia` defs respectively
;; 3. add network image resource to `quo.foundations.resources/networks`
;; 4. add alchemy tokens to `shadow-cljs.edn` (if necessary)

(def ^:private networks
  {:prod [mainnet/network
          optimism/network
          arbitrum/network
          base/network]
   :test [mainnet/sepolia-network
          optimism/sepolia-network
          arbitrum/sepolia-network
          base/sepolia-network
          status/sepolia-network]})

(def ^:private new-networks
  [base/network
   status/sepolia-network])

(def ^:private all-networks
  (->> networks
       vals
       (apply concat)))

;; NOTE: runs schema validation over all the networks only in debug
;; mode to make sure the networks are defined correctly
(when ^boolean js/goog.DEBUG
  (map validation/validate-network all-networks))

(def networks-by-chain-id
  (into {} (map (juxt :chain-id identity)) all-networks))

(defn- networks-by-testnet-mode
  [testnet?]
  (if testnet?
    (:test networks)
    (:prod networks)))

(defn chain-ids
  ([]
   (chain-ids false))
  ([testnet?]
   (->> (networks-by-testnet-mode testnet?)
        (map :chain-id)
        set)))

(defn network-names
  ([]
   (network-names false))
  ([testnet?]
   (->> (networks-by-testnet-mode testnet?)
        (map :network-name)
        set)))

(def all-network-names
  (->> all-networks
       (map :network-name)
       set))

(defn get-chain-id
  [network-name]
  (->> networks
       :prod
       (some #(when (= network-name (:network-name %)) %))
       :chain-id))

(defn get-testnet-chain-id
  [network-name]
  (->> networks
       :test
       (some #(when (= network-name (:network-name %)) %))
       :chain-id))

(defn new-network?
  [chain-id]
  (contains? (->> new-networks
                  (map :chain-id)
                  set)
             chain-id))

(defn get-network-details
  [chain-id]
  (get networks-by-chain-id chain-id 1))

(defn get-network-name
  [chain-id]
  (-> chain-id
      get-network-details
      :network-name))

(defn get-block-explorer-address-url
  [chain-id address]
  (-> chain-id
      get-network-details
      (get :block-explorer-url)
      (str "address/" address)))

(defn get-block-explorer-tx-url
  [chain-id tx-hash]
  (-> chain-id
      get-network-details
      (get :block-explorer-url)
      (str "tx/" tx-hash)))

(defn get-block-explorer-name
  [chain-id]
  (-> chain-id
      get-network-details
      :block-explorer-name))

(defn eth-mainnet?
  [chain-id]
  (= 1 chain-id))
