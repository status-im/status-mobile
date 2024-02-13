(ns quo.components.community.community-stat.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex-direction :row
   :align-items    :center
   :height         22})

(defn text
  [theme]
  {:color       (colors/theme-colors colors/black colors/white theme)
   :margin-left 2})
