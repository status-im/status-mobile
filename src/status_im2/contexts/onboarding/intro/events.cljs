(ns status-im2.contexts.onboarding.intro.events
  (:require [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.common.overlay.view :as overlay]
            [status-im2.contexts.profile.profiles.view :as profiles]))

(rf/defn intro-on-focus
  {:events [:onboarding/intro-on-focus]}
  [_]
  (when @overlay/blur-dismiss-fn-atom
    (@overlay/blur-dismiss-fn-atom))
  (when @profiles/pop-animation-fn-atom
    (@profiles/pop-animation-fn-atom)))
