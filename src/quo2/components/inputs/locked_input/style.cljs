(ns quo2.components.inputs.locked-input.style
  (:require [quo2.foundations.colors :as colors]))

(def info-box-container
  {:flex-direction     :row
   :border-radius      12
   :align-items        :center
   :background-color   (colors/theme-colors colors/neutral-80-opa-80 colors/neutral-10)
   :width              :100%
   :height             40
   :padding-horizontal 12
   :padding-vertical   9
   :gap                8
   :margin-top         5})

(def info-box-label
  {:font-size   15
   :color       (colors/theme-colors colors/white colors/black)
   :margin-left 5})
