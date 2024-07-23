(ns quo.components.wallet.account-card.properties
  (:require [quo.foundations.colors :as colors]))

(defn gradient-start-color
  [theme customization-color]
  (colors/resolve-color customization-color theme 0))

(defn gradient-end-color
  [theme customization-color]
  (colors/resolve-color customization-color theme 6))

(defn alert-icon-color
  [theme]
  (colors/theme-colors colors/neutral-80-opa-60 colors/white-70-blur theme))
