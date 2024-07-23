(ns quo.components.links.url-preview.style
  (:require
    [quo.foundations.colors :as colors]))

(def horizontal-padding 12)

(defn container
  [theme]
  {:height             56
   :background-color   (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
   :padding-vertical   10
   :padding-horizontal horizontal-padding
   :border-radius      12
   :align-self         :stretch
   :flex-direction     :row})

(defn loading-container
  [theme]
  {:height          56
   :border-width    1
   :border-radius   12
   :border-style    :dashed
   :align-items     :center
   :flex-direction  :row
   :justify-content :center
   :align-self      :stretch
   :padding         horizontal-padding
   :border-color    (colors/theme-colors colors/neutral-30 colors/neutral-80 theme)})

(defn loading-message
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def logo
  {:width         16
   :height        16
   :top           1
   :border-radius 8})

(def content-container
  {:margin-left 6
   :flex        1})

(defn title
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn body
  [theme]
  {:text-transform :lowercase
   :color          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def clear-button-container
  {:width       20
   :height      20
   :margin-left 6})
