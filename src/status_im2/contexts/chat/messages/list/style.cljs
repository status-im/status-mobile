(ns status-im2.contexts.chat.messages.list.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.messages.constants :as messages.constants]))

(def keyboard-avoiding-container
  {:flex    1
   :z-index 2})

(def list-container
  {:padding-vertical 16})

(defn background-container
  [background-color background-opacity top-margin]
  (reanimated/apply-animations-to-style
   {:opacity background-opacity}
   {:background-color background-color
    :position         :absolute
    :top              0
    :left             0
    :right            0
    :height           (+ top-margin messages.constants/header-container-radius)}))

(defn header-bottom-part
  [border-radius theme top-margin]
  (reanimated/apply-animations-to-style
   {:border-top-left-radius  border-radius
    :border-top-right-radius border-radius}
   {:background-color   (colors/theme-colors colors/white colors/neutral-95 theme)
    :padding-horizontal 20
    :margin-top         top-margin}))

(defn header-image
  [scale top left theme]
  (reanimated/apply-animations-to-style
   {:transform [{:scale scale}]
    :top       top
    :left      left}
   {:position      :absolute
    :border-width  4
    :border-radius 50
    :border-color  (colors/theme-colors colors/white colors/neutral-95 theme)}))

(def bio
  {:margin-top 8})
