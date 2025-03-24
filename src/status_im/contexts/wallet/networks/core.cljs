(ns status-im.contexts.wallet.networks.core
  (:require [status-im.contexts.wallet.networks.config :as networks.config]))

(defn new-network?
  [chain-id]
  (contains? networks.config/new-networks chain-id))

(defn get-testnet-mode-key
  [testnet?]
  (if testnet? :test :prod))

(defn get-network-details
  [db chain-id]
  (get-in db [:wallet :networks-by-id chain-id]))

(defn get-block-explorer-address-url
  ([db chain-id address]
   (-> db
       (get-network-details chain-id)
       (get-block-explorer-address-url address)))
  ([network address]
   (-> network
       :block-explorer-url
       (str "address/" address))))

(defn get-block-explorer-tx-url
  ([db chain-id tx-hash]
   (-> db
       (get-network-details chain-id)
       (get-block-explorer-tx-url tx-hash)))
  ([network tx-hash]
   (-> network
       :block-explorer-url
       (str "tx/" tx-hash))))

(defn eth-mainnet?
  [network]
  (-> network
      :chain-id
      (= 1)))
