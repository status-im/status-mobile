(ns status-im.ui.screens.hardwallet.pin.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :hardwallet/original-pin
 (fn [db]
   (get-in db [:hardwallet :pin :original])))

(re-frame/reg-sub
 :hardwallet/login-pin
 (fn [db]
   (get-in db [:hardwallet :pin :login])))

(re-frame/reg-sub
 :hardwallet/pin-confirmation
 (fn [db]
   (get-in db [:hardwallet :pin :confirmation])))

(re-frame/reg-sub
 :hardwallet/pin-enter-step
 (fn [db]
   (get-in db [:hardwallet :pin :enter-step] :original)))

(re-frame/reg-sub
 :hardwallet/pin-operation
 (fn [db]
   (get-in db [:hardwallet :pin :operation])))

(re-frame/reg-sub
 :hardwallet/pin-data
 (fn [db]
   (get-in db [:hardwallet :pin])))

(re-frame/reg-sub
 :hardwallet/pin
 :<- [:hardwallet/pin-data]
 :<- [:hardwallet/pin-enter-step]
 (fn [[pin-data step]]
   (get pin-data step)))

(re-frame/reg-sub
 :hardwallet/pin-status
 (fn [db]
   (get-in db [:hardwallet :pin :status])))

(re-frame/reg-sub
 :hardwallet/pin-error-label
 (fn [db]
   (get-in db [:hardwallet :pin :error-label])))
