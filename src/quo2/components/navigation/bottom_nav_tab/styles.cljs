(ns quo2.components.navigation.bottom-nav-tab.styles
  (:require [quo2.foundations.colors :as colors]))

(defn notification-dot
  [customization-color]
  {:width            8
   :height           8
   :border-radius    4
   :top              6
   :left             51
   :position         :absolute
   :background-color (colors/custom-color customization-color 60)})

(def notification-counter
  {:position :absolute
   :left     48
   :top      2})
