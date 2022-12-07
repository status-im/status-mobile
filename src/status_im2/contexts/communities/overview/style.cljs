(ns status-im2.contexts.communities.overview.style
  (:require [react-native.platform :as platform]
            [quo2.foundations.colors :as colors]
            ))

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

(def blur-channel-header
  {:position :absolute
   :top      (if platform/ios? 56 60)
   :height   34
   :width    "100%"
   :flex     1})

(def join-button
  {:width        "100%"
   :margin-top   20
   :margin-left  :auto
   :margin-right :auto})

(defn scroll-view-container [border-radius] {:position :absolute
                                             :top  -48
                                             :overflow :scroll
                                             :border-radius border-radius
                                             :height "100%"})

(def review-notice {:color colors/neutral-50
                    :margin-top 12
                    :margin-left :auto
                    :margin-right :auto})
