(ns quo2.components.selectors.reactions.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [pressed?]
  {:padding          10
   :border-radius    12
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80)
   :background-color (when pressed?
                       (colors/theme-colors colors/neutral-10 colors/neutral-80-opa-40))})
