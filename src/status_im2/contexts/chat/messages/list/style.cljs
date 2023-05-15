(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def cover-height 168)
(def overscroll-cover-height 2000)
(def cover-bg-color "#2A799B33")

(def footer
  {:z-index 5})

(defn keyboard-avoiding-container
  [view-height keyboard-height]
  {:position :absolute
   :display  :flex
   :flex     1
   :top      0
   :left     0
   :height   (- view-height keyboard-height)
   :right    0})

(def list-container
  {:padding-top    16
   :padding-bottom 16})

(defn header-container
  [insets]
  {:scaleY           (if platform/android? -1 0)
   :background-color (colors/theme-colors colors/white colors/neutral-95)
   :top              (if platform/ios?
                       (- 0 overscroll-cover-height)
                       (:top insets))
   :margin-bottom    (if platform/ios?
                       (- 0 overscroll-cover-height)
                       0)})

(defn header-cover
  [cover-bg-color status-bar-height]
  {:width            "100%"
   :height           (if platform/ios?
                       (+ overscroll-cover-height cover-height)
                       (+ status-bar-height cover-height))
   :background-color cover-bg-color})

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:position         :relative
    :top              -16
    :margin-bottom    -16
    :padding-bottom   24
    :background-color (colors/theme-colors colors/white colors/neutral-100)
    ;;:shadow-radius    16
    ;;:shadow-opacity   1
    ;;:shadow-color     "rgba(9, 16, 28, 0.06)"
    ;;:shadow-offset    {:width 0 :height -24}
    :display          :flex}))

(def header-avatar
  {:top           -36
   :margin-left   20
   :margin-right  20
   :margin-bottom -36})

(defn header-image
  [scale-animation top-margin-animation side-margin-animation]
  (reanimated/apply-animations-to-style
   {:transform     [{:scale scale-animation}]
    :margin-top    top-margin-animation
    :margin-left   side-margin-animation
    :margin-bottom side-margin-animation}
   {:align-items :flex-start}))

(def list-chat-group-header-style
  (when platform/android?
    {:scaleY -1}))

(def name-container
  {:flex-direction :row
   :align-items    :center})

(def bio
  {:margin-top 8})
