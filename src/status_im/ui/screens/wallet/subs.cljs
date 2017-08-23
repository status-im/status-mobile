(ns status-im.ui.screens.wallet.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.money :as money]))

(reg-sub :balance
  (fn [db]
    (get-in db [:wallet :balance])))

(reg-sub :price
  (fn [db]
    (get-in db [:prices :price])))

(reg-sub :last-day
  (fn [db]
    (get-in db [:prices :last-day])))

(reg-sub :eth-balance
  :<- [:balance]
  (fn [balance]
    (if balance
      (money/wei->ether balance)
      "...")))

(reg-sub :portfolio-value
  :<- [:balance]
  :<- [:price]
  (fn [[balance price]]
    (if (and balance price)
      (-> (money/wei->ether balance)
          (money/eth->usd price)
          (money/with-precision 2)
          str)
      "...")))

(reg-sub :portfolio-change
  :<- [:price]
  :<- [:last-day]
  (fn [[price last-day]]
    (if (and price last-day)
      (-> (money/percent-change price last-day)
          (money/with-precision 2)
          (str "%"))
      "-%")))

(reg-sub :wallet/transactions
  (fn [db]
    (get-in db [:wallet :transactions])))

(reg-sub :wallet/unsigned-transactions
  :<- [:wallet/transactions]
  (fn [transactions]
    (filter #(= (:state %) :unsigned) transactions)))

(reg-sub :wallet/pending-transactions
  :<- [:wallet/transactions]
  (fn [transactions]
    (filter #(= (:state %) :pending) transactions)))

(reg-sub :wallet/postponed-transactions
  :<- [:wallet/transactions]
  (fn [transactions]
    (filter #(= (:state %) :postponed) transactions)))

(reg-sub :wallet/sent-transactions
  :<- [:wallet/transactions]
  (fn [transactions]
    (filter #(= (:state %) :sent) transactions)))
