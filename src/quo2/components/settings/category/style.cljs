(ns quo2.components.settings.category.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [theme]
  {:left               0
   :right              0
   :background-color   (colors/theme-colors colors/neutral-5 colors/neutral-95 theme)
   :padding-horizontal 20
   :padding-top        12
   :padding-bottom     8})

(defn items
  [theme]
  {:margin-top       12
   :border-radius    12
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})

(defn separator
  [theme]
  {:height           1
   :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)})
