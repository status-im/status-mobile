(ns networks.core
  (:require
    [malli.core]
    [malli.error]
    [networks.arbitrum :as arbitrum]
    [networks.base :as base]
    [networks.mainnet :as mainnet]
    [networks.optimism :as optimism]
    [networks.status :as status]
    [networks.validation]))

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
  (map networks.validation/validate-network all-networks))

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

(defn chain-explorer-url
  ([chain-id]
   (-> networks-by-chain-id
       (get chain-id 1)
       (get :chain-explorer-base-url)))
  ([chain-id address]
   (-> (chain-explorer-url chain-id)
       (str "/" address))))

(defn tx-details-url
  [chain-id tx-hash]
  (-> networks-by-chain-id
      (get chain-id 1)
      (get :tx-details-base-url)
      (str "/" tx-hash)))

(defn chain-explorer-name
  [chain-id]
  (get-in networks-by-chain-id [chain-id :chain-explorer-name]))

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
