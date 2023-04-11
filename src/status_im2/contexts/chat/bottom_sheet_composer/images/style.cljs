(ns status-im2.contexts.chat.bottom-sheet-composer.images.style
  (:require [quo2.foundations.colors :as colors]))

(def image-container
  {:padding-top    12
   :padding-bottom 8
   :padding-right  12})

(def remove-photo-container
  {:width            14
   :height           14
   :border-radius    7
   :background-color colors/neutral-50
   :position         :absolute
   :top              5
   :right            5
   :justify-content  :center
   :align-items      :center})

(def small-image
  {:width         56
   :height        56
   :border-radius 8})

