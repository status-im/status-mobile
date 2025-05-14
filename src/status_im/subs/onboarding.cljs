(ns status-im.subs.onboarding
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :onboarding/customization-color
 :<- [:onboarding/profile]
 :<- [:profile/customization-color]
 (fn [[{:keys [color]} customization-color]
      [_sub-id {:keys [onboarding?]}]]
   (if (and onboarding? (some? color))
     color
     customization-color)))
