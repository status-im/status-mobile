(ns status-im2.contexts.wallet.send.events
  (:require
    [utils.number]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :wallet/select-address-tab
 (fn [{:keys [db]} [tab]]
   {:db (assoc-in db [:wallet :ui :send :select-address-tab] tab)}))

(rf/reg-event-fx :wallet/select-send-account-address
 (fn [{:keys [db]} [address]]
   {:db (assoc db [:wallet :ui :send :send-account-address] address)}))
