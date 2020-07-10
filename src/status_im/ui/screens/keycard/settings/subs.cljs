(ns status-im.ui.screens.keycard.settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.keycard.common :as common]
            [status-im.utils.datetime :as utils.datetime]))

(re-frame/reg-sub
 :keycard-paired-on
 (fn [db]
   (some-> (get-in db [:multiaccount :keycard-paired-on])
           (utils.datetime/timestamp->year-month-day-date))))

(re-frame/reg-sub
 :keycard-multiaccount-pairing
 (fn [db]
   (get-in db [:multiaccount :keycard-pairing])))

(re-frame/reg-sub
 :keycard/pin-retry-counter
 (fn [db]
   (get-in db [:keycard :application-info :pin-retry-counter])))

(re-frame/reg-sub
 :keycard/puk-retry-counter
 (fn [db]
   (get-in db [:keycard :application-info :puk-retry-counter])))

(re-frame/reg-sub
 :keycard/retry-counter
 :<- [:keycard/pin-retry-counter]
 :<- [:keycard/puk-retry-counter]
 (fn [[pin puk]]
   (if (zero? pin) puk pin)))

(re-frame/reg-sub
 :keycard-reset-card-disabled?
 (fn [db]
   (get-in db [:keycard :reset-card :disabled?] false)))

(re-frame/reg-sub
 :keycard-multiaccount?
 common/keycard-multiaccount?)
