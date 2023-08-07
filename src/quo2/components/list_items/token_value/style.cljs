(ns quo2.components.list-items.token-value.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [color bg-opacity theme]
  {:width              359
   :height             56
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :flex-direction     :row
   :justify-content    :space-between
   :background-color   (colors/custom-color-by-theme color 50 50 bg-opacity bg-opacity theme)})

(defn metric-text
  [status theme]
  {:color (case status
            :positive (colors/theme-colors colors/success-50 colors/success-60 theme)
            :negative (colors/theme-colors colors/danger-50 colors/danger-60 theme)
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(defn dot-divider
  [status theme]
  {:width             2
   :height            2
   :border-radius     2
   :margin-horizontal 4
   :background-color  (case status
                        :positive (colors/theme-colors colors/success-50-opa-40
                                                       colors/success-60-opa-40
                                                       theme)
                        :negative (colors/theme-colors colors/danger-50-opa-40
                                                       colors/danger-50-opa-40
                                                       theme)
                        (colors/theme-colors colors/neutral-80-opa-40 colors/neutral-50-opa-40 theme))})

(defn arrow-icon
  [status theme]
  {:size  16
   :color (if (= status :positive)
            (colors/theme-colors colors/success-50 colors/success-60 theme)
            (colors/theme-colors colors/danger-50 colors/danger-60 theme))})
