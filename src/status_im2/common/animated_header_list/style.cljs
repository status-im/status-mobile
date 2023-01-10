(ns status-im2.common.animated-header-list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn container-view
  [view-height theme-color]
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
    :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)
    :position         :absolute
    :top              56
    :z-index          3}
   position))

(defn blur-view
  [animation]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY animation}]}
   {:position :absolute
    :top      0
    :left     0
    :right    0
    :height   100
    :z-index  2}))

(defn entity-picture
  [animation]
  (reanimated/apply-animations-to-style
   {:transform [{:scale animation}]}
   {:width            80
    :height           80
    :border-radius    40
    :position         :absolute
    :bottom           0
    :left             20
    :justify-content  :center
    :align-items      :center
    :background-color (colors/theme-colors colors/white colors/neutral-95)
    :overflow         :hidden}))

(def header-bottom-part
  {:position                :absolute
   :bottom                  0
   :height                  44
   :left                    0
   :right                   0
   :background-color        (colors/theme-colors colors/white colors/neutral-95)
   :border-top-right-radius 16
   :border-top-left-radius  16})

(defn title-comp
  [animation]
  (reanimated/apply-animations-to-style
   {:opacity animation}
   {:position :absolute
    :top      56
    :left     64
    :z-index  3}))
