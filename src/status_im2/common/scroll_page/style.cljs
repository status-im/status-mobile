(ns status-im2.common.scroll-page.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(defn image-slider
  [size]
  {:top     -64
   :height  size
   :width   size
   :z-index 4
   :flex    1})

(defn blur-slider
  [animation height]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY animation}]}
   {:z-index          5
    :position         :absolute
    :top              0
    :height           height
    :right            0
    :left             0
    :background-color (if platform/ios?
                        (colors/theme-colors
                         colors/white-opa-70
                         colors/neutral-95-opa-70)
                        :transparent)}))

(defn sticky-header-title
  [animation]
  (reanimated/apply-animations-to-style
   {:opacity animation}
   {:position       :absolute
    :flex-direction :row
    :left           64
    :top            16
    :margin-top     44}))

(def sticky-header-image
  {:border-radius 12
   :border-width  0
   :border-color  :transparent
   :width         24
   :height        24
   :margin-right  8})

(defn children-container
  [{:keys [border-radius background-color]}]
  {:flex                    1
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius
   :background-color        background-color})

(def picture-radius 40)
(def picture-diameter (* 2 picture-radius))
(def picture-border-width 4)

(defn display-picture-container
  [animation]
  (reanimated/apply-animations-to-style
   {:transform [{:scale animation}]}
   {:border-radius picture-diameter
    :border-width  picture-border-width
    :border-color  (colors/theme-colors colors/white colors/neutral-95)
    :position      :absolute
    :top           (- (+ picture-radius picture-border-width))
    :left          (+ (/ picture-radius 2) picture-border-width)}))

(defn display-picture
  [theme]
  {:border-radius    picture-diameter
   :width            picture-diameter
   :height           picture-diameter
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)})
