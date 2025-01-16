(ns status-im.contexts.onboarding.syncing.progress.style
  (:require
    [quo.foundations.colors :as colors]))

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
   :padding-bottom   20
   :background-color (when-not in-onboarding? colors/neutral-80-opa-80-blur)})

(defn page-illustration
  [width pairing-progress?]
  {:flex            1
   :width           width
   :align-items     :center
   :margin-vertical (when pairing-progress? 24)
   :align-self      :center
   :justify-content :center})
