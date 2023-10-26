(ns quo.components.list-items.token-network.style
  (:require [quo.foundations.colors :as colors]))

(defn- background-color
  [state customization-color theme]
  (cond
    (= state :pressed)  (colors/resolve-color customization-color theme 5)
    (= state :active)   (colors/resolve-color customization-color theme 10)
    (= state :selected) (colors/resolve-color customization-color theme 5)
    :else               :transparent))

(defn container
  [state customization-color theme]
  {:flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :height             56
   :background-color   (background-color state customization-color theme)})

(defn check-color
  [customization-color theme]
  (colors/resolve-color customization-color theme))

(def info
  {:flex-direction :row
   :align-items    :center})

(def token-info
  {:height 40})

(def token-image
  {:width         32
   :height        32
   :border-width  1
   :border-radius 16
   :border-color  colors/neutral-80-opa-5
   :margin-right  8})

(def values-container
  {:align-items :flex-end})

(defn fiat-value
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
