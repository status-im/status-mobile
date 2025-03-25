(ns status-im.contexts.wallet.networks.core
  (:require [status-im.contexts.profile.data-store :as profile]
            [status-im.contexts.wallet.networks.config :as networks.config]))

(defn new-network?
  "Checks if the network should be highlighted as `new` in the UI, based on the local
  networks configuration"
  [chain-id]
  (contains? networks.config/new-networks chain-id))

(defn get-testnet-mode-key
  "Returns either `:test` or `:prod` based on the testnet setting from db"
  [db]
  (if (profile/testnet? db) :test :prod))

(defn get-network-details
  "Returns the network details based on the `chain-id` from the db"
  [db chain-id]
  (get-in db [:wallet :networks-by-id chain-id]))

(defn get-networks
  "Returns all networks from db, based on the testnet setting"
  [db]
  (get-in db [:wallet :networks (get-testnet-mode-key db)]))

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
      :block-explorer-url
      (str "address/" address)))

(defn get-block-explorer-tx-url
  "Returns the block-explorer transaction url for a chain"
  ([db chain-id tx-hash]
   (-> db
       (get-network-details chain-id)
       (get-block-explorer-tx-url tx-hash)))
  ([network tx-hash]
   (-> network
       :block-explorer-url
       (str "tx/" tx-hash))))

(defn eth-mainnet?
  "Checks if the passed network is the Ethereum Mainnet chain"
  [network]
  (-> network
      :chain-id
      (= 1)))
