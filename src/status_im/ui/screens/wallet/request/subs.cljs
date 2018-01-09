(ns status-im.ui.screens.wallet.request.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.tokens :as tokens]))

(re-frame/reg-sub ::request-transaction
  :<- [:wallet]
  :request-transaction)

(re-frame/reg-sub :wallet.request/symbol
  :<- [::request-transaction]
  (fn [transaction]
    (or (:symbol transaction) :ETH)))

(re-frame/reg-sub
  :wallet.request/request-enabled?
  :<- [:get-in [:wallet :request-transaction :amount]]
  :<- [:get-in [:wallet :request-transaction :amount-error]]
  :<- [:wallet.request/symbol]
  (fn [[amount amount-error symbol]]
    (and
      (or (nil? symbol) (tokens/ethereum? symbol))
      (nil? amount-error)
      (not (nil? amount)))))