(ns quo2.components.record-audio.record-audio.helpers
  (:require [react-native.reanimated :as ra]))

(defn animate-linear
  [shared-value value duration]
  (ra/animate
   shared-value
   value
   duration
   :linear))

(defn animate-linear-with-delay
  [shared-value value duration delay]
  (ra/animate-delay
   shared-value
   value
   delay
   duration
   :linear))

(defn animate-linear-with-delay-loop
  [shared-value value duration delay]
  (ra/animate-delay-repeat
   shared-value
   value
   delay
   -1
   duration
   :linear))

(defn animate-easing
  [shared-value value duration]
  (ra/animate
   shared-value
   value
   duration
   :easing1))

(defn animate-easing-with-delay
  [shared-value value duration delay]
  (ra/animate-delay
   shared-value
   value
   delay
   duration
   :easing1))

(defn set-value
  [shared-value value]
  (ra/set-val shared-value value))
