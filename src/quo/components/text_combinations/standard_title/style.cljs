(ns quo.components.text-combinations.standard-title.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex            1
   :flex-direction  :row
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

(def text
  {:flex          1
   ;; NOTE: assures the ellipses are not cut off when text is too long
   :padding-right 2})
