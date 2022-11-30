(ns status-im2.contexts.communities.overview.style
  (:require [react-native.platform :as platform]
            [quo2.foundations.colors :as colors]))

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

(def blur-channel-header {:blur-amount 32
                          :blur-type :xlight
                          :overlay-color (if platform/ios? colors/white-opa-70 :transparent)
                          :style {:position :absolute
                                  :top (if platform/ios? 44 48)
                                  :height 34
                                  :width "100%"
                                  :flex 1}})