(ns status-im.ui.screens.keycard.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :keycard/nfc-enabled?
 (fn [db]
   (get-in db [:keycard :nfc-enabled?])))

(re-frame/reg-sub
 :keycard/card-read-in-progress?
 (fn [db]
   (get-in db [:keycard :card-read-in-progress?] false)))
