(ns status-im2.contexts.onboarding.profiles.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

;; Profiles Section

(defn profiles-profile-card
  [last-item?]
  ;; This part needs to be improved, inverted shadow is not supported in android
  ;; https://reactnative.dev/docs/shadow-props#shadowoffset-ios
  ;; (merge
  ;;  (:shadow-1 (shadows/get-scales true :dark))
  {:padding-horizontal 20
   :margin-bottom      (when-not last-item? -24)})

(def profiles-container
  {:position :absolute
   :left     0
   :top      0
   :bottom   0
   :right    0})

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

(def multi-profile-button
  {:align-self    :flex-end
   :margin-bottom 20})

(def login-profile-card
  {:margin-bottom 20})

(def info-message
  {:margin-top 8})

(def forget-password-button
  {:margin-vertical 8})

(defn login-button
  []
  {:margin-bottom (if platform/android? 20 46)})

