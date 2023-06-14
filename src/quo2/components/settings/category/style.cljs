(ns quo2.components.settings.category.style
  (:require [quo2.foundations.colors :as colors]))


(defn title
  []
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40)})

(def title-container
  {:flex          1
   :margin-bottom 10})

(def category-list-container
  {:flex    1
   :padding 20})

(def flat-list
  {:border-radius 20
   :border-width  1
   :overflow      :hidden
   :border-color  (colors/theme-colors colors/neutral-20 colors/neutral-80)})
