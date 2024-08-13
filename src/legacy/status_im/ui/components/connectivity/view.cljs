(ns legacy.status-im.ui.components.connectivity.view
  (:require
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn easing
  [direction n]
  {:toValue         n
   :easing          ((if (= :in direction)
                       (animation/easing-in)
                       (animation/easing-out))
                     (.-quad ^js animation/easing))
   :duration        400
   :useNativeDriver true})

(defn animated-bar-style
  [margin-value width color]
  {:position         :absolute
   :width            width
   :transform        [{:translateX
                       (animation/interpolate
                        margin-value
                        {:inputRange  [0 1]
                         :outputRange [0 width]})}]
   :height           3
   :background-color color})

(views/defview loading-indicator-anim
  [parent-width]
  (views/letsubs [blue-bar-left-margin  (animation/create-value 0)
                  white-bar-left-margin (animation/create-value 0)]
    {:component-did-mount
     (fn [_]
       (animation/start
        (animation/anim-loop
         (animation/anim-sequence
          [(animation/parallel
            [(animation/timing blue-bar-left-margin (easing :in 0.19))
             (animation/timing white-bar-left-margin (easing :in 0.65))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :out 0.85))
             (animation/timing white-bar-left-margin (easing :out 0.85))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :in 0.19))
             (animation/timing white-bar-left-margin (easing :in 0.65))])
           (animation/parallel
            [(animation/timing blue-bar-left-margin (easing :out 0))
             (animation/timing white-bar-left-margin (easing :out 0))])]))))}
    [react/view
     [react/view
      {:style               {:width            parent-width
                             :position         :absolute
                             :top              -3
                             :z-index          3
                             :height           3
                             :background-color colors/white}
       :accessibility-label :loading-indicator}
      [react/animated-view
       {:style (animated-bar-style blue-bar-left-margin
                                   parent-width
                                   colors/blue)}]
      [react/animated-view
       {:style (assoc (animated-bar-style white-bar-left-margin
                                          parent-width
                                          colors/white)
                      :left
                      (* 0.15 parent-width))}]]]))
