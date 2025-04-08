(ns status-im.contexts.wallet.networks.core
  (:require [status-im.contexts.wallet.networks.config :as networks.config]
            [status-im.contexts.wallet.networks.filter :as networks.filter]
            [utils.url :as url]))

(defn get-chain-id
  [networks network-name]
  (some->> networks
           (some #(when (= (keyword network-name) (:network-name %)) %))
           :chain-id))

(defn new-network?
  "Checks if the network should be highlighted as `new` in the UI, based on the local
  networks configuration"
  [chain-id]
  (contains? networks.config/new-networks chain-id))

(defn eth-mainnet?
  "Checks if the passed network is the Ethereum Mainnet chain"
  [network]
  (-> network
      :chain-id
      (= 1)))

(defn get-block-explorer-tx-url
  "Returns the block-explorer transaction url for a chain"
  [network tx-hash]
  (-> network
      :block-explorer-url
      (url/add-path :tx tx-hash)))

(defn get-block-explorer-address-url
  "Returns the block-explorer address url for a chain"
  [network address]
  (-> network
      :block-explorer-url
      (url/add-path :address address)))

(defn get-networks-for-layer
  "Returns networks for the layer (`1` or `2`)"
  [networks layer]
  (keep (fn [network]
          (when (= layer (:layer network))
            network))
        networks))

(defn get-chain-ids
  [networks]
  (->> networks
       (map :chain-id)
       set))

(defn get-active-networks
  "Returns only active networks"
  [networks]
  (keep #(when (:active? %) %) networks))

(defn get-active-chain-ids
  "Returns only active network chain-ids"
  [networks]
  (->> networks
       get-active-networks
       get-chain-ids))

(defn get-max-active-networks
  []
  networks.config/max-active-networks)

(defn get-filtered-networks
  [networks network-filter]
  (-> networks
      (networks.filter/by-id network-filter)))

(defn get-filtered-chain-ids
  [networks network-filter]
  (->> network-filter
       (get-filtered-networks networks)
       get-chain-ids))
