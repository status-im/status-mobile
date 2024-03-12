(ns status-im.subs.settings
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :settings/change-password-current-step
 :<- [:settings/change-password]
 (fn [change-password]
   (get change-password :current-step)))

(rf/reg-sub
 :settings/change-password-loading
 :<- [:settings/change-password]
 (fn [change-password]
   (get change-password :loading?)))

(rf/reg-sub
 :settings/change-password-error
 :<- [:settings/change-password]
 (fn [change-password]
   (get change-password :verify-error)))
