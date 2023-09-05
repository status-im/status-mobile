(ns quo2.components.profile.showcase-nav.style
  (:require [quo2.foundations.colors :as colors]))

(def height 56)

(def root-container
  {:height height})

(defn container
  [state theme]
  {:padding-top      12
   :padding-left     20
   :padding-bottom   12
   :background-color (case state
                       :default (colors/theme-colors colors/white colors/neutral-95 theme)
                       :transparent)})

(def button-container
  {:margin-right 8})
