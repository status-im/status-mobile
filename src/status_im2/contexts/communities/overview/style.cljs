(ns status-im2.contexts.communities.overview.style
  (:require [quo.platform :as platform]
            [quo2.foundations.colors :as colors]))

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

(defn image-slider [height] {:top (if platform/ios? 0 -64) ; -44 -20 (the 20 is needed on android as the scroll doesn't bounce so this won't disapear otherwise) 
                             :height height
                             :z-index 4
                             :flex 1})

(defn blur-slider [height] {:blur-amount 32
                            :blur-type :xlight
                            :overlay-color (if platform/ios? colors/white-opa-70 "transparent")
                            :style {:z-index 5
                                    :top (if platform/ios? 0 -64) ; -44 -20 (the 20 is needed on android as the scroll doesn't bounce so this won't disapear otherwise) 
                                    :position :absolute
                                    :height height
                                    :width "100%"
                                    :flex 1}})

(def blur-channel-header {:blur-amount 32
                          :blur-type :xlight
                          :overlay-color (if platform/ios? colors/white-opa-70 "transparent")
                          :style {:position :absolute
                                  :top (if platform/ios? 44 48)
                                  :height 34
                                  :width "100%"
                                  :flex 1}})

(defn scroll-view-container [border-radius] {:position :absolute
                                             :top  -48
                                             :overflow :scroll
                                             :border-radius border-radius
                                             :height "100%"})