(ns quo2.components.list-items.community-list.style
  (:require [quo2.foundations.colors :as colors]))

(defn community-card
  [radius]
  {:shadow-offset    {:width  0
                      :height 2}
   :shadow-radius    radius
   :shadow-opacity   1
   :shadow-color     colors/shadow
   :elevation        1
   :border-radius    radius
   :justify-content  :space-between
   :background-color (colors/theme-colors colors/white colors/neutral-90)})

(def detail-container
  {:flex 1})

(defn list-info-container
  []
  {:flex-direction     :row
   :border-radius      16
   :padding-horizontal 12
   :align-items        :center
   :padding-vertical   8})
