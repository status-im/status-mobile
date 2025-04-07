(ns utils.ethereum.chain
  {:deprecated true})

(def BSC-mainnet-chain-id 56)
(def BSC-testnet-chain-id 97)

;; IDs standardized in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md#list-of-chain-ids
(def chains
  {:mainnet     {:id 1 :name "Ethereum"}
   :xdai        {:id 100 :name "xDai"}
   :sepolia     {:id 11155111 :name "Sepolia"}
   :bsc         {:id   BSC-mainnet-chain-id
                 :name "BSC"}
   :bsc-testnet {:id   BSC-testnet-chain-id
                 :name "BSC testnet"}
   :arbitrum    {:id 42161 :name "Arbitrum"}
   :optimism    {:id 10 :name "Optimism"}
   :base        {:id 8453 :name "Base"}})

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
