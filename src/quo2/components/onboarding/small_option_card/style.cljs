(ns quo2.components.onboarding.small-option-card.style
  (:require [quo2.foundations.colors :as colors]))

(def main-variant-height 335)
(def icon-variant-height 56)

(def text-container {:flex 1})

(def icon-title
  {:color  colors/white
   :height 22})

(def main-title
  {:color  colors/white
   :height 26})

(def subtitle
  {:color  colors/white-opa-70
   :height 18})

(def icon-variant
  {:flex-direction     :row
   :padding-vertical   8
   :padding-horizontal 12})

(def icon-variant-image-container
  {:width            32
   :height           40
   :justify-content  :center
   :align-items      :center
   :padding-vertical 4
   :margin-right     8})

(def icon-variant-image
  {:flex   1
   :width  32
   :height 32})

(def main-variant
  {:flex 1})

(def main-variant-text-container
  {:height             62
   :padding-top        10
   :padding-horizontal 12})

(defn main-variant-image
  [max-height]
  {:flex       1
   :max-height max-height})

(def main-button
  {:padding-horizontal 12 :margin-bottom 12 :margin-top 8})

(defn card
  [height]
  {:background-color colors/white-opa-5
   :border-radius    16
   :height           height})
