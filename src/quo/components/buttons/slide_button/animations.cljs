(ns quo.components.buttons.slide-button.animations
  (:require [react-native.reanimated :as reanimated]))

(def ^:private extrapolation
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn- track-interpolation-inputs
  [in-vectors track-width]
  (map (partial * track-width) in-vectors))

;; Interpolations
(defn- track-clamp-interpolation
  [track-width]
  {:in  [-1 0 1]
   :out [track-width 0 track-width]})

(defn- track-cover-interpolation
  [track-width thumb-size]
  {:in  [0 1]
   :out [thumb-size (+ track-width thumb-size)]})

(defn- arrow-icon-position-interpolation
  [thumb-size]
  {:in  [0.9 1]
   :out [0 (- thumb-size)]})

(defn- action-icon-position-interpolation
  [thumb-size]
  {:in  [0.9 1]
   :out [thumb-size 0]})

(defn interpolate-track
  "Interpolate the position in the track
  `x-pos`            Track animated value
  `track-width`      Usable width of the track
  `thumb-size`       Size of the thumb
  `interpolation` `  :thumb-border-radius`/`:thumb-drop-position`/`:thumb-drop-scale`/`:thumb-drop-z-index`/..."
  ([x-pos track-width thumb-size interpolation]
   (let [interpolations       {:track-cover          (track-cover-interpolation track-width thumb-size)
                               :track-clamp          (track-clamp-interpolation track-width)
                               :action-icon-position (action-icon-position-interpolation thumb-size)
                               :arrow-icon-position  (arrow-icon-position-interpolation thumb-size)}

         interpolation-values (interpolation interpolations)
         output               (:out interpolation-values)
         input                (-> (:in interpolation-values)
                                  (track-interpolation-inputs track-width))]
     (if interpolation-values
       (reanimated/interpolate x-pos
                               input
                               output
                               extrapolation)
       x-pos))))

;; Animations
(defn- animate-spring
  [value to-value]
  (reanimated/animate-shared-value-with-spring value
                                               to-value
                                               {:mass      1
                                                :damping   30
                                                :stiffness 400}))

(defn reset-track-position
  [x-pos]
  (animate-spring x-pos 0))
