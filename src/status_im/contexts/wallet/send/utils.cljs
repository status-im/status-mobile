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
  [{:keys [route token-decimals native-token? to?]}]
  (reduce (fn [acc path]
            (let [amount-hex   (if to? (:amount-in path) (:amount-out path))
                  amount-units (native-module/hex-to-number
                                (utils.hex/normalize-hex amount-hex))
                  amount       (money/with-precision
                                (if native-token?
                                  (money/wei->ether amount-units)
                                  (money/token->unit amount-units
                                                     token-decimals))
                                6)
                  chain-id     (if to? (get-in path [:to :chain-id]) (get-in path [:from :chain-id]))]
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
  [network-values disabled-chain-ids receiver-networks to?]
  (let [disabled-set           (set disabled-chain-ids)
        receiver-networks-set  (set receiver-networks)
        network-values-keys    (set (keys network-values))
        routes-found?          (pos? (count network-values-keys))
        updated-network-values (when routes-found?
                                 (reduce (fn [acc k]
                                           (if (or (contains? network-values-keys k)
                                                   (and to?
                                                        (not (contains? receiver-networks-set k))))
                                             acc
                                             (assoc acc k (money/bignumber "0"))))
                                         network-values
                                         disabled-chain-ids))]
    (cond-> (->> updated-network-values
                 (map
                  (fn [[k v]]
                    {:chain-id     k
                     :total-amount v
                     :type         (if (or to? (not (contains? disabled-set k))) :default :disabled)}))
                 (sort-by #(get network-priority-score (network-utils/id->network (:chain-id %))))
                 (filter
                  #(or (and to?
                            (or (contains? receiver-networks-set (:chain-id %))
                                (money/greater-than (:total-amount %) (money/bignumber "0"))))
                       (not to?)))
                 (vec))
      (and to?
           routes-found?
           (< (count updated-network-values) available-networks-count))
      (conj {:type :add}))))

(defn loading-network-amounts
  [valid-networks disabled-chain-ids receiver-networks to?]
  (let [disabled-set            (set disabled-chain-ids)
        receiver-networks-set   (set receiver-networks)
        receiver-networks-count (count receiver-networks)
        valid-networks          (concat valid-networks disabled-chain-ids)]
    (cond-> (->> valid-networks
                 (map (fn [k]
                        (cond-> {:chain-id k
                                 :type     (if (or to?
                                                   (not (contains? disabled-set k)))
                                             :loading
                                             :disabled)}
                          (and (not to?) (contains? disabled-set k))
                          (assoc :total-amount (money/bignumber "0")))))
                 (sort-by (fn [item]
                            (get network-priority-score
                                 (network-utils/id->network (:chain-id item)))))
                 (filter
                  #(or (and to? (contains? receiver-networks-set (:chain-id %)))
                       (and (not to?)
                            (not (contains? disabled-chain-ids (:chain-id %))))))
                 (vec))
      (and to? (< receiver-networks-count available-networks-count)) (conj {:type :add}))))

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
