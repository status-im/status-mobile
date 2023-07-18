(ns quo2.components.inputs.locked-input.style
  (:require [quo2.foundations.colors :as colors]))

(defn info-box-container
  [theme]
  {:flex-direction     :row
   :border-radius      12
   :align-items        :center
   :background-color   (colors/theme-colors colors/neutral-10
                                            colors/neutral-80-opa-80 theme)
   :width              :100%
   :height             40
   :padding-horizontal 12
   :padding-vertical   9
   :gap                8
   :margin-top         2})

(defn info-box-label
  [theme]
  {:font-size   15
   :color       (colors/theme-colors colors/black colors/white theme)
   :margin-left 7})
