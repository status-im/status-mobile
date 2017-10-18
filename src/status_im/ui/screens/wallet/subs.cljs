(ns status-im.ui.screens.wallet.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.money :as money]))

(reg-sub :wallet
  (fn [db]
    (:wallet db)))

(reg-sub :balance
  :<- [:wallet]
  (fn [wallet]
    (:balance wallet)))

(reg-sub :price
  (fn [db]
    (get-in db [:prices :price])))

(reg-sub :last-day
  (fn [db]
    (get-in db [:prices :last-day])))

(reg-sub :wallet/error-message?
  :<- [:wallet]
  (fn [wallet]
    (or (get-in wallet [:errors :balance-update])
        (get-in wallet [:errors :prices-update]))))

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
  :<- [:balance]
  (fn [[price last-day balance]]
    (when (and price last-day)
      (if (> balance 0)
        (-> (money/percent-change price last-day)
            (money/with-precision 2)
            .toNumber)
        0))))

(reg-sub :prices-loading?
  (fn [db]
    (:prices-loading? db)))

(reg-sub :wallet/balance-loading?
  :<- [:wallet]
  (fn [wallet]
    (:balance-loading? wallet)))
