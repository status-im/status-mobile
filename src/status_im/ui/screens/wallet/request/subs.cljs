(ns status-im.ui.screens.wallet.request.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :wallet.request/request-enabled?
  :<- [:get-in [:wallet/request-transaction :amount]]
  :<- [:get-in [:wallet/request-transaction :amount-error]]
  (fn [[amount amount-error]]
    (and
      (nil? amount-error)
      (not (nil? amount)))))