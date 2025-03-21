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

(def networks
  [mainnet/network
   optimism/network
   arbitrum/network
   base/network])

(def sepolia-networks
  [mainnet/sepolia-network
   optimism/sepolia-network
   arbitrum/sepolia-network
   base/sepolia-network
   status/sepolia-network])

(def new-networks
  (->> [base/network
        status/sepolia-network]
       (map :network-name)
       set))

(defn chain-ids
  [testnet?]
  (->> (if testnet? sepolia-networks networks)
       (map :chain-id)
       set))

(def all-networks (concat networks sepolia-networks))

(def network-names
  (->> all-networks
       (map :network-name)
       set))

;; NOTE: runs schema validation over all the networks only in debug
;; mode to make sure the networks are defined correctly
(when ^boolean js/goog.DEBUG
  (map validation/validate-network all-networks))

;; mappings

(def networks-by-chain-id
  (into {} (map (juxt :chain-id identity)) all-networks))

(def networks-by-network-name
  (into {} (map (juxt :network-name identity)) networks))

(def sepolia-networks-by-network-name
  (into {} (map (juxt :network-name identity)) sepolia-networks))

(def networks-by-short-name
  (into {} (map (juxt :short-name identity)) networks))

(defn chain-id->network-name
  [chain-id]
  (get-in networks-by-chain-id [chain-id :network-name]))

(defn network-name->chain-id
  [network-name testnet?]
  (-> (if testnet? sepolia-networks-by-network-name networks-by-network-name)
      (get network-name)
      (get :chain-id)))

(defn network-name->short-name
  [network-name]
  (get-in networks-by-network-name [network-name :short-name]))

(defn short-name->network-name
  [short-name]
  (get-in networks-by-short-name [short-name :network-name]))

;; extractors

(defn network-details
  [chain-id]
  (get networks-by-chain-id chain-id))

(defn block-explorer-address-url
  ([chain-id]
   (-> networks-by-chain-id
       (get chain-id 1)
       (get :block-explorer-url)
       (str "address")))
  ([chain-id address]
   (-> (block-explorer-address-url chain-id)
       (str "/" address))))

(defn block-explorer-tx-url
  [chain-id tx-hash]
  (-> networks-by-chain-id
      (get chain-id 1)
      (get :block-explorer-url)
      (str "tx/" tx-hash)))

(defn block-explorer-name
  [chain-id]
  (get-in networks-by-chain-id [chain-id :block-explorer-name]))

(defn full-name
  [chain-id]
  (get-in networks-by-chain-id [chain-id :full-name]))

(defn short-name
  [chain-id]
  (get-in networks-by-chain-id [chain-id :short-name]))

(defn accessibility-label
  [chain-id prefix]
  (-> (str prefix "-" (short-name chain-id))
      keyword))

(defn new-network?
  [network-name]
  (contains? new-networks network-name))
