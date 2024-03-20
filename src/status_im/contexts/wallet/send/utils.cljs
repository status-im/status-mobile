(ns status-im.contexts.wallet.send.utils
  (:require [utils.money :as money]))

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
