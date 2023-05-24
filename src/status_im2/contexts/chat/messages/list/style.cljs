(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(defonce ^:const cover-height 168)
(defonce ^:const overscroll-cover-height 2000)
(defonce ^:const header-avatar-top-offset -36)

(def footer
  {:z-index 5})

(defn keyboard-avoiding-container
  [view-height keyboard-height]
  {:position :absolute
   :flex     1
   :top      0
   :left     0
   :height   (- view-height keyboard-height)
   :right    0})

(def list-container
  {:padding-vertical 16})

(defn header-container
  [insets]
  {:scale-y          (if platform/android? -1 0)
   :background-color (colors/theme-colors colors/white colors/neutral-95)
   :top              (if platform/ios?
                       (- 0 overscroll-cover-height)
                       (:top insets))
   :margin-bottom    (if platform/ios?
                       (- 0 overscroll-cover-height)
                       0)})

(defn header-cover
  [cover-bg-color insets]
  {:flex             1
   :height           (if platform/ios?
                       (+ overscroll-cover-height cover-height)
                       (+ (:top insets) cover-height))
   :background-color cover-bg-color})

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:top              -16
    :margin-bottom    -16
    :padding-bottom   24
    :background-color (colors/theme-colors colors/white colors/neutral-100)
    :display          :flex}))

(def header-avatar
  {:top               header-avatar-top-offset
   :margin-horizontal 20
   :margin-bottom     header-avatar-top-offset})

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
    {:scale-y -1}))

(def name-container
  {:flex-direction :row
   :align-items    :center})

(def bio
  {:margin-top 8})
