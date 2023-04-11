(ns quo2.components.links.url-preview.style
  (:require [quo2.foundations.colors :as colors]))

(def horizontal-padding 12)

(defn container
  []
  {:height             56
   :background-color   (colors/theme-colors colors/neutral-5 colors/neutral-90)
   :padding-vertical   10
   :padding-horizontal horizontal-padding
   :border-radius      12
   :align-self         :stretch
   :flex-direction     :row})

(defn loading-container
  []
  {:height          56
   :border-width    1
   :border-radius   12
   :border-style    :dashed
   :align-items     :center
   :justify-content :center
   :align-self      :stretch
   :padding         horizontal-padding
   :border-color    (colors/theme-colors colors/neutral-30 colors/neutral-80)})

(defn loading-message
  []
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)})

(def logo
  {:width         16
   :height        16
   :top           1
   :border-radius 8})

(def content-container
  {:margin-left 6
   :flex        1})

(defn title
  []
  {:color (colors/theme-colors colors/neutral-100 colors/white)})

(defn body
  []
  {:text-transform :lowercase
   :color          (colors/theme-colors colors/neutral-50 colors/neutral-40)})

(def clear-button
  {:border-color colors/danger-50
   :border-width 1})

(def clear-button-container
  {:width       20
   :height      20
   :margin-left 6})
