(ns quo.components.banners.alert-banner.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [container-style]
  (merge
   {:flex-direction     :row
    :align-items        :center
    :height             50
    :padding-horizontal 20
    :padding-vertical   12}
   container-style))

(defn label
  [theme]
  {:flex              1
   :color             (colors/resolve-color :danger theme)
   :margin-horizontal 4})

(def button-text
  {:color colors/white})
