(ns status-im2.contexts.scan.style
  (:require [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]))

(def screen-padding 20)

(def blur
  {:style         {:position :absolute
                   :top      0
                   :left     0
                   :right    0
                   :bottom   0}
   :overlay-color colors/neutral-80-opa-80
   :blur-amount   20})

(def header-button
  {:margin-bottom 12
   :margin-left   screen-padding})


(def header-heading
  {:padding-horizontal screen-padding
   :padding-vertical   12
   :color              colors/white})

(def flex-spacer {:flex 1})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def hole
  (merge absolute-fill
         {:z-index 2 :opacity 0.95}))

(defn root-container
  [padding-top]
  {:z-index     5
   :flex        1
   :padding-top padding-top})

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-text
  {:padding-horizontal screen-padding
   :padding-top        12
   :padding-bottom     8
   :color              colors/white})

(def header-sub-text
  {:padding-horizontal screen-padding
   :color              colors/white})

(def tabs-container
  {:padding-horizontal screen-padding
   :margin-top         20})

(def scan-qr-code-container
  {:margin-top 19})

(def qr-view-finder
  {:margin-horizontal screen-padding
   :height            1
   :display           :flex})

(defn qr-view-finder-container
  [size]
  {:width           size
   :height          size
   :justify-content :space-between
   :margin-left     -1
   :margin-top      -1})

(defn viewfinder-container
  [viewfinder]
  {:position :absolute
   :left     (:x viewfinder)
   :top      (:y viewfinder)})

(def view-finder-border-container
  {:flex-direction  :row
   :justify-content :space-between})

(def camera-flash-button
  {:position :absolute
   :right    20
   :bottom   20})

(defn border
  [border1 border2 corner]
  (assoc {:border-color colors/white
          :width        78
          :height       78}
         border1
         2
         border2
         2
         corner
         16))

(defn border-tip
  [top bottom right left]
  {:background-color colors/white
   :position         :absolute
   :top              top
   :bottom           bottom
   :right            right
   :left             left
   :height           2
   :width            2
   :border-radius    2})

(def viewfinder-text
  {:color       colors/white-opa-70
   :text-align  :center
   :padding-top 16})

(def camera-permission-container
  {:height            335
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :border-color      colors/white-opa-10
   :border-width      1
   :border-radius     12
   :border-style      :dashed
   :align-items       :center
   :justify-content   :center})

(def enable-camera-access-header
  {:color colors/white})

(def enable-camera-access-sub-text
  {:color         colors/white-opa-70
   :margin-bottom 16})

(def enter-sync-code-container
  {:margin-top      20
   :justify-content :center
   :align-items     :center})

(defn bottom-container
  [padding-bottom]
  {:z-index                 6
   :padding-top             12
   :padding-bottom          padding-bottom
   :background-color        colors/white-opa-5
   :border-top-left-radius  20
   :border-top-right-radius 20
   :align-items             :center
   :justify-content         :center})

(def bottom-text
  {:color          colors/white
   :padding-bottom 12})

(def camera-style
  {:height           "100%"
   :borderRadius     16
   :background-color :white-opa-5})

(def camera-container
  {:position      :absolute
   :top           (if platform/android? 40 0)
   :left          0
   :right         0
   :bottom        0
   :border-radius 16})
