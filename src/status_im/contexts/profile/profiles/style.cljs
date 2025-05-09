(ns status-im.contexts.profile.profiles.style
  (:require
    [quo.foundations.colors :as colors]
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

(defn profiles-header
  [top]
  {:flex-direction     :row
   :padding-horizontal 20
   :padding-top        (+ 56 top)
   :margin-bottom      20})

(def profiles-header-text
  {:color colors/white
   :flex  1})

;; Login Section

(defn login-container
  [top]
  {:position           :absolute
   :left               0
   :top                0
   :right              0
   :bottom             0
   :padding-top        top
   :padding-horizontal 20})

(def multi-profile-button-container
  {:flex-direction   :row
   :padding-vertical 12
   :justify-content  :flex-end
   :margin-bottom    8})

(def login-profile-card
  {:margin-bottom 20})
