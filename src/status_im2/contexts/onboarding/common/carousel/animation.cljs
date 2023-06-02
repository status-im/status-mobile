(ns status-im2.contexts.onboarding.common.carousel.animation
  (:require
   [react-native.core :as rn]
   [react-native.reanimated :as reanimated]
   [utils.worklets.onboarding-carousel :as worklets.onboarding-carousel]))

(def ^:const progress-bar-animation-delay 300)
(def ^:const progress-bar-animation-duration 4000)
(def ^:const initial-division-quotient 5)

(defn slide-animation
  [progress-percentage & [delay duration]]
  (reanimated/with-delay
    (or delay progress-bar-animation-delay)
    (reanimated/with-timing
      progress-percentage
      (js-obj "duration" (or duration progress-bar-animation-duration)
              "easing"   (:linear reanimated/easings)))))

(defn calculate-remainder
  [dividend divisor]
  (if (and (not= 0 dividend) (zero? (mod dividend divisor)))
    divisor
    (mod dividend divisor)))

(defn animation-value
  [q index]
  (let [next-quotient (+ q index)]
    (calculate-remainder (* (if (>= next-quotient initial-division-quotient)
                              (- next-quotient initial-division-quotient)
                              next-quotient)
                            25)
                         100)))

(defn animate-progress-value
  [value]
  (if (= value 0) (slide-animation value 300 0) (slide-animation value)))

(defn animate-progress
  [progress paused default]
  (let [q (quot default 25)]
    (reanimated/set-shared-value
     progress
     (reanimated/with-pause
       (reanimated/with-repeat
         (reanimated/with-sequence
           (animate-progress-value (animation-value q 1))
           (animate-progress-value (animation-value q 2))
           (animate-progress-value (animation-value q 3))
           (animate-progress-value (animation-value q 4))
           (animate-progress-value (calculate-remainder default 100)))
         -1)
       paused))))

(defn use-initialize-animation
  [progress paused animate?]
  (reset! progress (reanimated/use-shared-value 0))
  (reset! paused (reanimated/use-shared-value false))
  (rn/use-effect
   (fn []
     (animate-progress @progress @paused 0))
   [animate?]))

(defn cleanup-animation
  [progress paused]
  (fn []
    (reanimated/cancel-animation @progress)
    (reanimated/cancel-animation @paused)))

(defn update-progress
  [progress paused new-progress]
  (reanimated/set-shared-value @progress new-progress)
  (animate-progress @progress @paused new-progress))

(defn carousel-left-position
  [window-width animate? progress]
  (if animate?
    (worklets.onboarding-carousel/carousel-left-position window-width @progress)
    (-> (or (reanimated/get-shared-value @progress) 0)
        (quot -25)
        (* window-width))))

(defn dynamic-progress-bar-width
  [progress-bar-width animate? progress]
  (if animate?
    (worklets.onboarding-carousel/dynamic-progress-bar-width progress-bar-width @progress)
    (-> (or (reanimated/get-shared-value @progress) 0)
        (* progress-bar-width)
        (/ 100))))
