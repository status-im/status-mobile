(ns status-im.ui.screens.chat.styles.message.datemark
  (:require [quo2.foundations.colors :as quo2.colors]))

(def datemark-mobile
  {:flex        1
   :justify-content :center
   :margin-vertical 16
   :padding-left 65})

(defn divider []
  {:flex 1
   :width "100%"
   :height 1
   :padding-left 53
   :background-color (quo2.colors/theme-colors quo2.colors/divider-light quo2.colors/divider-dark)
   :margin-top 5})

(defn datemark-text []
  {:color quo2.colors/neutral-50
   :font-size 14
   :line-height 16
   :font-weight "500"})
