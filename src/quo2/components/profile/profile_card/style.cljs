(ns quo2.components.profile.profile-card.style
  (:require [quo2.foundations.colors :as colors]))

(defn card-container
  [{:keys [customization-color padding-bottom border-bottom-radius]}]
  {:padding-horizontal         12
   :padding-top                12
   :padding-bottom             padding-bottom
   :flex                       1
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-left-radius  border-bottom-radius
   :border-bottom-right-radius border-bottom-radius
   :background-color           (colors/custom-color customization-color 50 40)})

(def card-header
  {:flex-direction  :row
   :justify-content :space-between})

(def name-container
  {:flex-direction :row
   :margin-top     8
   :align-items    :center
   :padding-right  12})

(def user-name
  {:color colors/white})

(def emoji-hash
  {:margin-top     12
   :letter-spacing 1.5})

(def user-hash
  {:margin-top 2
   :color      colors/white-opa-60})

(def keycard-icon
  {:margin-left 4
   :color       colors/white-opa-40})

(def option-button
  {:background-color colors/white-opa-5
   :margin-left      8})
