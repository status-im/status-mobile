(ns quo2.components.inputs.locked-input.style
  (:require [quo2.foundations.colors :as colors]))

(defn info-box-container
  [theme]
  {:flex-direction     :row
   :border-radius      12
   :align-items        :center
   :background-color   (colors/theme-colors colors/neutral-10
                                            colors/neutral-80-opa-80
                                            theme)
   :height             40
   :padding-horizontal 12
   :padding-vertical   9
   :margin-top         4})

(defn info-box-label
  [theme]
  {:color       (colors/theme-colors colors/black colors/white theme)
   :margin-left 8})
