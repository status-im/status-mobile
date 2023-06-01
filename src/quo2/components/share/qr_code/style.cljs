(ns quo2.components.share.qr-code.style
  (:require [quo2.foundations.colors :as colors]))

(def container
  {:flex-direction  :row
   :justify-content :center})

(defn image
  [width height]
  {:width            width
   :height           height
   :background-color colors/white-opa-70
   :border-radius    12
   :aspect-ratio     1})
