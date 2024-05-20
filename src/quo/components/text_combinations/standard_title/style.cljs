(ns quo.components.text-combinations.standard-title.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex-direction  :row
   :flex            1
   :justify-content :space-between})

(def right-counter
  {:padding-top    12
   :padding-bottom 2})

(defn right-counter-text
  [blur? theme]
  {:color (if blur?
            (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
            (colors/theme-colors colors/neutral-40 colors/neutral-50 theme))})

(defn right-tag-icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))
