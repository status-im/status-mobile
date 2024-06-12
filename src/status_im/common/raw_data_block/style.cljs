(ns status-im.common.raw-data-block.style
  (:require [quo.foundations.colors :as colors]))

(defn container
  [bottom-margin?]
  {:flex          1
   :padding       10
   :margin-top    10.5
   :margin-bottom (when bottom-margin? 10.5)
   :border-width  1
   :border-color  colors/neutral-10
   :border-radius 16})

(def content
  {:padding-bottom 20})
