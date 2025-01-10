(ns status-im.contexts.wallet.send.utils
  (:require
    [status-im.constants :as constants]
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

(defn- path-amount-in
  [path]
  (-> path :amount-in money/from-hex))

(defn- path-amount-out
  [path]
  (if (= (:bridge-name path) constants/bridge-name-hop)
    (let [{:keys [token-fees amount-in]} path]
      (-> amount-in
          money/from-hex
          (money/sub token-fees)))
    (-> path :amount-out money/from-hex)))

(defn convert-wei-to-eth
  [amount native-token? token-decimals]
  (money/with-precision
   (if native-token?
     (money/wei->ether amount)
     (money/token->unit amount token-decimals))
   constants/token-display-precision))

(defn network-amounts-by-chain
  [{:keys [route token-decimals native-token? receiver?]}]
  (reduce
   (fn [acc path]
     (let [amount   (if receiver?
                      (path-amount-out path)
                      (path-amount-in path))
           chain-id (if receiver?
                      (get-in path [:to :chain-id])
                      (get-in path [:from :chain-id]))]
       (as-> amount $
         (convert-wei-to-eth $ native-token? token-decimals)
         (update acc chain-id money/add $))))
   {}
   route))

(defn path-estimated-received
  "Calculates the (`bignumber`) estimated received token amount. For
  bridge transactions, the amount is the difference between the
  `amount-in` and the `token-fees`."
  [path]
  (if (-> path :bridge-name (= constants/bridge-name-hop))
    (-> path
        :amount-in
        money/from-hex
        (money/sub (:token-fees path)))
    (-> path
        :amount-out
        money/from-hex)))

(defn estimated-received-by-chain
  [route token-decimals native-token?]
  (reduce
   (fn [acc path]
     (let [chain-id           (get-in path [:to :chain-id])
           estimated-received (-> path
                                  path-estimated-received
                                  (convert-wei-to-eth native-token? token-decimals))]
       (update acc chain-id money/add estimated-received)))
   {}
   route))

(defn network-values-for-ui
  [amounts]
  (reduce-kv (fn [acc k v]
               (assoc acc k (if (money/equal-to v 0) "<0.01" v)))
             {}
             amounts))

(defn add-zero-values-to-network-values
  [network-values all-possible-chain-ids]
  (reduce
   (fn [acc chain-id]
     (let [route-value (get network-values chain-id)]
       (assoc acc chain-id (or route-value (money/bignumber "0")))))
   {}
   all-possible-chain-ids))

(defn token-available-networks-for-suggested-routes
  [{:keys [balances-per-chain disabled-chain-ids only-with-balance?]}]
  (let [disabled-set (set disabled-chain-ids)]
    (->> balances-per-chain
         (filter (fn [[_ {:keys [chain-id raw-balance]}]]
                   (and (not (contains? disabled-set chain-id))
                        (or (not only-with-balance?)
                            (and only-with-balance?
                                 (money/bignumber? raw-balance)
                                 (money/above-zero? raw-balance))))))
         (map first))))

(def ^:private network-priority-score
  {:ethereum 1
   :optimism 2
   :arbitrum 3})

(defn reset-loading-network-amounts-to-zero
  [network-amounts]
  (mapv
   (fn [network-amount]
     (cond-> network-amount
       (= (:type network-amount) :loading)
       (assoc :total-amount (money/bignumber "0")
              :type         :default)))
   network-amounts))

(defn reset-network-amounts-to-zero
  [network-amounts]
  (map
   (fn [network-amount]
     (assoc network-amount
            :total-amount (money/bignumber "0")
            :type         :default))
   network-amounts))

(defn network-amounts
  [network-values]
  (->> network-values
       (map
        (fn [[chain-id amount]]
          {:chain-id     chain-id
           :total-amount amount
           :type         :default}))
       (sort-by (fn [network-amount]
                  (get network-priority-score
                       (network-utils/id->network (:chain-id network-amount)))))
       (vec)))

(defn loading-network-amounts
  [{:keys [networks values receiver?]}]
  (->> networks
       (map
        (fn [chain-id]
          (let [network-value (when values (get values chain-id))]
            (cond-> {:chain-id chain-id
                     :type     (if network-value :default :loading)}
              network-value                             (assoc :total-amount
                                                               (money/bignumber network-value))
              (and (not network-value) (not receiver?)) (assoc :total-amount (money/bignumber "0"))))))
       (vec)))

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

(def ^:private collectible-tx-set
  #{:tx/collectible-erc-721
    :tx/collectible-erc-1155})

(defn tx-type-collectible?
  [tx-type]
  (contains? collectible-tx-set tx-type))

(defn convert-to-gwei
  [n precision]
  (-> n
      money/wei->gwei
      (money/with-precision precision)
      (str)))

(defn bridge-disabled?
  [token-symbol]
  (not (constants/bridge-assets token-symbol)))

(defn signature-rsv
  [signature]
  {:r (subs signature 0 64)
   :s (subs signature 64 128)
   :v (subs signature 128 130)})
