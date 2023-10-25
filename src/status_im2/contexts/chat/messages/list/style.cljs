(ns status-im2.contexts.chat.messages.list.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im2.config :as config]))

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

(defn header-image
  [scale-animation top-margin-animation side-margin-animation]
  (reanimated/apply-animations-to-style
   {:transform     [{:scale scale-animation}]
    :margin-top    top-margin-animation
    :margin-left   side-margin-animation
    :margin-bottom side-margin-animation}
   {:align-items :flex-start}))
