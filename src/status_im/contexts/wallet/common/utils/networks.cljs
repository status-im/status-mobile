(ns status-im.contexts.wallet.common.utils.networks
  (:require [clojure.string :as string]
            [status-im.constants :as constants]
            [utils.number]))

(def id->network
  {constants/ethereum-mainnet-chain-id constants/mainnet-network-name
   constants/ethereum-goerli-chain-id  constants/mainnet-network-name
   constants/ethereum-sepolia-chain-id constants/mainnet-network-name
   constants/optimism-mainnet-chain-id constants/optimism-network-name
   constants/optimism-goerli-chain-id  constants/optimism-network-name
   constants/optimism-sepolia-chain-id constants/optimism-network-name
   constants/arbitrum-mainnet-chain-id constants/arbitrum-network-name
   constants/arbitrum-goerli-chain-id  constants/arbitrum-network-name
   constants/arbitrum-sepolia-chain-id constants/arbitrum-network-name})

(defn- get-chain-id
  [{:keys [mainnet-chain-id sepolia-chain-id goerli-chain-id testnet-enabled? goerli-enabled?]}]
  (cond
    (and testnet-enabled? goerli-enabled?)
    goerli-chain-id

    testnet-enabled?
    sepolia-chain-id

    :else
    mainnet-chain-id))

(defn network->chain-id
  ([db network]
   (let [{:keys [test-networks-enabled? is-goerli-enabled?]} (:profile/profile db)]
     (network->chain-id {:network          network
                         :testnet-enabled? test-networks-enabled?
                         :goerli-enabled?  is-goerli-enabled?})))
  ([{:keys [network testnet-enabled? goerli-enabled?]}]
   (condp contains? (keyword network)
     #{constants/mainnet-network-name (keyword constants/mainnet-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/ethereum-mainnet-chain-id
       :sepolia-chain-id constants/ethereum-sepolia-chain-id
       :goerli-chain-id  constants/ethereum-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?})

     #{constants/optimism-network-name (keyword constants/optimism-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/optimism-mainnet-chain-id
       :sepolia-chain-id constants/optimism-sepolia-chain-id
       :goerli-chain-id  constants/optimism-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?})

     #{constants/arbitrum-network-name (keyword constants/arbitrum-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/arbitrum-mainnet-chain-id
       :sepolia-chain-id constants/arbitrum-sepolia-chain-id
       :goerli-chain-id  constants/arbitrum-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?}))))

(defn network-list
  [{:keys [balances-per-chain]} networks]
  (into #{}
        (mapv (fn [chain-id]
                (first (filter #(or (= (:chain-id %) chain-id)
                                    (= (:related-chain-id %) chain-id))
                               networks)))
              (keys balances-per-chain))))

(defn get-default-chain-ids-by-mode
  [{:keys [test-networks-enabled? is-goerli-enabled?]}]
  (cond
    (and test-networks-enabled? is-goerli-enabled?)
    constants/goerli-chain-ids

    test-networks-enabled?
    constants/sepolia-chain-ids

    :else
    constants/mainnet-chain-ids))

(defn resolve-receiver-networks
  [{:keys [prefix testnet-enabled? goerli-enabled?]}]
  (let [prefix     (if (string/blank? prefix)
                     constants/default-multichain-address-prefix
                     prefix)
        prefix-seq (string/split prefix #":")]
    (->> prefix-seq
         (remove string/blank?)
         (mapv
          #(network->chain-id
            {:network          %
             :testnet-enabled? testnet-enabled?
             :goerli-enabled?  goerli-enabled?})))))
