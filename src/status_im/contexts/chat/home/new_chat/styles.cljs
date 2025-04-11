(ns status-im.contexts.chat.home.new-chat.styles
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(def contact-selection-heading
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :flex-end
   :margin-top      24
   :margin-bottom   16})

(defn chat-button-container
  [theme]
  {:position           :absolute
   :bottom             0
   :padding-bottom     33
   :background-color   (colors/theme-colors colors/white-opa-70 colors/neutral-95-opa-70 theme)
   :padding-top        12
   :padding-horizontal 20
   :left               0
   :right              0})

(defn no-contacts
  []
  {:margin-bottom   (+ 96 safe-area/bottom)
   :flex            1
   :justify-content :center
   :align-items     :center})
