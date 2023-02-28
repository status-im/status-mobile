(ns quo2.components.animated-header-flatlist.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn container-view
  [view-height]
  {:position :absolute
   :top      0
   :left     0
   :right    0
   ;; height must be set, otherwise list will not scroll
   :height   view-height})

(defn button-container
  [position]
  (merge
   {:width            32
    :height           32
    :border-radius    10
    :justify-content  :center
    :align-items      :center
    :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}
   position))

(defn blur-view
  [animation scroll-y]
  (reanimated/apply-animations-to-style
    {:opacity animation}
   {:position :absolute
    :top      0
    :left     0
    :right    0
    :height   100
    :width "100%"
    :display :flex
    :flex-direction :row
    :z-index  2
    :overflow :hidden}))

(defn entity-picture
  [animation]
  (reanimated/apply-animations-to-style
   {}
   {:transform        [{:scale 1}]
    :position         :absolute
    :bottom           72
    :left             20
    :justify-content  :center
    :align-items      :center
    :overflow         :hidden}))

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:background-color (colors/theme-colors colors/white colors/neutral-100)}))

(defn header-comp
  [y-animation opacity-animation]
  (reanimated/apply-animations-to-style
   ;; here using `left` won't work on Android, so we are using `translateX`
   {:transform [{:translateY y-animation}]
    :opacity   opacity-animation}
   {:flex 1}))
