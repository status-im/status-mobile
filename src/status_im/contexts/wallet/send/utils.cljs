(ns status-im.contexts.wallet.send.utils
  (:require
    [legacy.status-im.utils.hex :as utils.hex]
    [native-module.core :as native-module]
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
