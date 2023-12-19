(ns status-im.subs.onboarding
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :onboarding/customization-color
 :<- [:onboarding/profile]
 :<- [:profile/customization-color]
 :<- [:onboarding/new-account?]
 (fn [[{:keys [color]} customization-color new-account?]]
   (if new-account?
     color
     customization-color)))
