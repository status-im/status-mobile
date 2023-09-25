(ns status-im2.contexts.onboarding.common.overlay.events
  (:require [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.common.overlay.view :as overlay]
            [status-im2.contexts.profile.profiles.view :as profiles]
            [re-frame.core :as re-frame]))

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
