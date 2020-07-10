(ns status-im.ui.screens.keycard.pin.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :keycard/original-pin
 (fn [db]
   (get-in db [:keycard :pin :original])))

(re-frame/reg-sub
 :keycard/login-pin
 (fn [db]
   (get-in db [:keycard :pin :login])))

(re-frame/reg-sub
 :keycard/pin-confirmation
 (fn [db]
   (get-in db [:keycard :pin :confirmation])))

(re-frame/reg-sub
 :keycard/pin-enter-step
 (fn [db]
   (get-in db [:keycard :pin :enter-step])))

(re-frame/reg-sub
 :keycard/pin-operation
 (fn [db]
   (get-in db [:keycard :pin :operation])))

(re-frame/reg-sub
 :keycard/pin-data
 (fn [db]
   (get-in db [:keycard :pin])))

(re-frame/reg-sub
 :keycard/pin
 :<- [:keycard/pin-data]
 :<- [:keycard/pin-enter-step]
 (fn [[pin-data step]]
   (get pin-data step)))

(re-frame/reg-sub
 :keycard/pin-status
 (fn [db]
   (get-in db [:keycard :pin :status])))

(re-frame/reg-sub
 :keycard/pin-error-label
 (fn [db]
   (get-in db [:keycard :pin :error-label])))

(re-frame/reg-sub
 :keycard/frozen-card?
 (fn [db]
   (let [{:keys [pin-retry-counter]}
         (get-in db [:keycard :application-info])]
     (zero? pin-retry-counter))))
