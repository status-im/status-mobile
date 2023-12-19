(ns legacy.status-im.ui.components.tooltip.animations
  (:require
    [legacy.status-im.ui.components.animation :as animation]))

(defn animate-tooltip
  [bottom-value bottom-anim-value opacity-value delta]
  (fn []
    (animation/start
     (animation/parallel
      [(animation/timing opacity-value
                         {:toValue         1
                          :duration        500
                          :useNativeDriver true})
       (animation/timing bottom-anim-value
                         {:toValue         (- bottom-value delta)
                          :easing          (.bezier ^js animation/easing 0.685 0.000 0.025 1.185)
                          :duration        500
                          :useNativeDriver true})]))))
