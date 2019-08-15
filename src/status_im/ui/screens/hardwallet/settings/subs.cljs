(ns status-im.ui.screens.hardwallet.settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.hardwallet.core :as core]
            [status-im.utils.datetime :as utils.datetime]))

(re-frame/reg-sub
 :keycard-paired-on
 (fn [db]
   (some-> (get-in db [:multiaccount :keycard-paired-on])
           (utils.datetime/timestamp->year-month-day-date))))

(re-frame/reg-sub
 :keycard-pairing
 (fn [db]
   (core/get-pairing db)))

(re-frame/reg-sub
 :keycard-multiaccount-pairing
 (fn [db]
   (get-in db [:multiaccount :keycard-pairing])))

(re-frame/reg-sub
 :hardwallet/pin-retry-counter
 (fn [db]
   (get-in db [:hardwallet :application-info :pin-retry-counter])))

(re-frame/reg-sub
 :hardwallet/puk-retry-counter
 (fn [db]
   (get-in db [:hardwallet :application-info :puk-retry-counter])))

(re-frame/reg-sub
 :keycard-reset-card-disabled?
 (fn [db]
   (get-in db [:hardwallet :reset-card :disabled?] false)))

(re-frame/reg-sub
 :keycard-multiaccount?
 (fn [db]
   (boolean
    (get-in db [:multiaccount :keycard-key-uid]))))
