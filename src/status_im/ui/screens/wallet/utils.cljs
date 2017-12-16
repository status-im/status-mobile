(ns status-im.ui.screens.wallet.utils
  (:require [status-im.utils.money :as money]))

(defn format-amount [amount decimals]
  (-> amount
      (or (money/bignumber 0))
      (money/token->unit decimals)
      money/to-fixed))