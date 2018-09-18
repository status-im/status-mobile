(ns status-im.ui.screens.hardwallet.connect.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :hardwallet/nfc-enabled?
 (fn [db]
   (get-in db [:hardwallet :nfc-enabled?])))