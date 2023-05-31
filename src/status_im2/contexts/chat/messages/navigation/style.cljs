(ns status-im2.contexts.chat.messages.navigation.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(defonce ^:const navigation-bar-height 100)
(defonce ^:const header-offset 56)

(defn button-container
  [position]
  (merge
   {:width            32
    :height           32
    :border-radius    10
    :justify-content  :center
    :align-items      :center
    :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}
   position))

(defn blur-view
  [status-bar-height]
  {:position       :absolute
   :top            0
   :left           0
   :right          0
   :height         (- navigation-bar-height
                      (if platform/ios? 0 status-bar-height))
   :display        :flex
   :flex-direction :row
   :overflow       :hidden})

(defn animated-blur-view
  [enabled? animation status-bar-height]
  (reanimated/apply-animations-to-style
   (when enabled?
     {:opacity animation})
   (blur-view status-bar-height)))

(def navigation-view
  {:z-index 4})

(defn header-container
  [status-bar-height]
  {:position       :absolute
   :top            (- header-offset
                      (if platform/ios? 0 status-bar-height))
   :left           0
   :right          0
   :padding-bottom 8
   :display        :flex
   :flex-direction :row
   :overflow       :hidden})

(def header
  {:flex 1})

(defn animated-header
  [enabled? y-animation opacity-animation]
  (reanimated/apply-animations-to-style
   ;; here using `top` won't work on Android, so we are using `translateY`
   (when enabled?
     {:transform [{:translateY y-animation}]
      :opacity   opacity-animation})
   header))

(defn pinned-banner
  [status-bar-height]
  {:position :absolute
   :left     0
   :right    0
   :top      (- navigation-bar-height
                (if platform/ios? 0 status-bar-height))})

(defn animated-pinned-banner
  [enabled? animation status-bar-height]
  (reanimated/apply-animations-to-style
   (when enabled?
     {:opacity animation})
   (pinned-banner status-bar-height)))

(def header-content-container
  {:flex-direction :row
   :align-items    :center
   :margin-left    8
   :margin-right   8
   :margin-top     -4
   :height         40})

(def header-avatar-container
  {:margin-right 8})

(def header-text-container
  {:flex 1})

(defn header-display-name
  []
  {:color (colors/theme-colors colors/black colors/white)})

(defn header-online
  []
  {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-50)})