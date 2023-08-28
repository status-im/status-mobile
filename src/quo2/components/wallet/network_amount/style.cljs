(ns quo2.components.wallet.network-amount.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex-direction :row
   :align-items    :center
   :height         18})

(def text
  {:margin-left  4
   :margin-right 8})

(defn divider
  [theme]
  {:width            1
   :height           8
   :background-color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)})
