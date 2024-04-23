(ns status-im.contexts.chat.messenger.messages.list.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.foundations.shadows :as shadows]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]))

(def keyboard-avoiding-container
  {:flex    1
   :z-index 2})

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

(defn header-bottom-container
  [bottom top-margin]
  (reanimated/apply-animations-to-style
   {:bottom bottom}
   {:margin-top top-margin}))

(defn header-bottom-part
  [theme]
  {:background-color   (colors/theme-colors colors/white colors/neutral-95 theme)
   :padding-horizontal 20
   :border-radius      20})

(defn header-bottom-shadow
  [theme]
  (assoc
   (shadows/get 2 theme :inverted)
   :left          0
   :right         0
   :height        40
   :position      :absolute
   :border-radius 20))

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

(def user-name
  {:align-items    :center
   :flex-direction :row
   :margin-top     52})

(defn bio-and-actions
  [top]
  (reanimated/apply-animations-to-style
   {:top top}
   {:row-gap 16}))
