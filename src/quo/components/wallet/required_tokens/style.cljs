(ns quo.components.wallet.required-tokens.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:align-items    :center
   :flex-direction :row})

(def collectible-img
  {:width         14
   :height        14
   :border-radius 4})

(defn divider
  [theme]
  {:width            2
   :height           2
   :border-radius    1
   :margin-left      8
   :background-color (colors/theme-colors colors/neutral-40
                                          colors/neutral-50
                                          theme)})
