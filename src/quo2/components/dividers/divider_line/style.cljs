(ns quo2.components.dividers.divider-line.style
  (:require [quo2.foundations.colors :as colors]))

(defn divider-line
  [theme]
  {:border-color        (colors/theme-colors colors/neutral-10 colors/neutral-90 theme)
   :padding-top         12
   :padding-bottom      8
   :border-bottom-width 1})
