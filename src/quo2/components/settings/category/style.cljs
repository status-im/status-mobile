(ns quo2.components.settings.category.style
  (:require [quo2.foundations.colors :as colors]))


(defn title []
  {:color (colors/theme-colors
           colors/neutral-50
           colors/neutral-40)})

(def title-container
  {:flex 1})

(def category-list-container
  {:flex 1})

