(ns status-im2.contexts.profile.profiles.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]))

;; Profiles Section

(defn profiles-profile-card
  [last-item?]
  ;; This part needs to be improved, inverted shadow is not supported in android
  ;; https://reactnative.dev/docs/shadow-props#shadowoffset-ios
  ;;
  ;;   (merge (shadows/get 1 :dark :inverted) ...)
  ;;
  {:padding-horizontal 20
   :margin-bottom      (when-not last-item? -24)})

(defn profiles-container
  [translate-x]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x translate-x}]}
   {:position :absolute
    :left     0
    :top      0
    :bottom   0
    :right    0}))

(def profiles-header
  {:flex-direction     :row
   :padding-horizontal 20
   :padding-top        112
   :margin-bottom      20})

(def profiles-header-text
  {:color colors/white
   :flex  1})

;; Login Section

(def login-container
  {:position           :absolute
   :left               0
   :top                0
   :right              0
   :bottom             0
   :padding-top        56
   :padding-horizontal 20})

(def multi-profile-button-container
  {:flex-direction  :row
   :justify-content :flex-end
   :margin-bottom   20})

(def login-profile-card
  {:margin-bottom 20})

(def error-message
  {:margin-top     8
   :flex-direction :row
   :align-items    :center})

(def forget-password-doc-container {:margin-right 16})
(def forget-password-step-container {:flex-direction :row :margin-top 14})
(def forget-password-step-content {:margin-left 10})
(def forget-password-step-title {:flex-direction :row})


(def share-link-button
  {:margin-top        12
   :margin-horizontal 16
   :margin-bottom     16})

(def radius 16)

(def top-background-view
  {:background-color colors/magenta-opa-40
   :position         :absolute
   :top              0
   :left             0
   :height           400
   :width            400
   :z-index          -1})

(def toolbar
  {:padding-bottom  16
   :padding-top     (safe-area/get-top)
   :flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(def header-icon-style
  {:border-radius    10
   :margin-left      16
   :width            32
   :height           32
   :background-color colors/white-opa-10})

(def right-accessories
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between
   :margin-right    20})

(def avatar
  {:margin-top        8
   :margin-horizontal 24
   :z-index           100})

(def user-info
  {:background-color        colors/neutral-95
   :padding-horizontal      16
   :border-top-left-radius  20
   :border-top-right-radius 20
   :margin-top              -38
   :padding-top             38
   :padding-bottom          16})

(def container-style {:background-color colors/neutral-95})

(def rounded-view
  {:margin-top        16
   :margin-horizontal 20
   :overflow          "hidden"
   :border-radius     radius})

(def list-item-container
  {:background-color "#242D3F"
   :padding-left     -12
   :padding-right    6
   :height           50})

(def logout-container
  {:align-items       :center
   :justify-content   :center
   :flex-direction    :row
   :height            48
   :margin-horizontal 20
   :margin-top        16
   :margin-bottom     64
   :overflow          "hidden"
   :border-radius     radius
   :background-color  colors/danger-50-opa-20})

