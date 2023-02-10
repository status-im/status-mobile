(ns quo2.components.selectors.disclaimer.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  []
  {:flex-direction   :row
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-80-opa-40)
   :padding          11
   :align-self       :stretch
   :border-radius    12
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-70)})

(def text
  {:margin-left 8})
