(ns quo.components.profile.link-card.properties
  (:require [quo.foundations.colors :as colors]))

(defn gradient-start-color
  [theme customization-color]
  (colors/resolve-color customization-color theme 0))

(defn gradient-end-color
  [theme customization-color]
  (colors/resolve-color customization-color theme 6))
