(ns quo2.components.record-audio.record-audio.helpers
  (:require [react-native.reanimated :as reanimated]))

(defn animate-linear
  [shared-value value duration]
  (reanimated/animate-shared-value-with-timing
   shared-value
   value
   duration
   :linear))

(defn animate-linear-with-delay
  [shared-value value duration delay-ms]
  (reanimated/animate-shared-value-with-delay
   shared-value
   value
   duration
   :linear
   delay-ms))

(defn animate-linear-with-delay-loop
  [shared-value value duration delay-ms]
  (reanimated/animate-shared-value-with-delay-repeat
   shared-value
   value
   duration
   :linear
   delay-ms
   -1))

(defn animate-easing
  [shared-value value duration]
  (reanimated/animate-shared-value-with-timing
   shared-value
   value
   duration
   :easing1))

(defn animate-easing-with-delay
  [shared-value value duration delay-ms]
  (reanimated/animate-shared-value-with-delay
   shared-value
   value
   duration
   :easing1
   delay-ms))

(defn set-value
  [shared-value value]
  (reanimated/set-shared-value shared-value value))
