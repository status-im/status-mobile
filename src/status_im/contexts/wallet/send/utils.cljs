(ns status-im.contexts.wallet.send.utils
  (:require
    [legacy.status-im.utils.hex :as utils.hex]
    [native-module.core :as native-module]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [utils.money :as money]))

(defn amount-in-hex
  [amount token-decimal]
  (money/to-hex (money/mul (money/bignumber amount) (money/from-decimal token-decimal))))

(defn map-multitransaction-by-ids
  [transaction-batch-id transaction-hashes]
  (reduce-kv (fn [map1 chain-id value1]
               (merge map1
                      (reduce
                       (fn [map2 tx-id]
                         (assoc map2
                                tx-id
                                {:status   :pending
                                 :id       transaction-batch-id
                                 :chain-id chain-id}))
                       {}
                       value1)))
             {}
             transaction-hashes))

(defn calculate-gas-fee
  [data]
  (let [gas-amount         (money/bignumber (get data :gas-amount))
        gas-fees           (get data :gas-fees)
        eip1559-enabled?   (get gas-fees :eip-1559-enabled)
        optimal-price-gwei (money/bignumber (if eip1559-enabled?
                                              (get gas-fees :max-fee-per-gas-medium)
                                              (get gas-fees :gas-price)))
        total-gas-fee-wei  (money/mul (money/->wei :gwei optimal-price-gwei) gas-amount)
        l1-fee-wei         (money/->wei :gwei (get gas-fees :l-1-gas-fee))]
    (money/add total-gas-fee-wei l1-fee-wei)))

(defn calculate-full-route-gas-fee
  "Sums all the routes fees in wei and then convert the total value to ether"
  [route]
  (money/wei->ether (reduce money/add (map calculate-gas-fee route))))

(defn network-amounts-by-chain
  [{:keys [route token-decimals native-token? receiver?]}]
  (reduce
   (fn [acc path]
     (let [amount-hex   (if receiver? (:amount-in path) (:amount-out path))
           amount-units (native-module/hex-to-number
                         (utils.hex/normalize-hex amount-hex))
           amount       (money/with-precision
                         (if native-token?
                           (money/wei->ether amount-units)
                           (money/token->unit amount-units
                                              token-decimals))
                         6)
           chain-id     (if receiver? (get-in path [:to :chain-id]) (get-in path [:from :chain-id]))]
       (update acc chain-id money/add amount)))
   {}
   route))

(defn network-values-for-ui
  [amounts]
  (reduce-kv (fn [acc k v]
               (assoc acc k (if (money/equal-to v 0) "<0.01" v)))
             {}
             amounts))

(defn token-available-networks-for-suggested-routes
  [{:keys [balances-per-chain disabled-chain-ids]}]
  (let [disabled-set (set disabled-chain-ids)]
    (->> balances-per-chain
         (filter (fn [[_ {:keys [chain-id]}]]
                   (not (contains? disabled-set chain-id))))
         (map first))))

(def ^:private network-priority-score
  {:ethereum 1
   :optimism 2
   :arbitrum 3})

(def ^:private available-networks-count
  (count (set (keys network-priority-score))))

(defn reset-network-amounts-to-zero
  [network-amounts]
  (map
   (fn [network-amount]
     (cond-> network-amount
       (= (:type network-amount) :loading)
       (assoc :total-amount (money/bignumber "0")
              :type         :default)))
   network-amounts))

