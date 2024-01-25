(ns quo.components.wallet.account-card.properties
  (:require [quo.foundations.colors :as colors]))

(defn gradient-start-color
  [theme customization-color]
  (colors/theme-colors
   (colors/resolve-color customization-color theme 0)
   colors/neutral-95
   theme))

(defn gradient-end-color
  [theme customization-color]
  (colors/theme-colors
   (colors/resolve-color customization-color theme 6)
   colors/neutral-95
   theme))

(defn alert-icon-color
  [theme]
  (colors/theme-colors colors/neutral-50 colors/white theme))
