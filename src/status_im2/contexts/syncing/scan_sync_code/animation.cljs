(ns status-im2.contexts.syncing.scan-sync-code.animation
  (:require [react-native.reanimated :as reanimated]
            [status-im2.constants :as constants]))

(defn animate-subtitle
  [subtitle-opacity]
  (reanimated/animate-shared-value-with-delay
   subtitle-opacity
   1
   constants/onboarding-modal-animation-duration
   :easing4
   (/ constants/onboarding-modal-animation-delay 2)))

(defn animate-title
  [title-opacity]
  (reanimated/animate-shared-value-with-delay
   title-opacity
   1
   0
   :easing4
   (+ constants/onboarding-modal-animation-duration
      constants/onboarding-modal-animation-delay)))

(defn animate-bottom
  [bottom-view-translate-y]
  (reanimated/animate-delay
   bottom-view-translate-y
   0
   (+ constants/onboarding-modal-animation-duration
      constants/onboarding-modal-animation-delay)
   100))

(defn animate-content
  [content-opacity]
  (reanimated/animate-shared-value-with-delay
   content-opacity
   1
   constants/onboarding-modal-animation-duration
   :easing4
   (/ constants/onboarding-modal-animation-delay 2)))

(defn reset-animations
  [{:keys [content-opacity subtitle-opacity title-opacity]}]
  (reanimated/animate-shared-value-with-timing
   content-opacity
   0
   (/ constants/onboarding-modal-animation-duration 8)
   :easing4)

  (reanimated/animate-shared-value-with-timing
   subtitle-opacity
   0
   (- constants/onboarding-modal-animation-duration
      constants/onboarding-modal-animation-delay)
   :easing4)

  (reanimated/animate-shared-value-with-timing
   title-opacity
   0
   0
   :easing4))