(defn network-amounts
  [{:keys [network-values disabled-chain-ids receiver-networks token-networks-ids receiver?]}]
  (let [disabled-set                             (set disabled-chain-ids)
        receiver-networks-set                    (set receiver-networks)
        network-values-keys                      (set (keys network-values))
        routes-found?                            (pos? (count network-values-keys))
        token-networks-ids-set                   (set token-networks-ids)
        not-available-networks                   (if receiver?
                                                   (filter #(not (token-networks-ids-set %))
                                                           receiver-networks)
                                                   [])
        not-available-networks-set               (set not-available-networks)
        network-values-with-disabled-chains      (when routes-found?
                                                   (reduce
                                                    (fn [acc k]
                                                      (if (or (contains? network-values-keys k)
                                                              (and receiver?
                                                                   (not (contains? receiver-networks-set
                                                                                   k))))
                                                        acc
                                                        (assoc acc k (money/bignumber "0"))))
                                                    network-values
                                                    disabled-chain-ids))
        network-values-with-not-available-chains (if (and receiver? routes-found?)
                                                   (let [network-values-keys
                                                         (set (keys
                                                               network-values-with-disabled-chains))]
                                                     (reduce
                                                      (fn [acc k]
                                                        (if (not (contains? network-values-keys k))
                                                          (assoc acc k nil)
                                                          acc))
                                                      network-values-with-disabled-chains
                                                      not-available-networks))
                                                   network-values-with-disabled-chains)]
    (cond-> (->>
              network-values-with-not-available-chains
              (map
               (fn [[chain-id amount]]
                 {:chain-id     chain-id
                  :total-amount amount
                  :type         (cond
                                  (contains? not-available-networks-set chain-id)         :not-available
                                  (or receiver? (not (contains? disabled-set chain-id)))  :default
                                  (and (not receiver?) (contains? disabled-set chain-id)) :disabled)}))
              (sort-by (fn [network-amount]
                         (get network-priority-score
                              (network-utils/id->network (:chain-id network-amount)))))
              (filter
               (fn [network-amount]
                 (or (and receiver?
                          (or (contains? receiver-networks-set (:chain-id network-amount))
                              (money/greater-than (:total-amount network-amount) (money/bignumber "0"))))
                     (not receiver?))))
              (vec))
      (and receiver?
           routes-found?
           (< (count network-values-with-not-available-chains) available-networks-count))
      (conj {:type :add}))))

(defn loading-network-amounts
  [{:keys [valid-networks disabled-chain-ids receiver-networks token-networks-ids receiver?]}]
  (let [disabled-set               (set disabled-chain-ids)
        receiver-networks-set      (set receiver-networks)
        receiver-networks-count    (count receiver-networks)
        token-networks-ids-set     (set token-networks-ids)
        valid-networks-set         (set valid-networks)
        not-available-networks     (if receiver?
                                     (filter #(not (token-networks-ids-set %)) receiver-networks)
                                     [])
        not-available-networks-set (set not-available-networks)
        valid-networks             (concat valid-networks
                                           disabled-chain-ids
                                           (when receiver?
                                             (filter #(not (valid-networks-set %))
                                                     not-available-networks)))]
    (cond-> (->> valid-networks
                 (map
                  (fn [chain-id]
                    (cond->
                      {:chain-id chain-id
                       :type     (cond
                                   (contains? not-available-networks-set chain-id)         :not-available
                                   (or receiver?
                                       (not (contains? disabled-set chain-id)))            :loading
                                   (and (not receiver?) (contains? disabled-set chain-id)) :disabled)}
                      (and (not receiver?) (contains? disabled-set chain-id))
                      (assoc :total-amount (money/bignumber "0")))))
                 (sort-by (fn [network-amount]
                            (get network-priority-score
                                 (network-utils/id->network (:chain-id network-amount)))))
                 (filter
                  (fn [network-amount]
                    (or (and receiver? (contains? receiver-networks-set (:chain-id network-amount)))
                        (and (not receiver?)
                             (not (contains? disabled-chain-ids (:chain-id network-amount)))))))
                 (vec))
      (and receiver? (< receiver-networks-count available-networks-count)) (conj {:type :add}))))

(defn network-links
  [route from-values-by-chain to-values-by-chain]
  (reduce (fn [acc path]
            (let [from-chain-id       (get-in path [:from :chain-id])
                  to-chain-id         (get-in path [:to :chain-id])
                  from-chain-id-index (first (keep-indexed #(when (= from-chain-id (:chain-id %2)) %1)
                                                           from-values-by-chain))
                  to-chain-id-index   (first (keep-indexed #(when (= to-chain-id (:chain-id %2)) %1)
                                                           to-values-by-chain))
                  position-diff       (- from-chain-id-index to-chain-id-index)]
              (conj acc
                    {:from-chain-id from-chain-id
                     :to-chain-id   to-chain-id
                     :position-diff position-diff})))
          []
          route))
