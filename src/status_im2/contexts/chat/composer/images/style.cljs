(ns status-im2.contexts.chat.composer.images.style
  (:require
    [quo.foundations.colors :as colors]
    [status-im2.contexts.chat.composer.constants :as constants]))

(def image-container
  {:padding-top    constants/images-padding-top
   :padding-bottom constants/images-padding-bottom
   :padding-right  12})

(defn remove-photo-container
  [theme]
  {:width            16
   :height           16
   :border-radius    8
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :position         :absolute
   :top              5
   :right            9
   :justify-content  :center
   :align-items      :center})


(def remove-photo-inner-container
  {:width            14
   :height           14
   :border-radius    7
   :background-color colors/neutral-50
   :justify-content  :center
   :align-items      :center})

(def small-image
  {:width         56
   :height        56
   :border-radius 12})
