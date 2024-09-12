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

(def content-container
  {:flex           1
   :flex-direction :row})

(defn label
  [theme]
  {:color             (colors/resolve-color :danger theme)
   :margin-horizontal 4
   :flex              1
   :flex-wrap         :wrap})

(def button-text
  {:color colors/white})

(def icon
  {:margin-top 2})
