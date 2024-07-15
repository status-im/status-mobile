(ns status-im.contexts.onboarding.common.overlay.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.contexts.onboarding.common.overlay.view :as overlay]
    [status-im.contexts.profile.profiles.view :as profiles]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 :onboarding/overlay-dismiss-fx
 (fn []
   (when-let [blur-dismiss-fn @overlay/blur-dismiss-fn-atom]
     (blur-dismiss-fn))
   (when-let [pop-animation-fn @profiles/pop-animation-fn-atom]
     (pop-animation-fn))))

(rf/defn overlay-dismiss
  {:events [:onboarding/overlay-dismiss]}
  [_]
  {:onboarding/overlay-dismiss-fx nil})

(re-frame/reg-fx
 :onboarding/overlay-show-fx
 (fn []
   (when-let [blur-show-fn @overlay/blur-show-fn-atom]
     (blur-show-fn))
   (when-let [push-animation-fn @profiles/push-animation-fn-atom]
     (push-animation-fn))))

(rf/defn overlay-show
  {:events [:onboarding/overlay-show]}
  [_]
  {:onboarding/overlay-show-fx nil})

