(ns status-im2.contexts.onboarding.identifiers.profile-card.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.reanimated :as reanimated]))

(def card-view
  {:margin-horizontal 20
   :margin-bottom     :auto
   :flex-direction    :row})

(defn card-container
  [background-color]
  (reanimated/apply-animations-to-style
   {:background-color background-color}
   {:padding-horizontal 12
    :padding-top        12
    :padding-bottom     12
    :flex               1
    :border-radius      16}))

(def card-header
  {:flex-direction  :row
   :justify-content :space-between})

(defn avatar
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {}))

(defn mask-view
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:width            48
    :background-color :transparent
    :height           48
    :border-color     :black
    :border-width     3
    :border-radius    44}))

(def picture-avatar-mask
  {:width         48
   :height        48
   :border-radius 48})

(defn user-name-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:flex-direction :row
    :margin-top     8
    :align-items    :center
    :padding-right  12}))

(def user-name
  {:color colors/white})

(defn user-hash
  [user-hash-color user-hash-opacity]
  (reanimated/apply-animations-to-style
   {:color   user-hash-color
    :opacity user-hash-opacity}
   (merge typography/monospace
          typography/paragraph-1
          {:margin-top 2})))

(def emoji-hash
  {:margin-top  12
   :line-height 20.5})
