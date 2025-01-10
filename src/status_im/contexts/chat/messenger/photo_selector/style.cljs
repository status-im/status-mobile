(ns status-im.contexts.chat.messenger.photo-selector.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn gradient-container
  [bottom-inset]
  {:left     0
   :right    0
   :height   (+ bottom-inset (if platform/ios? 51 85))
   :position :absolute
   :bottom   0})

(def buttons-container
  {:position        :absolute
   :flex-direction  :row
   :left            0
   :right           0
   :top             20
   :justify-content :center
   :z-index         1})

(def clear-container
  {:position :absolute
   :right    20})

(defn image
  [window-width index]
  {:width                   (- (/ window-width 3) 0.67)
   :height                  (/ window-width 3)
   :margin-left             (when (not= (mod index 3) 0) 1)
   :margin-bottom           1
   :border-top-left-radius  (when (= index 0) 20)
   :border-top-right-radius (when (= index 2) 20)})

(defn overlay
  [window-width theme]
  {:position         :absolute
   :width            (- (/ window-width 3) 0.67)
   :height           (/ window-width 3)
   :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40 theme)})

(def image-count
  {:position :absolute
   :top      8
   :right    8})

(def photo-limit-toast-container
  {:top (if platform/ios? 6 16)})
