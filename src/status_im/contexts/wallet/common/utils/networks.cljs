(ns status-im.contexts.wallet.common.utils.networks
  (:require
    [clojure.string :as string]
    [status-im.contexts.wallet.networks.core :as networks]
    [utils.money :as money]
    [utils.number]))

(def ^:private max-network-prefixes 2)

(defn network->chain-id
  ([db network]
   (let [{:keys [test-networks-enabled?]} (:profile/profile db)]
     (network->chain-id {:network          network
                         :testnet-enabled? test-networks-enabled?})))
  ([{:keys [network testnet-enabled?]}]
   (-> network
       keyword
       (networks/get-chain-id testnet-enabled?))))

(defn network-list
  [{:keys [balances-per-chain]} networks]
  (->> balances-per-chain
       keys
       (map (fn [chain-id]
              (first (filter #(or (= (:chain-id %) chain-id)
                                  (= (:related-chain-id %) chain-id))
                             networks))))
       set))

(defn network-list-with-positive-balance
  "Same as `network-list`, but only returns the networks that have a positive token balance"
  [{:keys [balances-per-chain] :as token} networks]
  (as-> balances-per-chain $
    (filter #(-> % second :raw-balance money/above-zero?) $)
    (assoc token :balances-per-chain $)
    (network-list $ networks)))

(defn short-names->network-preference-prefix
  [short-names]
  (str (string/join ":" short-names) ":"))

(defn network-preference-prefix->network-names
  [prefix]
  (as-> prefix $
    (string/split $ ":")
    (map networks/short-name->network-name $)
    (remove nil? $)))

(defn network-names->network-preference-prefix
  [network-names]
  (if (empty? network-names)
    ""
    (->> network-names
         (map networks/network-name->short-name)
         (remove nil?)
         short-names->network-preference-prefix)))

(defn split-network-full-address
  [address]
  (as-> address $
    (string/split $ ":")
    [(butlast $) (last $)]))

(defn sorted-networks-with-details
  [networks]
  (->> networks
       (map
        (fn [network]
          (-> network :chain-id networks/network-details)))
       (sort-by (juxt :layer :short-name))))

(defn format-address
  [address network-preferences]
  (let [short-names         (map networks/network-name->short-name network-preferences)
        prefix              (when (<= (count short-names) max-network-prefixes)
                              (short-names->network-preference-prefix short-names))
        transformed-address (str prefix address)]
    transformed-address))

(defn network-summary
  [network token-symbol amount]
  (let [formatted-amount (if (money/equal-to amount 0)
                           "<0.01"
                           amount)
        summary          {:amount       formatted-amount
                          :token-symbol token-symbol}]
    (if (= :mainnet network)
      {:mainnet summary}
      {network summary})))
