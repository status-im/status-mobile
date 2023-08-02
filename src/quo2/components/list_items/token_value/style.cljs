(ns quo2.components.list-items.token-value.style
  (:require [quo2.foundations.colors :as colors]))


(defn container
  [color bg-opacity]
  {:width              359
   :height             56
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :flex-direction     :row
   :justify-content    :space-between
   :background-color   (colors/custom-color-by-theme color 50 50 bg-opacity bg-opacity)})
