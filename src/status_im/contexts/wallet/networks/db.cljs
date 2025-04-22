(ns status-im.contexts.wallet.networks.db
  (:require [clojure.set :as set]
            [status-im.contexts.profile.db :as profile.db]
            [status-im.contexts.wallet.networks.config :as networks.config]
            [status-im.contexts.wallet.networks.core :as networks]))

(defn get-testnet-mode-key
  "Returns either `:test` or `:prod` based on the testnet setting from db"
  [db]
  (if (profile.db/testnet? db) :test :prod))

(defn get-network-details
  "returns the network details based on the `chain-id` from the db"
  [db chain-id]
  (get-in db [:wallet :networks/by-id chain-id]))

(defn get-chain-ids
  "Returns all chain-ids from db, based on the testnet setting"
  [db]
  (set (get-in db [:wallet :networks/chain-ids-by-mode (get-testnet-mode-key db)])))

(defn get-networks
  "Returns all networks from db, based on the testnet setting"
  [db]
  (->> (get-in db [:wallet :networks/chain-ids-by-mode (get-testnet-mode-key db)])
       (map (partial get-network-details db))))

(defn get-active-chain-ids
  "Returns all active chain-ids from db"
  [db]
  (-> db
      get-networks
      networks/get-active-chain-ids))

(defn get-active-networks
  "Returns all active networks from db"
  [db]
  (->> db
       get-networks
       networks/get-active-networks))

(defn get-chain-id
  "Returns the `chain-id` based on the `network-name`"
  [db network-name]
  (-> db
      get-networks
      (networks/get-chain-id network-name)))

(defn get-network-name
  "returns the network name based on the `chain-id` from the db"
  [db chain-id]
  (-> db (get-network-details chain-id) :full-name))

(defn get-block-explorer-address-url
  "Returns the block-explorer address url for a chain"
  [db chain-id address]
  (-> db
      (get-network-details chain-id)
      (networks/get-block-explorer-address-url address)))

(defn get-block-explorer-tx-url
  "Returns the block-explorer transaction url for a chain"
  [db chain-id tx-hash]
  (-> db
      (get-network-details chain-id)
      (networks/get-block-explorer-tx-url tx-hash)))

(defn max-active-networks-reached?
  [db]
  (-> db
      get-active-chain-ids
      count
      (>= (networks/get-max-active-networks))))

(defn get-new-chain-ids
  [db]
  (-> db
      get-chain-ids
      (set/intersection networks.config/new-networks)))

(defn network-active?
  [db chain-id]
  (-> db
      get-active-chain-ids
      (contains? chain-id)))
