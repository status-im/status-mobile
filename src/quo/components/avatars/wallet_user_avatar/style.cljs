(ns quo.components.avatars.wallet-user-avatar.style
  (:require [quo.foundations.colors :as colors]))

(defn- circle-color
  [customization-color]
  (colors/custom-color customization-color 50 20))

(defn- text-color
  [customization-color theme]
  (colors/theme-colors
   (colors/custom-color customization-color 50)
   (colors/custom-color customization-color 60)
   theme))

(defn container
  [circle-size customization-color]
  {:width            circle-size
   :height           circle-size
   :border-radius    circle-size
   :text-align       :center
   :justify-content  :center
   :align-items      :center
   :background-color (circle-color customization-color)})

(defn text
  [customization-color theme]
  {:color (text-color customization-color theme)})
