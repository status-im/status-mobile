(ns utils.ethereum.chain
  (:require [status-im.contexts.wallet.networks.core :as networks]))

(def BSC-mainnet-chain-id 56)
(def BSC-testnet-chain-id 97)

;; TODO @clauxx: is this ns needed? seems like leftover from when we supported one chain at a time
(def wallet-networks
  (reduce-kv (fn [m k v]
               (let [key-name (if (:testnet? v)
                                (-> v :network-name name (str "-testnet") keyword)
                                (:network-name v))]
                 (assoc m key-name {:id k :name (:full-name v)})))
             {}
             networks/networks-by-chain-id))

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids
(def chains
  (assoc wallet-networks
         :xdai        {:id 100 :name "xDai"}
         :sepolia     {:id 11155111 :name "Sepolia"}
         :bsc         {:id   BSC-mainnet-chain-id
                       :name "BSC"}
         :bsc-testnet {:id   BSC-testnet-chain-id
                       :name "BSC testnet"}))

(defn chain-id->chain-keyword
  [i]
  (or (some #(when (= i (:id (val %))) (key %)) chains)
      :custom))

(defn chain-keyword->chain-id
  [k]
  (get-in chains [k :id]))

(defn network->chain-id
  [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword
  [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn network->chain-name
  [network]
  (-> network
      network->chain-keyword
      name))

(defn get-current-network
  [_]
  nil)

(defn chain-keyword
  [db]
  (network->chain-keyword (get-current-network db)))

(defn chain-id
  [db]
  (network->chain-id (get-current-network db)))

(defn chain-ids
  [db]
  (let [test-networks-enabled? (get-in db [:profile/profile :test-networks-enabled?])
        networks               (get-in db [:wallet :networks])
        env-networks           (get networks (if test-networks-enabled? :test :prod))]
    (map :chain-id env-networks)))
