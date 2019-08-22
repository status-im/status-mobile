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

(def connectivity-bar-height 35)
(def neg-connectivity-bar-height (- connectivity-bar-height))

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
                         :z-index 3
                         :height 3
                         :background-color colors/white}}
     [react/animated-view {:style (animated-bar-style blue-bar-left-margin
                                                      parent-width
                                                      colors/blue)}]
     [react/animated-view {:style (assoc (animated-bar-style white-bar-left-margin
                                                             parent-width
                                                             colors/white)
                                         :left (* 0.15 parent-width))}]]))

(def to-hide? (reagent/atom false))

(defn manage-visibility [connected? anim-opacity anim-y]
  (if connected?
    (when @to-hide?
      (animation/start
       (animation/parallel
        [(animation/timing anim-opacity
                           {:toValue 0
                            :delay 800
                            :duration 150
                            :easing (.-ease (animation/easing))
                            :useNativeDriver true})
         (animation/timing anim-y
                           {:toValue         (if platform/desktop? 0 neg-connectivity-bar-height)
                            :delay           800
                            :duration        150
                            :easing          (.-ease (animation/easing))
                            :useNativeDriver true})])
       ;; second param of start() - a callback that fires when animation stops
       #(reset! to-hide? false)))
    ;; else
    (animation/start
     (animation/parallel
      [(animation/timing anim-opacity
                         {:toValue 1
                          :duration 150
                          :easing (.-ease (animation/easing))
                          :useNativeDriver true})
       (animation/timing anim-y
                         {:toValue (if platform/desktop? connectivity-bar-height 0)
                          :duration 150
                          :easing (.-ease (animation/easing))
                          :useNativeDriver true})])
     ;; second param of start() - a callback that fires when animation stops
     #(reset! to-hide? true))))

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
      (fn [{:keys [view-id message on-press-fn connected? connecting?] :as opts}]
        [react/animated-view {:style               (styles/text-wrapper
                                                    (assoc opts
                                                           :height (if platform/desktop?
                                                                     anim-translate-y
                                                                     connectivity-bar-height)
                                                           :background-color (if connected?
                                                                               colors/green
                                                                               colors/gray)
                                                           ;;TODO how does this affect desktop?
                                                           :transform      anim-translate-y
                                                           :opacity anim-opacity
                                                           :modal? (= view-id :chat-modal)))
                              :accessibility-label :connection-status-text}
         (when connecting?
           [react/activity-indicator {:color colors/white :margin-right 6}])
         (if (= message :mobile-network)
           [react/nested-text {:style    styles/text
                               :on-press on-press-fn}
            (i18n/label :t/waiting-for-wifi) " "
            [{:style {:text-decoration-line :underline}}
             (i18n/label :t/waiting-for-wifi-change)]]
           [react/text {:style    styles/text
                        :on-press on-press-fn}
            (i18n/label message)])])})))

(defview connectivity-view [anim-translate-y]
  (letsubs [status-properties [:connectivity/status-properties]
            view-id           [:view-id]
            window-width (reagent/atom 0)]
    {:component-did-mount
     (fn []
       (when anim-translate-y
         (if (:connected? status-properties)
           (animation/set-value anim-translate-y neg-connectivity-bar-height)
           (animation/set-value anim-translate-y 0))))}
    (let [{:keys [loading-indicator?]} status-properties]
      [react/view {:style     {:align-items :stretch
                               :z-index     1}
                   :on-layout #(reset! window-width (-> % .-nativeEvent .-layout .-width))}
       (when loading-indicator?
         [loading-indicator @window-width])
       ;; This view below exists only to hide the connectivity-status bar when "connected".
       ;; Ideally connectivity-status bar would be hidden under "toolbar/toolbar",
       ;; but that has to be transparent(enven though it sits above the bar)
       ;; to show through the "loading-indicator"
       ;; TODO consider making the height the same height as the "toolbar/toolbar"
       [react/view {:position :absolute
                    :top neg-connectivity-bar-height
                    :width @window-width
                    :z-index 2
                    :height connectivity-bar-height
                    :background-color colors/white}]
       [connectivity-status
        (merge status-properties
               {:view-id      view-id
                :window-width @window-width})
        anim-translate-y]])))

;; "push?" determines whether "content" gets pushed down when disconnected
;; like in :home view, or stays put like in :chat view
;; TODO determine-how-this-affects/fix desktop
(defn connectivity-animation-wrapper [style anim-value push? & content]
  (vec (concat
        (if platform/desktop?
          [react/view {:style {:flex 1}}]
          [react/animated-view
           {:style (merge {:flex          1
                           :margin-bottom neg-connectivity-bar-height}
                          ;; A translated view (connectivity-view in this case)
                          ;; prevents touch interaction to component below
                          ;; them. If we don't bring this view on the same level
                          ;; or above as the translated view, the top
                          ;; portion(same height as connectivity-view) of
                          ;; "content" (which now occupies translated view's
                          ;; natural[untranslated] position) becomes
                          ;; unresponsive to touch
                          (when-not @to-hide?
                            {:z-index 1})
                          (if push?
                            {:transform [{:translateY anim-value}]}
                            {:transform [{:translateY neg-connectivity-bar-height}]})
                          style)}])
        content)))
