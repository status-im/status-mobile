(ns status-im2.contexts.communities.overview.style
  (:require [react-native.platform :as platform]
            [status-im2.common.scroll-page.view :as scroll-page]
            [quo2.foundations.colors :as colors]))

(def preview-user
  {:flex-direction :row
   :align-items    :center
   :margin-top     20})

(def blur-channel-header {:blur-amount 32
                          :blur-type :xlight
                          :overlay-color (if platform/ios? colors/white-opa-70 "transparent")
                          :style {:position :absolute
                                  :top (if platform/ios? 44 48)
                                  :height 34
                                  :width "100%"
                                  :flex 1}})

(defn icon-top [scroll-height]
  (if (<= scroll-height scroll-page/negative-scroll-position-0)
    -40
    (->> (+ scroll-page/scroll-position-0 scroll-height)
         (* (if platform/ios? 3 1))
         (+ -40)
         (min 8))))

(defn icon-size [scroll-height]
  (->> (+ scroll-page/scroll-position-0 scroll-height)
       (* (if platform/ios? 3 1))
       (- scroll-page/max-image-size)
       (max  scroll-page/min-image-size)
       (min scroll-page/max-image-size)))