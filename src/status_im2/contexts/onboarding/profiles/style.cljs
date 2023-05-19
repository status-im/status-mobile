(ns status-im2.contexts.onboarding.profiles.style
  (:require [quo2.foundations.colors :as colors]))

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

(def error-message
  {:margin-top     8
   :flex-direction :row
   :align-items    :center})

(def forget-password-doc-container {:margin-right 16})
(def forget-password-step-container {:flex-direction :row :margin-top 14})
(def forget-password-step-content {:margin-left 10})
(def forget-password-step-title {:flex-direction :row})
