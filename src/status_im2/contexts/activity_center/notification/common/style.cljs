(ns status-im2.contexts.activity-center.notification.common.style
  (:require [quo2.foundations.colors :as colors]))

(def swipe-action-width 72)
(def swipe-button-border-radius 16)

(def user-avatar-tag
  {:background-color colors/white-opa-10})

(def user-avatar-tag-text
  {:color colors/white})

(def left-swipe-opacity-interpolation-js
  (clj->js {:inputRange  [0 swipe-action-width]
            :outputRange [0 1]
            :extrapolate :clamp}))

(def left-swipe-translate-x-interpolation-js
  (clj->js {:inputRange  [0 swipe-action-width]
            :outputRange [(- swipe-action-width) 0]
            :extrapolate :clamp}))

(def right-swipe-opacity-interpolation-js
  (clj->js {:inputRange  [(- swipe-action-width) 0]
            :outputRange [1 0]
            :extrapolate :clamp}))

(def right-swipe-translate-x-interpolation-js
  (clj->js {:inputRange  [(- swipe-action-width) 0]
            :outputRange [0 swipe-action-width]
            :extrapolate :clamp}))

(def swipe-base
  {:align-items     :center
   :justify-content :center
   :border-radius   swipe-button-border-radius
   :width           swipe-action-width})

(defn swipe-success-container
  [style]
  (merge swipe-base
         {:background-color colors/success-60}
         style))

(defn swipe-danger-container
  [style]
  (merge swipe-base
         {:background-color colors/danger-60}
         style))

(defn swipe-primary-container
  [style]
  (merge swipe-base
         {:background-color colors/primary-60}
         style))

(def swipe-text
  {:margin-top 5
   :color      colors/white})

(def swipe-text-wrapper
  {:align-items :center})
