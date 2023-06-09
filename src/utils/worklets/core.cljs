(ns utils.worklets.core)

(def core-js (js/require "../src/js/worklets/core.js"))

(defn interpolate-value
  [shared-value
   input-range
   output-range
   extrapolation]
  (.interpolateValue ^js core-js
                     shared-value
                     (clj->js input-range)
                     (clj->js output-range)
                     (clj->js extrapolation)))

(defn interpolate-color-value
  [shared-value
   input-range
   output-range
   color-space
   options]
  (.interpolateColorValue ^js core-js
                          shared-value
                          (clj->js input-range)
                          (clj->js output-range)
                          color-space
                          (clj->js options)))

(defn apply-animations-to-style
  [animations style]
  (.applyAnimationsToStyle ^js core-js (clj->js animations) (clj->js style)))
