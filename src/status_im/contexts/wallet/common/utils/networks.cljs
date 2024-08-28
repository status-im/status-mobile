(ns status-im.contexts.wallet.common.utils.networks
  (:require [clojure.string :as string]
            [quo.foundations.resources :as resources]
            [status-im.constants :as constants]
            [utils.number]))

(def ^:private last-comma-followed-by-text-to-end-regex #",\s(?=[^,]+$)")

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

(def network->short-name
  {constants/mainnet-network-name  constants/mainnet-short-name
   constants/optimism-network-name constants/optimism-short-name
   constants/arbitrum-network-name constants/arbitrum-short-name
   constants/ethereum-network-name constants/ethereum-short-name})

(def short-name->network
  {constants/mainnet-short-name  constants/mainnet-network-name
   constants/optimism-short-name constants/optimism-network-name
   constants/arbitrum-short-name constants/arbitrum-network-name})

(defn short-names->network-preference-prefix
  [short-names]
  (str (string/join ":" short-names) ":"))

(defn network-preference-prefix->network-names
  [prefix]
  (as-> prefix $
    (string/split $ ":")
    (map short-name->network $)
    (remove nil? $)))

(defn network-names->network-preference-prefix
  [network-names]
  (if (empty? network-names)
    ""
    (->> network-names
         (map network->short-name)
         (remove nil?)
         short-names->network-preference-prefix)))

(defn network-ids->formatted-text
  [network-ids]
  (let [network-names  (->> network-ids
                            (map id->network)
                            (map name)
                            (map string/capitalize)
                            (string/join ", "))
        formatted-text (string/replace network-names last-comma-followed-by-text-to-end-regex " and ")]
    formatted-text))

(defn token-available-on-network?
  [token-networks chain-id]
  (let [token-networks-ids     (mapv #(:chain-id %) token-networks)
        token-networks-ids-set (set token-networks-ids)]
    (contains? token-networks-ids-set chain-id)))

(defn split-network-full-address
  [address]
  (as-> address $
    (string/split $ ":")
    [(butlast $) (last $)]))

(def mainnet-network-details
  {:source           (resources/get-network constants/mainnet-network-name)
   :short-name       constants/mainnet-short-name
   :full-name        constants/mainnet-full-name
   :network-name     constants/mainnet-network-name
   :abbreviated-name constants/mainnet-abbreviated-name})

(def arbitrum-network-details
  {:source           (resources/get-network constants/arbitrum-network-name)
   :short-name       constants/arbitrum-short-name
   :full-name        constants/arbitrum-full-name
   :network-name     constants/arbitrum-network-name
   :abbreviated-name constants/arbitrum-abbreviated-name})

(def optimism-network-details
  {:source           (resources/get-network constants/optimism-network-name)
   :short-name       constants/optimism-short-name
   :full-name        constants/optimism-full-name
   :network-name     constants/optimism-network-name
   :abbreviated-name constants/optimism-abbreviated-name})

(defn get-network-details
  [chain-id]
  (as-> chain-id $
    (condp contains? $
      #{constants/ethereum-mainnet-chain-id constants/ethereum-goerli-chain-id
        constants/ethereum-sepolia-chain-id}
      mainnet-network-details

      #{constants/arbitrum-mainnet-chain-id constants/arbitrum-goerli-chain-id
        constants/arbitrum-sepolia-chain-id}
      arbitrum-network-details

      #{constants/optimism-mainnet-chain-id constants/optimism-goerli-chain-id
        constants/optimism-sepolia-chain-id}
      optimism-network-details

      nil)
    (when $
      (assoc $ :chain-id chain-id))))

(defn sorted-networks-with-details
  [networks]
  (->> networks
       (map
        (fn [{:keys [chain-id related-chain-id layer]}]
          (assoc (get-network-details chain-id)
                 :chain-id         chain-id
                 :related-chain-id related-chain-id
                 :layer            layer)))
       (sort-by (juxt :layer :short-name))))
