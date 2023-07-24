(ns quo2.components.calendar.calendar-month.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]))

(def container
  {:align-items        :center
   :flex-direction     :row
   :flex-grow          1
   :padding-horizontal 12
   :padding-vertical   9
   :justify-content    :space-between})

(defn text
  []
  (-> typography/paragraph-1
      (merge typography/font-semi-bold)
      (merge {:color (colors/theme-colors colors/neutral-100 colors/white)})))
