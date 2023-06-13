(ns status-im2.contexts.chat.messages.pin.banner.style
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defonce ^:const pinned-banner-height 40)

(defn blur-container-style
  [top-offset opacity-animation enabled?]
  (reanimated/apply-animations-to-style
   {:opacity opacity-animation}
   {:position :absolute
    :display  (if enabled? :flex :none)
    :top      top-offset
    :left     0
    :right    0
    :bottom   0
    :height   pinned-banner-height
    :overflow :hidden}))

(defn blur-view-style
  []
  {:style       {:flex 1}
   :blur-radius (if platform/ios? 20 10)
   :blur-type   (colors/theme-colors :light :dark)
   :blur-amount 20})

(defn pinned-banner
  [top-offset]
  {:position :absolute
   :left     0
   :right    0
   :top      top-offset})

(defn animated-pinned-banner
  [top-offset enabled? animation]
  (reanimated/apply-animations-to-style
   (when enabled?
     {:opacity animation})
   (pinned-banner top-offset)))