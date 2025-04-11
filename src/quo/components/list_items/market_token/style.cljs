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

(def left-side
  {:flex-direction :row
   :align-items    :center
   :flex           1})

(def right-side
  {:align-items     :flex-end
   :justify-content :space-between})

(def percentage-container
  {:flex-direction :row
   :align-items    :center})

(def token-name
  {:weight :semi-bold})

(defn token-short-name
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def arrow
  {:margin-left 4})

(def left-text-block
  {:margin-left 8})

(defn market-cap
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

