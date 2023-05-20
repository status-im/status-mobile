(ns quo2.components.share.qr-code.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex-direction   :row
   :justify-content  :center
   :background-color colors/white
   :border-radius    12
   :overflow         :hidden})

(defn image
  [width height]
  {:width        width
   :height       height
   :aspect-ratio 1})
