(ns status-im.ui.screens.wallet.send.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :camera-dimensions
  (fn [db]
    (get-in db [:wallet/send-transaction :camera-dimensions])))

(re-frame/reg-sub :camera-flashlight
  (fn [db]
    (get-in db [:wallet/send-transaction :camera-flashlight])))

(re-frame/reg-sub
  :wallet.send/sign-enabled?
  :<- [:get-in [:wallet/send-transaction :amount]]
  :<- [:get-in [:wallet/send-transaction :to-address]]
  :<- [:get-in [:wallet/send-transaction :amount-error]]
  (fn [[amount to-address amount-error]]
    (and
      (nil? amount-error)
      (not (nil? to-address)) (not= to-address "")
      (not (nil? amount)) (not= amount ""))))

(re-frame/reg-sub
  :wallet.send/sign-password-enabled?
  :<- [:get-in [:wallet/send-transaction :password]]
  (fn [password]
    (and (not (nil? password)) (not= password ""))))
