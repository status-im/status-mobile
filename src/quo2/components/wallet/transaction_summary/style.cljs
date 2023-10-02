(ns quo2.components.wallet.transaction-summary.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [theme]
  {:border-radius  16
   :padding-top    9
   :padding-bottom 8
   :border-color   (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :border-width   1})

(def transaction-header-container
  {:flex-direction     :row
   :align-items        :center
   :margin-bottom      9
   :padding-horizontal 12})

(defn transaction-header
  [theme]
  {:color       (colors/theme-colors colors/neutral-100 colors/white theme)
   :margin-left 4})

(defn prop-text
  [theme]
  {:margin-right 4
   :color        (colors/theme-colors colors/neutral-100 colors/white theme)})

(def prop-tag
  {:margin-right 4})

(def icon-container
  {:width           20
   :height          20
   :align-items     :center
   :justify-content :center})

(def content
  {:margin-bottom      8
   :padding-horizontal 12})

(def content-line
  {:flex-direction :row
   :margin-top     4
   :align-items    :center})

(defn icon-color
  [theme]
  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))

(defn divider
  [theme]
  {:height           1
   :margin-top       4
   :margin-bottom    3
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn extra-info-header
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn extra-info-content
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(def extra-info-container
  {:margin-horizontal 12})

(def extras-container
  {:flex-direction  :row
   :justify-content :space-between
   :margin-top      4})
