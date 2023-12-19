(ns legacy.status-im.ui.screens.keycard.components.description
  (:require
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.keycard.components.style :as styles]
    [reagent.core :as reagent]))

(defn text-block-style
  [animated]
  {:height        66
   :margin-bottom 8
   :opacity       (animation/interpolate animated
                                         {:inputRange  [0 1]
                                          :outputRange [1 0]})
   :transform     [{:translateY (animation/interpolate animated
                                                       {:inputRange  [0 1]
                                                        :outputRange [0 10]})}]})

(def easing (animation/bezier 0.77 0 0.175 1))

(defonce animating (atom nil))

(defn animate-description
  [animated]
  (when-not @animating
    (reset! animating true)
    ;; TODO; Animate exit
    (animation/set-value animated 1)
    (animation/start
     (animation/timing animated
                       {:toValue 0
                        :timing  300
                        :easing  easing})
     #(reset! animating false))))

(defn animated-description
  []
  (let [current-text   (reagent/atom nil)
        animated-value (animation/create-value 0)]
    (fn [{:keys [title description]}]
      (when-not (= @current-text [title description])
        (reset! current-text [title description])
        (animate-description animated-value))
      [react/animated-view {:style (text-block-style animated-value)}
       [react/text
        {:style           styles/title-style
         :number-of-lines 1}
        title]
       [react/text
        {:style           styles/helper-text-style
         :number-of-lines 2}
        description]])))
