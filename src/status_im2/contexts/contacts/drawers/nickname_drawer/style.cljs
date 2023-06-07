(ns status-im2.contexts.contacts.drawers.nickname-drawer.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn context-container
  []
  {:flex-direction   :row
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
   :border-radius    20
   :align-items      :center
   :align-self       :flex-start
   :padding          4
   :margin-top       8
   :margin-left      -4
   :margin-bottom    16})

(def buttons-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      20})

(defn nickname-container
  [insets]
  {:margin-horizontal 20
   :margin-bottom     (when platform/ios? (:bottom insets))})

(defn nickname-description
  []
  {:margin-left 4
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40)})

(def nickname-description-container
  {:flex-direction :row
   :align-items    :center
   :margin-top     8})
