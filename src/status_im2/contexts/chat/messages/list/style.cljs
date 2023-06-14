(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defonce ^:const cover-height 168)
(defonce ^:const overscroll-cover-height 2000)
(defonce ^:const header-avatar-top-offset -36)
(defonce ^:const messages-list-bottom-offset 16)

(defn keyboard-avoiding-container
  [{:keys [top]}]
  {:position      :relative
   :flex          1
   :top           (- top)
   :margin-bottom (- top)})

(def list-container
  {:padding-vertical 16})

(defn header-container
  [show?]
  {:display          (if show? :flex :none)
   :background-color (colors/theme-colors colors/white colors/neutral-100)
   :top              (- overscroll-cover-height)
   :margin-bottom    (- overscroll-cover-height)})

(defn header-cover
  [cover-bg-color]
  {:flex             1
   :height           (+ overscroll-cover-height cover-height)
   :background-color (colors/theme-colors (:light cover-bg-color)
                                          (:dark cover-bg-color))})

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:top              -16
    :margin-bottom    -16
    :padding-bottom   24
    :background-color (colors/theme-colors colors/white colors/neutral-95)}))

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

(def name-container
  {:flex-direction :row
   :align-items    :center})

(def bio
  {:margin-top 8})
