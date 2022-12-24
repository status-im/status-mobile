(ns status-im2.subs.toasts
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :toasts/toast
 :<- [:toasts]
 (fn [toasts [_ toast-id]]
   (get-in toasts [:toasts toast-id])))
