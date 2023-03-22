(ns status-im2.contexts.chat.messages.composer-new.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def handle-container-height 20)

(def input-height 32)

(def actions-container-height 56)

(defn container
  [insets]
  (merge
    {:border-top-left-radius  20
     :border-top-right-radius 20
     :padding-horizontal      20
     :position :absolute
     ;:height 400
     :bottom                  0
     :left                    0
     :right                   0
     :background-color        (colors/theme-colors colors/white colors/neutral-90)
     :z-index                 2
     :padding-bottom          (:bottom insets)}
    (if platform/ios?
      {:shadow-radius  16
       :shadow-opacity 1
       :shadow-color   "rgba(9, 16, 28, 0.04)"
       :shadow-offset  {:width 0 :height -2}}
      {:elevation 4})))

(defn handle-container
  []
  {:left            0
   :right           0
   :top             0
   :height          handle-container-height
   ;:background-color :red
   :z-index         1
   :justify-content :center
   :align-items     :center})

(defn handle
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10)})

(defn input
  [height]
  (reanimated/apply-animations-to-style
    {
     :min-height height
     ;:height height
     ;:height height
     ;:max-height height
     ;:height           height
     }
    (merge typography/paragraph-1
           {:padding-vertical 5
            ;:height 300
            ;:background-color :green
            })))


(defn actions-container
  []
  {:height          actions-container-height
   :justify-content :center
   })
