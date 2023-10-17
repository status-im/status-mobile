(ns quo.components.calendar.calendar.weekdays-header.style
  (:require
    [quo.foundations.colors :as colors]))

(def container-weekday-row
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal 8})

(def container-weekday
  {:width           32
   :height          30
   :padding-top     2
   :justify-content :center
   :align-items     :center})

(defn text-weekdays
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
