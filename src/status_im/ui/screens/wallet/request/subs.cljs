(ns status-im.ui.screens.wallet.request.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.tokens :as tokens]))

(re-frame/reg-sub
  :wallet.request/request-enabled?
  :<- [:get-in [:wallet/request-transaction :amount]]
  :<- [:get-in [:wallet/request-transaction :amount-error]]
  :<- [:get-in [:wallet/request-transaction :symbol]]
  (fn [[amount amount-error symbol]]
    (and
      (or (nil? symbol) (tokens/ethereum? symbol))
      (nil? amount-error)
      (not (nil? amount)))))