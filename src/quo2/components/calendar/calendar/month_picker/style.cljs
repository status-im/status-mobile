(ns quo2.components.calendar.calendar.month-picker.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:align-items        :center
   :flex-direction     :row
   :flex-grow          1
   :padding-horizontal 12
   :padding-vertical   9
   :justify-content    :space-between})

(defn text
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})
