(ns quo.components.selectors.reactions-selector.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [pressed? theme]
  {:padding          10
   :border-radius    12
   :border-width     1
   :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :background-color (when pressed?
                       (colors/theme-colors colors/neutral-10 colors/neutral-80-opa-40 theme))})
