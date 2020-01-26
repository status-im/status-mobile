(ns status-im.ui.components.loading-spinner
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.svg :as svg]
            [status-im.ui.components.animation :as animation]
            [reagent.core :as reagent]))

(defn- circle-stroke-dasharray [degrees radius]
  "Returns the stroke-dasharray and stroke-dashoffset required to achieve a 
  stroke that covers degrees of a circle with radius"
  (let [radians (* degrees (/ Math/PI 180))
        circumference (* 2 Math/PI radius)
        arc-length (* radians radius)]
    {:stroke-dasharray circumference
     :stroke-dashoffset (- circumference arc-length)}))

(defn- spinner []
  (let [rotation (animation/create-value 0)]
    (reagent/create-class 
      {:component-did-mount
       (fn [_]
        (animation/start
          (animation/anim-loop
            (animation/timing 
              rotation
              {:toValue 1
               :duration 1000
               :easing (.-linear (animation/easing))}))))
       :reagent-render
       (fn [] 
         [react/animated-view
          {:style {:transform [{:rotate 
                                (animation/interpolate 
                                  rotation 
                                  {:inputRange [0, 1]
                                   :outputRange ["0deg", "360deg"]})}]}}
          [svg/svgview {:height 40 :width 40 :style {:padding 5}} 
           [svg/circle (merge
                         {:cx 20
                          :cy 20
                          :fill "none"
                          :stroke colors/blue
                          :stroke-width 3
                          :stroke-linecap "round"
                          :r 15}
                         (circle-stroke-dasharray 260 15))]]])})))

(views/defview loading-spinner []
  (views/letsubs [visibility-state (reagent/atom :invisible)
                  visibility (animation/create-value 0)]
    ;{:component-did-mount
     ;(fn [_]
       ;(animation/start
         ;(animation/timing
           ;visibility
           ;{:toValue 1
            ;:duration 500
            ;:useNativeDriver false
            ;:easing (.-linear (animation/easing))})))}
    [react/animated-view 
     {:style 
      {:width "100%"
       :flex 1
       :align-items :center
       :justify-content :center
       :opacity (animation/interpolate
                  visibility
                  {:inputRange [0,1]
                   :outputRange [0,1]})
       :height (animation/interpolate
                 visibility
                 {:inputRange [0,1]
                  :outputRange [0,86]})}}
      [spinner]]))
