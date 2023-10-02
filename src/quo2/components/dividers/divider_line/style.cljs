(ns quo2.components.dividers.divider-line.style
  (:require [quo2.foundations.colors :as colors]))

(defn divider-line
  [{:keys [blur? container-style theme]}]
  (merge
   {:border-color        (if blur?
                           (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                           (colors/theme-colors colors/neutral-10 colors/neutral-90 theme))
    :border-bottom-width 1}
   container-style))
