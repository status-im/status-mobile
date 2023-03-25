(ns status-im2.contexts.onboarding.common.background.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.common.resources :as resources]
            [react-native.linear-gradient :as linear-gradient]
            [status-im2.contexts.onboarding.common.background.style :as style]))

(defn view
  [dark-overlay?]
  [rn/view
   {:style style/background-container}
   [rn/image
    {:blur-radius (if dark-overlay? 13 0)
     :style       {:height "100%"
                   :width  "100%"}
     ;; Todo - get background image from sub using carousel index on landing page
     :source      (resources/get-image :onboarding-bg-1)}]
   [linear-gradient/linear-gradient
    {:colors [(if dark-overlay? (colors/custom-color :yin 50) "#000716")
              (if dark-overlay? (colors/custom-color :yin 50 0) "#000716")]
     :start  {:x 0 :y 0}
     :end    {:x 0 :y 1}
     :style  (style/background-gradient-overlay dark-overlay?)}]
   (when dark-overlay?
     [rn/view
      {:style style/background-blur-overlay}])])