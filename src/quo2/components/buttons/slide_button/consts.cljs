(ns quo2.components.buttons.slide-button.consts
  (:require
   [quo2.foundations.colors :as colors]))

(def track-padding 4)
(def threshold-release-frac 0.65)
(def threshold-drag-frac 0.9)
(def thumb-border-radius 12)
(def small-dimensions {:track-height 40
                       :thumb 32})
(def large-dimensions {:track-height 48
                       :thumb 40})
(def slide-colors
  {:thumb (colors/custom-color-by-theme :blue 50 60)
   :success (colors/custom-color-by-theme :blue 50 60 60 60)
   :text (colors/custom-color-by-theme :blue 50 60)
   :text-transparent colors/white-opa-40
   :track (colors/custom-color :blue 50 10)})




