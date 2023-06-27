(ns status-im.wallet.utils
  (:require [status-im.utils.money :as money]))

(defn format-amount
  [amount decimals]
  (-> amount
      (or (money/bignumber 0))
      (money/token->unit decimals)
      money/to-fixed))

;;NOTE(goranjovic) - we are internally using symbol ETH for native currencies of any ethereum network
;; some sidechains have different names for this native currency, which we handle with `symbol-display`
;; override.
(defn display-symbol
  [{:keys [symbol-display symbol]}]
  (when-let [name (or symbol-display symbol)]
    (clojure.core/name name)))

;;NOTE(goranjovic) - in addition to custom symbol display, some sidechain native currencies are listed
;;under a different
;; ticker on exchange networks. We handle that with `symbol-exchange` override.
(defn exchange-symbol
  [{:keys [symbol-exchange symbol-display symbol]}]
  (clojure.core/name (or symbol-exchange symbol-display symbol)))
