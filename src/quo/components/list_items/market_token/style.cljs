(ns quo.components.list-items.market-token.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [color bg-opacity theme]
  {:height             56
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :flex-direction     :row
   :justify-content    :space-between
   :background-color   (colors/resolve-color color theme bg-opacity)})

(defn percentage-text
  [percentage-change theme]
  {:color (if (pos? percentage-change)
            (colors/theme-colors colors/success-50 colors/success-60 theme)
            (colors/theme-colors colors/danger-50 colors/danger-60 theme))})

(defn arrow-icon
  [percentage-change theme]
  {:size  16
   :color (if (pos? percentage-change)
            (colors/resolve-color :success theme)
            (colors/resolve-color :danger theme))})

