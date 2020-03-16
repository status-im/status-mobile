(ns status-im.ui.components.connectivity.view
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.utils.utils :as utils]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(def connectivity-bar-height 36)
(def neg-connectivity-bar-height (- connectivity-bar-height))

;; ui-connectivity-status delays
(def standard-delay 1000)
(def long-delay 5000)

;; millisec window from foreground\login within which use long delay
(def timewindow-for-long-delay 5000)

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
    [react/view {:style {:width            parent-width
                         :position         :absolute
                         :top              -3
                         :z-index          3
                         :height           3
                         :background-color colors/white}}
     [react/animated-view {:style (animated-bar-style blue-bar-left-margin
                                                      parent-width
                                                      colors/blue)}]
     [react/animated-view {:style (assoc (animated-bar-style white-bar-left-margin
                                                             parent-width
                                                             colors/white)
                                         :left (* 0.15 parent-width))}]]))

(def to-hide? (reagent/atom false))

(defn manage-visibility [connected? animate? anim-opacity anim-y status-hidden]
  "status-hidden is a per-view state, while to-hide? is a global state common to
all connectivity views (we have at least one view in home and one in chat)"
  (if connected?
    (if animate?
      (when (and @to-hide? (not @status-hidden))
        (animation/start
         (animation/parallel
          [(animation/timing anim-opacity
                             {:toValue         0
                              :delay           800
                              :duration        150
                              :easing          (.-ease (animation/easing))
                              :useNativeDriver true})
           (animation/timing anim-y
                             {:toValue         (if platform/desktop? 0 neg-connectivity-bar-height)
                              :delay           800
                              :duration        150
                              :easing          (.-ease (animation/easing))
                              :useNativeDriver true})])
         ;; second param of start() - a callback that fires when animation stops
         #(do (reset! to-hide? false) (reset! status-hidden true))))
      (do
        (animation/set-value anim-opacity 0)
        (animation/set-value anim-y (if platform/desktop? 0 neg-connectivity-bar-height))
        (reset! to-hide? false)
        (reset! status-hidden true)))
    ;; else
    (if animate?
      (when (and (not @to-hide?) @status-hidden)
        (animation/start
         (animation/parallel
          [(animation/timing anim-opacity
                             {:toValue         1
                              :duration        150
                              :easing          (.-ease (animation/easing))
                              :useNativeDriver true})
           (animation/timing anim-y
                             {:toValue         (if platform/desktop? connectivity-bar-height 0)
                              :duration        150
                              :easing          (.-ease (animation/easing))
                              :useNativeDriver true})])
         ;; second param of start() - a callback that fires when animation stops
         #(do (reset! to-hide? true) (reset! status-hidden false))))
      (do
        (animation/set-value anim-opacity 1)
        (animation/set-value anim-y (if platform/desktop? connectivity-bar-height 0))
        (reset! to-hide? true)
        (reset! status-hidden false)))))

(defn connectivity-status
  [{:keys [connected?]} status-hidden]
  (let [anim-translate-y (animation/create-value neg-connectivity-bar-height)
        anim-opacity     (animation/create-value 0)]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (manage-visibility connected? false
                           anim-opacity anim-translate-y status-hidden))
      :should-component-update
      ;; ignore :loading-indicator?
      (fn [_ [_ old_p] [_ new_p]]
        (not= (dissoc old_p :loading-indicator?)
              (dissoc new_p :loading-indicator?)))
      :component-did-update
      (fn [comp]
        (manage-visibility (:connected? (reagent/props comp)) true
                           anim-opacity anim-translate-y status-hidden))
      :reagent-render
      (fn [{:keys [message on-press-event connected? connecting?] :as opts}]
        (when-not @status-hidden
          [react/animated-view {:style               (styles/text-wrapper
                                                      (assoc opts
                                                             :height connectivity-bar-height
                                                             :background-color (if connected?
                                                                                 colors/green
                                                                                 colors/gray)
                                                             :transform anim-translate-y
                                                             :opacity anim-opacity))
                                :accessibility-label :connection-status-text}
           (when connecting?
             [react/activity-indicator {:color colors/white :margin-right 6}])
           (if (= message :mobile-network)
             [react/nested-text {:style    styles/text
                                 :on-press (when on-press-event #(re-frame/dispatch [on-press-event]))}
              (i18n/label :t/waiting-for-wifi) " "
              [{:style {:text-decoration-line :underline}}
               (i18n/label :t/waiting-for-wifi-change)]]
             (when message
               [react/text {:style    styles/text
                            :on-press (when on-press-event #(re-frame/dispatch [on-press-event]))}
                (i18n/label message)]))]))})))

;; timer updating the enqueued status
(def timer (atom nil))

;; connectivity status change going to be persisted to :connectivity/ui-status-properties
(def enqueued-connectivity-status-properties (atom nil))

(defn propagate-status
  "Smoothly propagate from :connectivity/status-properties subscription to
:ui-status-properties db. UI components will render based on :ui-status-properties"
  [{:keys [status-properties app-active-since logged-in-since ui-status-properties]}]
  (when (or (and (nil? @enqueued-connectivity-status-properties)
                 (not= status-properties ui-status-properties))
            (and (some? @enqueued-connectivity-status-properties)
                 (not= status-properties @enqueued-connectivity-status-properties)))
    ;; reset queued with new state and start a timer if not yet started
    (reset! enqueued-connectivity-status-properties status-properties)
    (when-not @timer
      (reset!
       timer
       (utils/set-timeout
        #(do
           (reset! timer nil)
           (when @enqueued-connectivity-status-properties
             (re-frame/dispatch [:set
                                 :connectivity/ui-status-properties
                                 @enqueued-connectivity-status-properties])
             (reset! enqueued-connectivity-status-properties nil)))

        ;; timeout choice:
        ;; if the app is in foreground or logged-in for less than <timeframe>,
        ;;  postpone state changes for <long> otherwise <short>
        (let [ts      (max
                       (or logged-in-since 0)
                       (or app-active-since 0))
              ts-diff (- (datetime/timestamp) ts)
              timeout (if (< ts-diff timewindow-for-long-delay)
                        long-delay
                        standard-delay)]
          (log/debug "propagate-status set-timeout: " logged-in-since app-active-since ts-diff timeout)
          timeout))))))

(defn status-propagator-dummy-view
  "this empty view is needed to react propagate status-properties to ui-status-properties"
  [props]
  (reagent/create-class
   {:component-did-mount
    #(propagate-status props)
    :should-component-update
    (fn [_ _ [_ props]]
      (propagate-status props)
      false)
    :reagent-render
    #()}))

(defview connectivity [header footer]
  (letsubs [status-properties    [:connectivity/status-properties]
            app-active-since     [:app-active-since]
            logged-in-since      [:logged-in-since]
            ui-status-properties [:connectivity/ui-status-properties]
            status-hidden        (reagent/atom true)
            window-width         (reagent/atom 0)]
    (let [loading-indicator? (:loading-indicator? ui-status-properties)]
      [react/view {:style     {:flex 1}
                   :on-layout #(reset! window-width (-> % .-nativeEvent .-layout .-width))}
       [react/view {:style {:z-index 2 :background-color colors/white}}
        header
        [react/view
         (when (and loading-indicator? @status-hidden)
           [loading-indicator @window-width])]]
       [connectivity-status
        (merge (or ui-status-properties
                   {:connected? true :message :t/connected})
               {:window-width @window-width})
        status-hidden]
       ;;TODO this is something weird, rework
       [status-propagator-dummy-view {:status-properties    status-properties
                                      :app-active-since     app-active-since
                                      :logged-in-since      logged-in-since
                                      :ui-status-properties ui-status-properties}]
       footer])))