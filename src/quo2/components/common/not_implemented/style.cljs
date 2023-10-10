(ns quo2.components.common.not-implemented.style
  (:require [quo2.foundations.colors :as colors]))

(defn text
  [blur? theme]
  {:border-color :red
   :border-width 1
   :color        (if blur?
                   colors/white
                   (colors/theme-colors colors/neutral-100
                                        colors/white
                                        theme))})
