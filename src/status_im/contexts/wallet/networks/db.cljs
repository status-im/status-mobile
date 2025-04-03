(ns status-im.contexts.wallet.networks.db
  (:require [status-im.contexts.profile.db :as profile.db]
            [status-im.contexts.wallet.networks.core :as networks]))

(defn get-testnet-mode-key
  "Returns either `:test` or `:prod` based on the testnet setting from db"
  [db]
  (if (profile.db/testnet? db) :test :prod))

(defn get-network-details
  "Returns the network details based on the `chain-id` from the db"
  [db chain-id]
  (get-in db [:wallet :networks-by-id chain-id]))

(defn get-networks
  "Returns all networks from db, based on the testnet setting"
  [db]
  (get-in db [:wallet :networks (get-testnet-mode-key db)]))

(defn get-chain-id
  "Returns the `chain-id` based on the `network-name`"
  [db network-name]
  (-> db
      get-networks
      (networks/get-chain-id network-name)))

(defn get-chain-ids
  "Returns all `chain-id`s, based on the testnet setting"
  [db]
  (->> db
       get-networks
       (map :chain-id)
       set))

(defn get-block-explorer-address-url
  "Returns the block-explorer address url for a chain"
  [db chain-id address]
  (-> db
      (get-network-details chain-id)
      (networks/get-block-explorer-address-url address)))
