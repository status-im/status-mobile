(ns status-im2.contexts.onboarding.common.carousel.animation
  (:require
    [react-native.reanimated :as reanimated]
    [utils.worklets.onboarding-carousel :as worklets.onboarding-carousel]))

(def progress (atom nil))
(def paused (atom nil))

(def ^:const progress-bar-animation-delay 300)
(def ^:const progress-bar-animation-duration 4000)

(defn slide-animation
  [progress-percentage & [delay duration]]
  (reanimated/with-delay
   (or delay progress-bar-animation-delay)
   (reanimated/with-timing
    progress-percentage
    (js-obj "duration" (or duration progress-bar-animation-duration)
            "easing"   (:linear reanimated/easings)))))

(defn animate-progress
  [progress paused]
  (reanimated/set-shared-value
   progress
   (reanimated/with-pause
    (reanimated/with-repeat
     (reanimated/with-sequence
      (slide-animation 25)
      (slide-animation 50)
      (slide-animation 75)
      (slide-animation 100)
      (slide-animation 0 300 0))
     -1)
    paused)))

(defn initialize-animation
  []
  (reset! progress (reanimated/use-shared-value 0))
  (reset! paused (reanimated/use-shared-value false))
  (animate-progress @progress @paused))

(defn carousel-left-position
  [window-width animate?]
  (if animate?
    (worklets.onboarding-carousel/carousel-left-position window-width @progress)
    (-> (or (reanimated/get-shared-value @progress) 0)
        (quot -25)
        (* window-width))))

(defn dynamic-progress-bar-width
  [progress-bar-width animate?]
  (if animate?
    (worklets.onboarding-carousel/dynamic-progress-bar-width progress-bar-width @progress)
    (-> (or (reanimated/get-shared-value @progress) 0)
        (* progress-bar-width)
        (/ 100))))
