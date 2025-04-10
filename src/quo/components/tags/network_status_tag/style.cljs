(ns quo.components.tags.network-status-tag.style
  (:require
    [quo.foundations.colors :as colors]))

(def main
  {:justify-content :center
   :align-items     :center
   :height          24})

(defn inner
  [theme]
  {:border-width       1
   :border-radius      12
   :flex               1
   :flex-direction     :row
   :border-color       (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :align-items        :center
   :padding-horizontal 8})

(def label
  {:color       colors/warning-50
   :margin-left 6})

(def dot
  {:width            8
   :height           8
   :border-radius    8
   :background-color colors/warning-50})
