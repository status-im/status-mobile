(ns status-im.common.scroll-page.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn blur-slider
  [animation height theme]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY animation}]}
   {:z-index          5
    :position         :absolute
    :top              0
    :height           height
    :right            0
    :left             0
    :background-color (if platform/android?
                        (colors/theme-colors colors/white colors/neutral-80 theme)
                        :transparent)}))

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
  [animation theme]
  (reanimated/apply-animations-to-style
   {:transform [{:scale animation}]}
   {:border-radius    picture-diameter
    :border-width     picture-border-width
    :border-color     (colors/theme-colors colors/white colors/neutral-95 theme)
    :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
    :position         :absolute
    :top              (- (+ picture-radius picture-border-width))
    :left             (+ (/ picture-radius 2) picture-border-width)}))

(defn display-picture
  [theme]
  {:border-radius    picture-diameter
   :width            picture-diameter
   :height           picture-diameter
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)})

(defn cover-background
  [cover-color]
  {:background-color cover-color
   :height           167
   :margin-bottom    -16})
