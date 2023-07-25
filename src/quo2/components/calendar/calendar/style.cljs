(ns quo2.components.calendar.calendar.style
  (:require
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]))

(defn container
  []
  {:flex-direction   :row
   :height           270
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80)
   :border-radius    12
   :border-width     1
   :background-color (colors/theme-colors colors/white colors/neutral-80-opa-40)})

(def container-main
  {:flex-grow 1})
