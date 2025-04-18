(ns quo.components.common.list.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [blur? theme]
  {:border-radius    16
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/white colors/neutral-95 theme))
   :border-width     (if blur? 0 1)
   :border-color     (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn separator
  [blur? theme]
  {:height           1
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})
