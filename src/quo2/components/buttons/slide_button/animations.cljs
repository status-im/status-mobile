(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts :as consts]
   [react-native.gesture :as gesture]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

;; Utils 
(defn- clamp-value [value min-value max-value]
  (cond
    (< value min-value) min-value
    (> value max-value) max-value
    :else value))

(defn calc-usable-track
  "Calculate the track section in which the
  thumb can move in. Mostly used for interpolations."
  [track-width thumb-size]
  (- (or @track-width 200) (* consts/track-padding 2) thumb-size))

(def ^:private extrapolation {:extrapolateLeft  "clamp"
                              :extrapolateRight "clamp"})

(defn- track-interpolation-inputs
  [in-vectors track-width]
  (map #(* track-width %) in-vectors))

;; Interpolations
(defn- track-clamp-interpolation
  [track-width]
  {:in [-1 0 1]
   :out [track-width 0 track-width]})

(defn- track-cover-interpolation
  [track-width thumb-size]
  {:in [0 1]
   :out [(/ thumb-size 2) track-width]})

(defn- arrow-icon-position-interpolation
  [thumb-size]
  {:in [0.9 1]
   :out [0 (- thumb-size)]})

(defn- action-icon-position-interpolation
  [thumb-size]
  {:in [0.9 1]
   :out [thumb-size 0]})

(defn interpolate-track
  "Interpolate the position in the track
  `x-pos`            Track animated value
  `track-width`      Usable width of the track
  `thumb-size`       Size of the thumb
  `interpolation` `  :thumb-border-radius`/`:thumb-drop-position`/`:thumb-drop-scale`/`:thumb-drop-z-index`/..."
  ([x-pos track-width thumb-size interpolation]
   (let [interpolations {:track-cover (track-cover-interpolation track-width thumb-size)
                         :track-clamp (track-clamp-interpolation track-width)
                         :action-icon-position (action-icon-position-interpolation thumb-size)
                         :arrow-icon-position (arrow-icon-position-interpolation thumb-size)}

         interpolation-values (interpolation interpolations)
         output (:out interpolation-values)
         input (-> (:in interpolation-values)
                   (track-interpolation-inputs track-width))]
     (if (nil? interpolation-values)
       x-pos
       (reanimated/interpolate x-pos
                               input
                               output
                               extrapolation)))))

;; Animations
(defn- animate-spring
  [value to-value]
  (reanimated/animate-shared-value-with-spring value
                                               to-value
                                               {:mass      1
                                                :damping   30
                                                :stiffness 400}))

(defn complete-animation
  [sliding-complete?]
  (js/setTimeout (fn [] (reset! sliding-complete? true)) 100))

(defn reset-track-position
  [x-pos]
  (animate-spring x-pos 0))

;; Gestures
(defn drag-gesture
  [x-pos
   disabled?
   track-width
   sliding-complete?]
  (-> (gesture/gesture-pan)
      (gesture/enabled (not @disabled?))
      (gesture/min-distance 0)
      (gesture/on-update (fn [event]
                           (let [x-translation (oops/oget event "translationX")
                                 clamped-x (clamp-value x-translation 0 track-width)
                                 reached-end? (>= clamped-x track-width)]
                             (reanimated/set-shared-value x-pos clamped-x)
                             (when (and reached-end? (not @sliding-complete?))
                               (reset! disabled? true)
                               (complete-animation sliding-complete?)))))
      (gesture/on-end (fn [event]
                        (let [x-translation (oops/oget event "translationX")
                              reached-end? (>= x-translation track-width)]
                          (when (not reached-end?)
                            (reset-track-position x-pos)))))))

