(ns status-im.contexts.chat.messenger.messages.list.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]))

(def keyboard-avoiding-container
  {:flex    1
   :z-index 2})

(def chat-actions-container
  {:margin-top     16
   :padding-bottom 20})

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
    ;; :row-gap            100
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

(defn user-name-container
  [top left]
  (reanimated/apply-animations-to-style
   {:top  top
    :left left}
   {:z-index -1}))

(defn user-name
  [group-chat]
  {:align-items    :center
   :flex-direction :row
   :margin-top     (if group-chat 52 52)})

(def bio
  {:margin-top 8})
