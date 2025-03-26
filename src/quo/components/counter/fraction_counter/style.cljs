(ns quo.components.counter.fraction-counter.style
  (:require [quo.foundations.colors :as colors]))

(def counter
  {:padding-top    12
   :padding-bottom 2})

(defn counter-text
  [error? blur? theme]
  {:color (cond
            error? (colors/theme-colors colors/danger-50 colors/danger-60 theme)
            blur?  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
            :else  (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))})
