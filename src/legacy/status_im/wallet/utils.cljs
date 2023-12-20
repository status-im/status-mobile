(ns legacy.status-im.wallet.utils
  (:require
    [utils.money :as money]))

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
  [m]
  (when-let [some-symbol (or (:symbol-display m) (:symbol m))]
    (name some-symbol)))

;;NOTE(goranjovic) - in addition to custom symbol display, some sidechain native currencies are listed
;;under a different
;; ticker on exchange networks. We handle that with `symbol-exchange` override.
(defn exchange-symbol
  [m]
  (name (or (:symbol-exchange m)
            (:symbol-display m)
            (:symbol m))))

(defn get-default-account
  [accounts]
  (some #(when (:wallet %) %) accounts))

(defn default-address
  [db]
  (-> (get db :profile/wallet-accounts)
      get-default-account
      :address))
