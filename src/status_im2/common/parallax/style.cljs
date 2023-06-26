(ns status-im2.common.parallax.style
  (:require [react-native.safe-area :as safe-area]
            [react-native.platform :as platform]))

(def outer-container
  {:position :absolute
   :top      (if platform/android?
               (+ (safe-area/get-top) (safe-area/get-bottom))
               (safe-area/get-bottom))
   :left     0
   :right    0
   :bottom   0})

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
