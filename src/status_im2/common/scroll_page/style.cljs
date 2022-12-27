(ns status-im2.common.scroll-page.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn image-slider
  [height]
  {:top     (if platform/ios? 0 -64)
   ;; -64 is needed on android as the scroll doesn't
   ;; bounce so this slider won't disapear otherwise
   :height  height
   :z-index 4
   :flex    1})

(defn blur-slider
  [height]
  {:blur-amount   32
   :blur-type     :xlight
   :overlay-color (if platform/ios? colors/white-opa-70 :transparent)
   :style         {:z-index  5
                   :top      (if platform/ios? 0 -64)
                   ;; -64 is needed on android as the scroll doesn't
                   ;; bounce so this slider won't disapear otherwise
                   :position :absolute
                   :height   height
                   :width    "100%"
                   :flex     1}})

(defn scroll-view-container
  [border-radius]
  {:position      :absolute
   :top           -48
   :overflow      :scroll
   :border-radius border-radius
   :height        "100%"})