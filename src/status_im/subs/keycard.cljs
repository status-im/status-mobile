(ns status-im.subs.keycard
  (:require [utils.re-frame :as rf]))

(rf/reg-sub
 :keycard/keycard-profile?
 (fn [db]
   (not (nil? (get-in db [:profile/profile :keycard-pairing])))))

(rf/reg-sub
 :keycard/nfc-enabled?
 :<- [:keycard]
 (fn [keycard]
   (:nfc-enabled? keycard)))

(rf/reg-sub
 :keycard/connected?
 :<- [:keycard]
 (fn [keycard]
   (:card-connected? keycard)))

(rf/reg-sub
 :keycard/pin
 :<- [:keycard]
 (fn [keycard]
   (:pin keycard)))

(rf/reg-sub
 :keycard/pin-retry-counter
 :<- [:keycard]
 (fn [keycard]
   (get-in keycard [:application-info :pin-retry-counter])))

(rf/reg-sub
 :keycard/connection-sheet-opts
 :<- [:keycard]
 (fn [keycard]
   (:connection-sheet-opts keycard)))

(rf/reg-sub
 :keycard/application-info-error
 :<- [:keycard]
 (fn [keycard]
   (:application-info-error keycard)))

(rf/reg-sub
 :keycard/initialized?
 :<- [:keycard]
 (fn [keycard]
   (get-in keycard [:application-info :initialized?])))
