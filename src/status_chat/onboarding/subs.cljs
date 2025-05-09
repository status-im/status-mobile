(ns status-chat.onboarding.subs
  (:require [re-frame.core :as rf]
            [status-chat.onboarding.constants :as constants]))

(rf/reg-sub :onboarding/flow
 (fn [db]
   (get db :onboarding/flow)))

(rf/reg-sub :onboarding/step-idx
 (fn [db]
   (get db :onboarding/step-idx)))

(rf/reg-sub :onboarding/replies-by-step
 (fn [db]
   (get db :onboarding/replies-by-step)))

(rf/reg-sub :onboarding/reply-for-step
 :<- [:onboarding/replies-by-step]
 (fn [replies [_ step]]
   (get replies step)))

(rf/reg-sub :onboarding/current-step
 :<- [:onboarding/flow]
 :<- [:onboarding/step-idx]
 (fn [[flow step-idx]]
   (nth flow step-idx)))

(rf/reg-sub :onboarding/steps-list
 :<- [:onboarding/flow]
 :<- [:onboarding/step-idx]
 (fn [[flow step-idx]]
   (some->> step-idx
            inc
            (subvec flow 0)
            reverse)))

(rf/reg-sub :onboarding/color
 :<- [:onboarding/replies-by-step]
 (fn [replies]
   (get-in replies [:color :input-value] constants/default-color)))
