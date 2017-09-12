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

(reg-sub :wallet/error-message?
  (fn [db]
    (or (get-in db [:wallet :errors :balance-update])
        (get-in db [:wallet :errors :prices-update]))))

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
    (when (and price last-day)
      (-> (money/percent-change price last-day)
          (money/with-precision 2)
          .toNumber))))

(reg-sub :prices-loading?
  (fn [db]
    (:prices-loading? db)))

(reg-sub :wallet/balance-loading?
  (fn [db]
    (get-in db [:wallet :balance-loading?])))
