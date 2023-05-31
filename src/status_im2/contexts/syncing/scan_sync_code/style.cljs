(ns status-im2.contexts.syncing.scan-sync-code.style
  (:require [quo2.foundations.colors :as colors]
            [status-im.utils.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def screen-padding 20)

(def flex-spacer {:flex 1})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def hole
  (merge absolute-fill
         {:top 0 :z-index 2 :opacity 0.95}))

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

(defn viewfinder-container
  [viewfinder]
  {:position :absolute
   :left     (:x viewfinder)
   :top      (:y viewfinder)
   :overflow :hidden})

(def viewfinder-text
  {:color       colors/white-opa-70
   :text-align  :center
   :padding-top 16})

(def camera-permission-container
  {:height            335
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :border-color      colors/white-opa-10
   :border-radius     12
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
  [translate-y padding-bottom]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y translate-y}]}
   {:z-index                 6
    :padding-top             12
    :padding-bottom          padding-bottom
    :background-color        colors/white-opa-5
    :border-top-left-radius  20
    :border-top-right-radius 20
    :align-items             :center
    :justify-content         :center}))

(def bottom-text
  {:color          colors/white
   :padding-bottom 12})

(def camera-style
  {:height           "100%"
   :borderRadius     16
   :background-color :transparent})

(def camera-container
  {:position      :absolute
   :top           (if platform/android? 40 0)
   :left          0
   :right         0
   :bottom        0
   :border-radius 16})
