(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.utils.utils :as utils]))

(defn easing [direction n]
  {:toValue         n
   :easing          ((if (= :in direction)
                       (animation/easing-in)
                       (animation/easing-out))
                     (.-quad (animation/easing)))
   :duration        400
   :useNativeDriver true})

(defn animated-bar-style [margin-value width color]
  {:position         :absolute
   :width            width
   :transform        [{:translateX
                       (animation/interpolate
                        margin-value
                        {:inputRange  [0 1]
                         :outputRange [0 width]})}]
   :height           3
   :background-color color})

(views/defview loading-indicator [parent-width]
  (views/letsubs [blue-bar-left-margin (animation/create-value 0)
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
    [react/view {:style {:width parent-width
                         :position :absolute
                         :top -3
                         :height 3
                         :background-color colors/white}}
     [(react/animated-view) {:style (animated-bar-style blue-bar-left-margin
                                                        parent-width
                                                        colors/blue)}]
     [(react/animated-view) {:style (assoc (animated-bar-style white-bar-left-margin
                                                               parent-width
                                                               colors/white)
                                           :left (* 0.15 parent-width))}]]))

(defonce show-connected? (reagent/atom true))

(defn manage-visibility [connected? anim-opacity anim-height]
  (if connected?
    (do (animation/start
         (animation/parallel
          [(animation/timing anim-opacity
                             {:toValue 0
                              :delay 800
                              :duration 150
                              :easing (.-ease (animation/easing))})
           (animation/timing anim-height
                             {:toValue 0
                              :delay 800
                              :duration 150
                              :easing (.-ease (animation/easing))})]))
        (utils/set-timeout
         #(reset! show-connected? false)
         2000))
    (do (reset! show-connected? true)
        (animation/start
         (animation/parallel
          [(animation/timing anim-opacity
                             {:toValue 1
                              :duration 150
                              :easing (.-ease (animation/easing))})
           (animation/timing anim-height
                             {:toValue 35
                              :duration 150
                              :easing (.-ease (animation/easing))})])))))

(defn connectivity-status
  [{:keys [connected?]}]
  (let [anim-opacity (animation/create-value 0)
        anim-height (animation/create-value 0)]
    (manage-visibility connected?
                       anim-opacity anim-height)
    (reagent/create-class
     {:component-did-update
      (fn [comp]
        (manage-visibility (:connected? (reagent/props comp))
                           anim-opacity anim-height))
      :reagent-render
      (fn [{:keys [view-id message on-press-fn
                   connected? connecting? loading-indicator?] :as opts}]
        (when (or (not connected?)
                  @show-connected?)
          [(react/animated-view) {:style               (styles/text-wrapper
                                                        (assoc opts
                                                               :height anim-height
                                                               :background-color (if connected?
                                                                                   colors/green
                                                                                   colors/gray)
                                                               :opacity anim-opacity
                                                               :modal? (= view-id :chat-modal)))
                                  :accessibility-label :connection-status-text}
           (when connecting?
             [react/activity-indicator {:animated     true
                                        :color        colors/white
                                        :margin-right 6}])
           (if (= message :mobile-network)
             [react/nested-text {:style    styles/text
                                 :on-press on-press-fn}
              (i18n/label :t/waiting-for-wifi) " "
              [{:style {:text-decoration-line :underline}}
               (i18n/label :t/waiting-for-wifi-change)]]
             [react/text {:style    styles/text
                          :on-press on-press-fn}
              (i18n/label message)])]))})))

(defview connectivity-view []
  (letsubs [status-properties [:connectivity/status-properties]
            view-id           [:view-id]
            window-width      [:dimensions/window-width]]
    (let [{:keys [loading-indicator?]} status-properties]
      [react/view {:style {:align-self :flex-start}}
       (when loading-indicator?
         [loading-indicator window-width])
       [connectivity-status
        (merge status-properties
               {:view-id      view-id
                :window-width window-width})]])))
