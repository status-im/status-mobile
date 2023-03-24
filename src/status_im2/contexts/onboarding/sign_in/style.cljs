(ns status-im2.contexts.onboarding.sign-in.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-padding 20)

(def flex-spacer {:flex 1})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn root-container
  [padding-top]
  {:flex        1
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
   :top      (:y viewfinder)})

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
  [padding-bottom]
  {:padding-top             12
   :padding-bottom          padding-bottom
   :background-color        colors/white-opa-5
   :border-top-left-radius  20
   :border-top-right-radius 20
   :align-items             :center
   :justify-content         :center})

(def bottom-text
  {:color          colors/white
   :padding-bottom 12})
