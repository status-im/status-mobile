(ns quo2.components.onboarding.small-option-card.style
  (:require [quo2.foundations.colors :as colors]))

(def text-container {:flex 1})

(def title
  {:color  colors/white
   :height 22})

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
  {:flex          1
   :margin-bottom -8})

(def main-variant-text-container
  {:height             62
   :padding-top        10
   :padding-bottom     12
   :padding-horizontal 12})

(def touchable-overlay {:border-radius 16})

(defn card-container
  [main-variant?]
  (when main-variant? {:height 343}))

(defn card
  [main-variant?]
  {:background-color colors/white-opa-5
   :border-radius    16
   :height           (if main-variant? 335 56)})
