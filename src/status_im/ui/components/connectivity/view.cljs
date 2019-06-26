(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]))

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
     [react/animated-view {:style (animated-bar-style blue-bar-left-margin
                                                      parent-width
                                                      colors/blue)}]
     [react/animated-view {:style (assoc (animated-bar-style white-bar-left-margin
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
                              :easing (.-ease (animation/easing))
                              :useNativeDriver true})
           (animation/timing anim-height
                             {:toValue         (if platform/desktop? 0 -35)
                              :delay           800
                              :duration        150
                              :easing          (.-ease (animation/easing))
                              :useNativeDriver true})]))
        (utils/set-timeout
         #(reset! show-connected? false)
         2000))
    (do (reset! show-connected? true)
        (animation/start
         (animation/parallel
          [(animation/timing anim-opacity
                             {:toValue 1
                              :duration 150
                              :easing (.-ease (animation/easing))
                              :useNativeDriver true})
           (animation/timing anim-height
                             {:toValue (if platform/desktop? 35 0)
                              :duration 150
                              :easing (.-ease (animation/easing))
                              :useNativeDriver true})])))))

(defn connectivity-status
  [{:keys [connected?]} anim-translate-y]
  (let [anim-translate-y (or anim-translate-y (animation/create-value 0))
        anim-opacity     (animation/create-value 0)]
    (manage-visibility connected?
                       anim-opacity anim-translate-y)
    (reagent/create-class
     {:component-did-update
      (fn [comp]
        (manage-visibility (:connected? (reagent/props comp))
                           anim-opacity anim-translate-y))
      :reagent-render
      (fn [{:keys [view-id message on-press-fn
                   connected? connecting?] :as opts}]
        [react/animated-view {:style               (styles/text-wrapper
                                                    (assoc opts
                                                           :height (if platform/desktop?
                                                                     anim-translate-y
                                                                     35)
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
            (i18n/label message)])])})))

(defn connectivity-animation-wrapper [style anim-value & content]
  (vec (concat
        (if platform/desktop?
          [react/view {:style {:flex 1}}]
          [react/animated-view
           {:style
            (merge {:flex      1
                    :transform [{:translateY anim-value}]}
                   style)}])
        content)))

(defview connectivity-view [anim-translate-y]
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
                :window-width window-width})
        anim-translate-y]])))
