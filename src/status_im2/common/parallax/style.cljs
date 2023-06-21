(ns status-im2.common.parallax.style
  (:require [react-native.safe-area :as safe-area]))

(def outer-container
  {:position :absolute
   :top      (safe-area/get-top)
   :left     0
   :right    0
   :bottom   (safe-area/get-bottom)})

(def video
  {:position :absolute
   :top      0
   :left     0
   :right    0
   :bottom   0})

(defn container-view
  [width height]
  {:position :absolute
   :width    width
   :height   height})
