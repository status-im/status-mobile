(ns quo2.components.calendar.calendar.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]))

(defn container
  [theme]
  {:flex-direction   :row
   :height           270
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :border-radius    12
   :border-width     1
   :background-color (colors/theme-colors colors/white colors/neutral-80-opa-40 theme)})

(def container-main
  {:flex-grow 1})
