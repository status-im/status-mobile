(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [react-native.gesture :as gesture]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

;; Utils 
(defn calc-usable-track
  "Calculate the track section in which the
  thumb can move in. Mostly used for interpolations."
  [track-width thumb-size]
  (- (or @track-width 200) (* consts/track-padding 2) thumb-size))

(def ^:private extrapolation {:extrapolateLeft  "clamp"
                              :extrapolateRight "clamp"})

(defn calc-final-padding
  "Calculate the padding animation applied
  to the track to surround the thumb."
  [track-width thumb-size]
  (-> track-width
      (/ 2)
      (- (/ thumb-size 2))
      (- consts/track-padding)))

;; Interpolations
(defn clamp-track
  "Clamps the thumb position to the usable portion
   of the track"
  [x-pos track-width thumb-size]
  (let [track-dim [0 (calc-usable-track track-width thumb-size)]]
    (reanimated/interpolate
     x-pos
     track-dim
     track-dim
     extrapolation)))

(defn interpolate-track-cover
  "Interpolates the start edge of the track text container
  based on the thumb position, which should hide the text
  behind the thumb."
  [x-pos track-width thumb-size]
  (let [usable-track (calc-usable-track track-width thumb-size)
        output-start-pos (/ thumb-size 2)
        clamped (clamp-track x-pos track-width thumb-size)]
    (reanimated/interpolate
     clamped
     [0 usable-track]
     [output-start-pos usable-track]
     extrapolation)))

;; Gestures
(defn- gesture-on-update
  [event
   offset
   x-pos
   slide-state
   track-width
   thumb-size]
  (let [x-translation (oops/oget event "translationX")
        x (+ x-translation @offset)
        reached-end? (>= x (calc-usable-track track-width thumb-size))]
    (doall [(when (not reached-end?)
              (reanimated/set-shared-value x-pos x))
            (doall [(when (= @slide-state :rest)
                      (reset! slide-state :dragging))
                    (when reached-end?
                      (reset! slide-state :complete))])])))

(defn- gesture-on-end
  [event
   offset
   complete-threshold
   thumb-state]
  (let [x-translation (oops/oget event "translationX")
        x (+ x-translation @offset)]
    (if (<= x complete-threshold)
      (reset! thumb-state :incomplete)
      (reset! thumb-state :complete))))

(defn- gesture-on-start
  [event
   x-pos
   offset
   thumb-state]
  (let [x-translation (oops/oget event "translationX")]
    (reanimated/set-shared-value x-pos x-translation)
    (reset! thumb-state :dragging)
    (reset! offset (reanimated/get-shared-value x-pos))))

(defn drag-gesture
  [{:keys [x-pos]}
   disabled?
   track-width
   thumb-state
   thumb-size]
  (let [offset (react/state 0)
        complete-threshold (* @track-width consts/threshold-frac)]
    (-> (gesture/gesture-pan)
        (gesture/enabled (not disabled?))
        (gesture/min-distance 0)
        (gesture/on-update
         (fn [event]
           (gesture-on-update event offset x-pos thumb-state track-width thumb-size)))
        (gesture/on-end
         (fn [event]
           (gesture-on-end event offset complete-threshold thumb-state)))
        (gesture/on-start
         (fn [event] (gesture-on-start event x-pos offset thumb-state))))))

;; Animation helpers
(defn- animate-spring
  [value to-value]
  (reanimated/animate-shared-value-with-spring
   value
   to-value
   {:mass      1
    :damping   6
    :stiffness 300}))

(defn- animate-timing
  [value to-position duration]
  (reanimated/animate-shared-value-with-timing
   value to-position duration :linear))

(defn- animate-sequence [anim & seq-animations]
  (reanimated/set-shared-value
   anim
   (apply reanimated/with-sequence seq-animations)))

;; Animations
(defn init-animations [] {:x-pos (reanimated/use-shared-value 0)
                          :thumb-border-radius (reanimated/use-shared-value 12)
                          :track-scale (reanimated/use-shared-value 1)
                          :track-border-radius (reanimated/use-shared-value 14)
                          :track-container-padding (reanimated/use-shared-value 0)})

(defn animate-reset-thumb [{:keys [x-pos]}]
  (animate-timing x-pos 0 200))

(def ^:private shrink-duration 300)
(defn animate-shrink-track [{:keys [track-container-padding]} final-padding]
  (animate-timing
   track-container-padding final-padding shrink-duration))

(defn animate-center-thumb [{:keys [x-pos]}]
  (animate-timing x-pos 0 shrink-duration))

(defn animate-round-track-thumb [{:keys [track-border-radius thumb-border-radius]}]
  ((animate-timing track-border-radius 100 shrink-duration)
   (animate-timing thumb-border-radius 100 shrink-duration)))

(defn animate-scale-track [{:keys [:track-scale]}]
  (animate-sequence track-scale
                    (animate-timing track-scale 1.2 200)
                    (animate-timing track-scale 0.8 200)
                    (animate-spring track-scale 1)))

