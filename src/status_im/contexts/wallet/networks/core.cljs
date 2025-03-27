(ns status-im.contexts.wallet.networks.core
  (:require [status-im.contexts.wallet.networks.config :as networks.config]
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
