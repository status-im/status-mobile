(ns status-im.ui.screens.hardwallet.pin.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :hardwallet/pin
 (fn [db]
   (get-in db [:hardwallet :pin :original])))

(re-frame/reg-sub
 :hardwallet/pin-confirmation
 (fn [db]
   (get-in db [:hardwallet :pin :confirmation])))

(re-frame/reg-sub
 :hardwallet/pin-enter-step
 (fn [db]
   (get-in db [:hardwallet :pin :enter-step])))

(re-frame/reg-sub
 :hardwallet/pin-status
 (fn [db]
   (get-in db [:hardwallet :pin :status])))

(re-frame/reg-sub
 :hardwallet/pin-error
 (fn [db]
   (get-in db [:hardwallet :pin :error])))
