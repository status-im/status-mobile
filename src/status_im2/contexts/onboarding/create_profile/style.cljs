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
   :padding-top   (if platform/android? 0 12)
   :align-self    :flex-end
   :height        64})

(def blur-button-container
  (merge button-container
         {:background-color colors/neutral-80-opa-1-blur}))

(def view-button-container
  (merge button-container {:margin-bottom 24}))

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def navigation-bar {:height 56})

(def info-message
  {:margin-top 8})

(def title
  {:color         colors/white
   :margin-top    12
   :margin-bottom 20})

(def color-title
  {:color         colors/white-70-blur
   :margin-top    20
   :margin-bottom 16})

(def content-container
  {:flex               1
   :padding-horizontal 20})

(def input-container
  {:flex          1
   :align-items   :flex-start
   :margin-bottom 24})

(def profile-input-container
  {:flex-direction  :row
   :justify-content :center})
