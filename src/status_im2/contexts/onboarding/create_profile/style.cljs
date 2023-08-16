(ns status-im2.contexts.onboarding.create-profile.style
  (:require [react-native.platform :as platform]
            [quo2.foundations.colors :as colors]))

(def continue-button
  {:width        "100%"
   :margin-left  :auto
   :margin-top   (if platform/android? :auto 0)
   :margin-right :auto})

(def button-container
  {:width         "100%"
   :padding-left  20
   :padding-right 20
   :padding-top   12
   :align-self    :flex-end
   :height        64})

(defn view-button-container
  [keyboard-shown?]
  (merge button-container
         (if platform/ios?
           {:margin-bottom (if keyboard-shown? 0 34)}
           {:margin-bottom (if keyboard-shown? 12 34)})))

(def blur-button-container
  (merge button-container
         (when platform/android? {:padding-bottom 12})))

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0
   :z-index  100})

(def info-message
  {:margin-top 8})

(def title
  {:color         colors/white
   :margin-top    12
   :margin-bottom 18})

(def color-title
  {:color         colors/white-70-blur
   :margin-top    20
   :margin-bottom 16})

(def content-container
  {:padding-horizontal 20})

(def input-container
  {:align-items :flex-start})

(def profile-input-container
  {:flex-direction  :row
   :justify-content :center})
