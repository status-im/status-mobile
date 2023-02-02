(ns quo2.components.style
  (:require [quo2.foundations.colors :as colors]))

(def unread-grey-dot
  {:width            8
   :height           8
   :border-radius    4
   :background-color (colors/theme-colors
                      colors/neutral-40
                      colors/neutral-60)})
