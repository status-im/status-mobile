(ns utils.ethereum.chain
  (:require [status-im.constants :as constants]))

(def BSC-mainnet-chain-id 56)
(def BSC-testnet-chain-id 97)

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids
(def chains
  {:mainnet         {:id   constants/ethereum-mainnet-chain-id
                     :name "Mainnet"}
   :xdai            {:id 100 :name "xDai"}
   :goerli          {:id   constants/ethereum-goerli-chain-id
                     :name "Ethreum Goerli"}
   :bsc             {:id   BSC-mainnet-chain-id
                     :name "BSC"}
   :bsc-testnet     {:id   BSC-testnet-chain-id
                     :name "BSC testnet"}
   :arbitrum        {:id   constants/arbitrum-mainnet-chain-id
                     :name "Arbitrum"}
   :arbitrum-goerli {:id   constants/arbitrum-goerli-chain-id
                     :name "Arbitrum Goerli"}
   :optimism        {:id   constants/optimism-mainnet-chain-id
                     :name "Optimism"}
   :optimism-goerli {:id   constants/optimism-goerli-chain-id
                     :name "Optimism Goerli"}})

(defn chain-id->chain-keyword
  [i]
  (or (some #(when (= i (:id (val %))) (key %)) chains)
      :custom))

(defn chain-id->chain-name
  [i]
  (or (some #(when (= i (:id (val %))) (:name (val %))) chains)
      :custom))

(defn chain-keyword->chain-id
  [k]
  (get-in chains [k :id]))

(defn chain-keyword->snt-symbol
  [k]
  (case k
    :mainnet :SNT
    :STT))

(defn testnet?
  [id]
  (contains? #{(chain-keyword->chain-id :goerli)
               (chain-keyword->chain-id :bsc-testnet)}
             id))

(defn network->chain-id
  [network]
  (get-in network [:config :NetworkId]))

(defn network->chain-keyword
  [network]
  (chain-id->chain-keyword (network->chain-id network)))

(defn binance-chain-id?
  [chain-id]
  (or (= BSC-mainnet-chain-id chain-id)
      (= BSC-testnet-chain-id chain-id)))

(defn current-network
  [db]
  (let [networks   (get db :networks/networks)
        network-id (get db :networks/current-network)]
    (get networks network-id)))

(defn binance-chain?
  [db]
  (-> db
      current-network
      network->chain-id
      binance-chain-id?))

(defn network->chain-name
  [network]
  (-> network
      network->chain-keyword
      name))

(defn get-current-network
  [m]
  (get (:networks/networks m) (:networks/current-network m)))

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


(defn chain-ids->address-prefix [chain-ids])

(defn chain-short-ids->address-prefix [chain-short-ids])

(defn address-prefix->chain-ids [adress-prefix])

(defn address-prefix->chain-short-ids [address-prefix])
