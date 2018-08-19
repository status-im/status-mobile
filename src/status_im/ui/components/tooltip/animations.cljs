(ns status-im.ui.components.tooltip.animations
  (:require [status-im.ui.components.animation :as animation]))

(defn animate-tooltip [bottom-value bottom-anim-value opacity-value delay delta]
  (fn []
    (let [tooltip-animation (animation/parallel
                             [(animation/timing opacity-value
                                                {:toValue  1
                                                 :duration 500})
                              (animation/timing bottom-anim-value
                                                {:toValue  (- bottom-value delta)
                                                 :easing   (.bezier (animation/easing) 0.685, 0.000, 0.025, 1.185)
                                                 :duration 500})])]
      (animation/start
       (if delay
         (animation/anim-sequence
          [(animation/anim-delay delay)
           tooltip-animation])
         tooltip-animation)))))
