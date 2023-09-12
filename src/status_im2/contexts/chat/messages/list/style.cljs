(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [status-im2.config :as config]
            [react-native.reanimated :as reanimated]))

(defonce ^:const cover-height 168)
(defonce ^:const overscroll-cover-height 2000)
(defonce ^:const header-avatar-top-offset -36)

(defn keyboard-avoiding-container
  [{:keys [top]}]
  {:position      :relative
   :flex          1
   :top           (if config/shell-navigation-disabled? (- top) 0)
   :margin-bottom (if config/shell-navigation-disabled? (- top) 0)})

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
   :background-color (colors/theme-colors
                      (colors/custom-color cover-bg-color 50 20)
                      (colors/custom-color cover-bg-color 50 40))})

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

(def bio
  {:margin-top 8})
