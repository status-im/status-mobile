(ns utils.ethereum.chain)

(def BSC-mainnet-chain-id 56)
(def BSC-testnet-chain-id 97)

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids
(def chains
  {:mainnet     {:id 1 :name "Mainnet"}
   :xdai        {:id 100 :name "xDai"}
   :goerli      {:id 5 :name "Goerli"}
   :bsc         {:id   BSC-mainnet-chain-id
                 :name "BSC"}
   :bsc-testnet {:id   BSC-testnet-chain-id
                 :name "BSC tetnet"}})

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
