(ns status-im2.contexts.profile.profiles.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

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

