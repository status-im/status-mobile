(ns status-im2.contexts.chat.messages.composer.controls.style
  (:require [quo2.foundations.colors :as colors]))

(defn controls
  [insets]
  {:padding-horizontal 20
   :elevation          4
   :z-index            3
   :position           :absolute
   :background-color   (colors/theme-colors colors/white colors/neutral-90)
   :padding-bottom     (+ 12 (:bottom insets))
   :bottom             0
   :left               0
   :right              0})

(def buttons-container
  {:flex-direction :row
   :margin-top     12
   :min-height     32})

(defn record-audio-container
  [insets]
  {:align-items      :center
   :background-color :transparent
   :flex-direction   :row
   :position         :absolute
   :left             0
   :right            0
   :bottom           (- (:bottom insets) 7)})
