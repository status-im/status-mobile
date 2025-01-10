(ns status-im.contexts.profile.settings.screens.password.change-password.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]))

(def form-container
  {:margin-horizontal 20})

(def heading
  {:margin-bottom 20
   :margin-top    12})

(def heading-subtitle {:color colors/white})

(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def space-between-inputs {:height 16})

(def error-container
  {:margin-top     8
   :flex-direction :row
   :align-items    :center})

(def warning-container
  {:margin-top 12})

(defn loading-container
  [{:keys [top bottom]}]
  {:flex            1
   :justify-content :space-between
   :padding-top     top
   :padding-bottom  bottom})

(def loading-content
  {:flex               1
   :padding-horizontal 20})

(def logout-container
  {:margin-vertical   12
   :margin-horizontal 20})

(def password-tips
  {:flex-direction    :row
   :margin-horizontal 20
   :justify-content   :space-between})

(def bottom-part
  {:margin-bottom   (if platform/ios? (safe-area/get-bottom) 12)
   :justify-content :flex-end})

(def disclaimer-container
  {:margin-horizontal 20
   :margin-vertical   4})

(def button-container
  {:margin-top        12
   :margin-horizontal 20})
