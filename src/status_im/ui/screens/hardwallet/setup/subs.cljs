(ns status-im.ui.screens.hardwallet.setup.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :hardwallet-setup-step
 (fn [db]
   (get-in db [:hardwallet :setup-step])))