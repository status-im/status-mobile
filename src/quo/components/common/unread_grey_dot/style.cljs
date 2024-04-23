(ns quo.components.common.unread-grey-dot.style
  (:require
    [quo.foundations.colors :as colors]))

(defn unread-grey-dot
  [theme]
  {:width            8
   :height           8
   :border-radius    4
   :background-color (colors/theme-colors
                      colors/neutral-40
                      colors/neutral-60
                      theme)})
