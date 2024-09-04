(ns quo.components.list-items.network-list.style
  (:require [quo.foundations.colors :as colors]))

(defn- background-color
  [state customization-color theme]
  (case state
    :pressed (colors/resolve-color customization-color theme 5)
    :active  (colors/resolve-color customization-color theme 10)
    :transparent))

(defn container
  [state customization-color theme]
  {:flex-direction     :row
   :justify-content    :space-between
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   8
   :border-radius      12
   :height             56
   :opacity            (if (= state :disabled) 0.3 1)
   :background-color   (background-color state customization-color theme)})

(def info
  {:flex-direction :row
   :align-items    :center
   :gap            2})

(def network-image
  {:border-width     1
   :border-radius    16
   :border-color     colors/neutral-80-opa-5
   :margin-right     8
   :background-color colors/neutral-80-opa-5
   :height           32
   :width            32})

(def values-container
  {:align-items :flex-end})

(defn fiat-value
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
