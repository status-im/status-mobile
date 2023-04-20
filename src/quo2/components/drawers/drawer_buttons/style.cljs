(ns quo2.components.drawers.drawer-buttons.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def outer-container
  {:height        216
   :border-radius 20})

(def top-card
  {:flex                    1
   :padding-vertical        12
   :padding-horizontal      20
   :border-top-left-radius  20
   :border-top-right-radius 20
   :background-color        (if platform/ios?
                              colors/neutral-80-opa-80-blur
                              colors/neutral-80)})

(def bottom-card
  {:position                :absolute
   :top                     80
   :left                    0
   :right                   0
   :bottom                  0
   :padding-vertical        12
   :padding-horizontal      20
   :border-top-left-radius  20
   :border-top-right-radius 20
   :background-color        colors/white-opa-5})

(def bottom-container
  {:flex-direction  :row
   :justify-content :space-between})

(def bottom-icon
  {:border-radius   40
   :border-width    1
   :margin-left     24
   :height          28
   :width           28
   :justify-content :center
   :align-items     :center
   :border-color    colors/white-opa-5})

(def bottom-text
  {:flex  1
   :color colors/white-70-blur})

(def top-text
  {:color colors/white-70-blur})

(defn heading-text
  [gap]
  {:color         colors/white
   :margin-bottom gap})
