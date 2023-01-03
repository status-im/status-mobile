(ns status-im2.common.scroll-page.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn image-slider
  [height]
  {:top     (if platform/ios? 0 -64)
   ;; -64 is needed on android as the scroll doesn't
   ;; bounce so this slider won't disapear otherwise
   :height  height
   :width   height
   :z-index 4
   :flex    1})

(def blur-slider
  {:z-index  5
   :top      (if platform/ios? 0 0)
   ;; -64 is needed on android as the scroll doesn't
   ;; bounce so this slider won't disapear otherwise
   :position :absolute
   :height   (if platform/ios? 100 124)
   :width    "100%"
   :flex     1})

(defn scroll-view-container
  [border-radius]
  {:position      :absolute
   :top           -48
   :overflow      :scroll
   :border-radius border-radius
   :height        "100%"})

(def sticky-header-title
  {:position       :absolute
   :flex-direction :row
   :left           64
   :top            16})

(def sticky-header-image
  {:border-radius 12
   :border-width  0
   :border-color  :transparent
   :width         24
   :height        24
   :margin-right  8})

(defn display-picture-container
  [animation]
  {:transform        [{:scale animation}]
   :border-radius    40
   :border-width     1
   :border-color     colors/white
   :position         :absolute
   :top              -40
   :left             17
   :padding          2
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(def display-picture
  {:border-radius 50
   :border-width  0
   :border-color  :transparent
   :width         80
   :height        80})
