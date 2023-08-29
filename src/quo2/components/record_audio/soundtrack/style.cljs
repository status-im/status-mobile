(ns quo2.components.record-audio.soundtrack.style
  (:require [react-native.platform :as platform]))

(defn player-slider-container
  []
  (merge
   {:position :absolute
    :left     (if platform/ios? 115 104)
    :right    (if platform/ios? 108 92)
    :bottom   (if platform/ios? 16 27)}
   (when platform/android?
     ;; Workaround to increase the thickness of the slider track on Android
     ;; which is currently not supported by the Slider library and remove
     ;; the thumb shadow that appears when dragging.
     {:transform        [{:scaleY 2}]
      :background-color :transparent})))
