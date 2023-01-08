(ns quo2.components.profile.profile-card.style
  (:require [quo2.foundations.colors :as colors]))

(defn card-container
  [customization-color]
  {:flex-direction   :column
   :padding          12
   :flex             1
   :border-radius    16
   :background-color (colors/custom-color customization-color 50 40)})

(def card-header
  {:flex-direction  :row
   :justify-content :space-between})

(def name-container
  {:flex-direction :row
   :margin-top     8
   :margin-bottom  2
   :align-items    :center
   :padding-right  12})

(def user-name
  {:margin-right 4
   :color        colors/white})

(def emoji-hash
  {:margin-top 10})

(def user-hash
  {:color colors/white-opa-60})

(def sign-button
  {:margin-top 14})

(def keycard-icon
  {:color colors/white-opa-40})

(def option-button
  {:background-color colors/white-opa-5})
