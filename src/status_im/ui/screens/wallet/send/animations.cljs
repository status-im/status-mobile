(ns status-im.ui.screens.wallet.send.animations
  (:require [status-im.ui.components.animation :as animation]))

(defn animate-sign-panel [opacity-value bottom-value]
  (animation/start
   (animation/parallel
    [(animation/timing opacity-value {:toValue  1
                                      :duration 500})
     (animation/timing bottom-value {:toValue  53
                                     :easing   (.bezier (animation/easing) 0.685, 0.000, 0.025, 1.185)
                                     :duration 500})])))
