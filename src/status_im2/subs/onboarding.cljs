(ns status-im2.subs.onboarding
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :onboarding-2/customization-color
 :<- [:onboarding-2/profile]
 :<- [:profile/customization-color]
 :<- [:onboarding-2/new-account?]
 (fn [[{:keys [color]} customization-color new-account?]]
   (if new-account?
     color
     customization-color)))
