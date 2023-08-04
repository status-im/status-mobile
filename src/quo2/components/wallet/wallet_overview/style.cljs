(ns quo2.components.wallet.wallet-overview.style
  (:require [quo2.foundations.colors :as colors]))

(def container-info
  {:flex               1
   :padding-horizontal 20
   :padding-top        12
   :padding-bottom     32})

(def container-info-top
  {:flex-direction  :row
   :flex            1
   :justify-content :space-between
   :align-items     :center})

(def network-dropdown
  {:border-color  colors/neutral-50
   :border-width  1
   :border-radius 10
   :width         68
   :height        32})

(defn color-metrics
  [metrics]
  (if (= metrics :positive) colors/success-50 colors/danger-50))

(defn color-text-heading
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn color-text-paragraph
  [theme]
  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme))

(defn style-text-heading
  [theme]
  {:color (color-text-heading theme)})

(defn style-text-paragraph
  [theme]
  {:color (color-text-paragraph theme)})

(defn dot-separator
  [metrics]
  {:background-color  (color-metrics metrics)
   :margin-horizontal 4
   :width             2
   :height            2})

(defn loading-bar
  [width height margin-right theme]
  {:width            width
   :height           height
   :border-radius    6
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
   :margin-right     margin-right})
