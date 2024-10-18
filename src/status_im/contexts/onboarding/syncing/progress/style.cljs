(ns status-im.contexts.onboarding.syncing.progress.style
  (:require
   [quo.foundations.colors :as colors]
   [react-native.safe-area :as safe-area]))

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn page-container
  [in-onboarding?]
  {:flex             1
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0
   :padding-top      (safe-area/get-top)
   :padding-bottom   20
   :background-color (when-not in-onboarding? colors/neutral-80-opa-80-blur)})

(defn page-illustration
  [width]
  {:flex            1
   :width           width
   :align-items     :center
   :align-self      :center
   :justify-content :center})
