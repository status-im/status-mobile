(ns quo.components.avatars.wallet-user-avatar.style
  (:require [quo.foundations.colors :as colors]))

(defn- circle-color
  [customization-color neutral? theme]
  (if neutral?
    (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
    (colors/resolve-color customization-color theme 20)))

(defn- text-color
  [customization-color neutral? theme]
  (if neutral?
    (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    (colors/resolve-color customization-color theme)))

(defn container
  [circle-size customization-color neutral? theme]
  {:width            circle-size
   :height           circle-size
   :border-radius    circle-size
   :text-align       :center
   :justify-content  :center
   :align-items      :center
   :background-color (circle-color customization-color neutral? theme)})

(defn text
  [customization-color neutral? theme]
  {:color (text-color customization-color neutral? theme)})
