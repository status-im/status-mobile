(ns status-im2.contexts.chat.composer.images.style
  (:require [quo2.foundations.colors :as colors]))

(def image-container
  {:padding-top    12
   :padding-bottom 8
   :padding-right  12})

(def remove-photo-container
  {:width            15
   :height           15
   :border-radius    8
   :background-color colors/neutral-50
   :border-width     1
   :border-color     colors/white
   :position         :absolute
   :top              5
   :right            9
   :justify-content  :center
   :align-items      :center})

(def small-image
  {:width         56
   :height        56
   :border-radius 12})

