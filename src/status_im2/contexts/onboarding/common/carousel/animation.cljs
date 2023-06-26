(ns status-im2.contexts.onboarding.common.carousel.animation
  (:require
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [utils.worklets.onboarding-carousel :as worklets.onboarding-carousel]
    [react-native.gesture :as gesture]
    [oops.core :as oops]))

(def ^:const progress-bar-animation-delay 300)
(def ^:const progress-bar-animation-duration 4000)
(def ^:const initial-division-quotient 5)
(def ^:const progress-threshold 25)
(def ^:const drag-limit 100)

(defn slide-animation
  [progress-percentage & [duration]]
  (reanimated/with-delay
   progress-bar-animation-delay
   (reanimated/with-timing
    progress-percentage
    (js-obj "duration" (or duration progress-bar-animation-duration)
            "easing"   (:linear reanimated/easings)))))

(defn calculate-remainder
  [dividend divisor]
  (if (and (pos-int? dividend) (zero? (mod dividend divisor)))
    divisor
    (mod dividend divisor)))

(defn animation-value
  [q index]
  (let [next-quotient (+ q index)
        dividend      (if (>= next-quotient initial-division-quotient)
                        (* (- next-quotient initial-division-quotient) progress-threshold)
                        (* next-quotient progress-threshold))]
    (calculate-remainder dividend drag-limit)))

(defn animate-progress-value
  [value]
  (if (zero? value) (slide-animation value 0) (slide-animation value)))

(defn animate-progress
  [progress paused next-progress]
  (let [q (quot next-progress 25)]
    (reanimated/set-shared-value
     progress
     (reanimated/with-pause
      (reanimated/with-repeat
       (reanimated/with-sequence
        (animate-progress-value (animation-value q 1))
        (animate-progress-value (animation-value q 2))
        (animate-progress-value (animation-value q 3))
        (animate-progress-value (animation-value q 4))
        (animate-progress-value (calculate-remainder next-progress drag-limit)))
       -1)
      paused))))

(defn get-next-progress
  [progress]
  (let [current-progress (reanimated/get-shared-value @progress)]
    (if (>= current-progress drag-limit)
      drag-limit
      (-> (quot current-progress progress-threshold)
          (+ 1)
          (* progress-threshold)))))

(defn get-previous-progress
  [progress]
  (let [current-progress (reanimated/get-shared-value @progress)]
    (if (< current-progress progress-threshold)
      0
      (-> (quot current-progress progress-threshold)
          (- 1)
          (* progress-threshold)))))

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

(defn drag-gesture
  [progress paused]
  (->
    (gesture/gesture-pan)
    (gesture/enabled true)
    (gesture/max-pointers 1)
    (gesture/on-finalize
     (fn [event]
       (let [next?     (< (oops/oget event "translationX") (- drag-limit))
             previous? (> (oops/oget event "translationX") drag-limit)]
         (when next?
           (update-progress progress paused (get-next-progress progress)))
         (when previous?
           (update-progress progress paused (get-previous-progress progress))))))))

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
