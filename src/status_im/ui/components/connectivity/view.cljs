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
            [status-im.utils.platform :as platform]))

(def connectivity-bar-height 36)
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

(defn manage-visibility [connected? animate? anim-opacity anim-y status-hidden]
  "status-hidden is a per-view state, while to-hide? is a global state common to
all connectivity views (we have at least one view in home and one in chat)"
  (if connected?
    (if animate?
      (when (and @to-hide? (not @status-hidden))
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
         #(do (reset! to-hide? true) (reset! status-hidden false))))
      (do
        (animation/set-value anim-opacity 1)
        (animation/set-value anim-y (if platform/desktop? connectivity-bar-height 0))
        (reset! to-hide? true)
        (reset! status-hidden false)))))

(defn connectivity-status
  [{:keys [connected?]} anim-translate-y status-hidden]
  (let [anim-translate-y (or anim-translate-y (animation/create-value 0))
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
      (fn [{:keys [view-id message on-press-event connected? connecting?] :as opts}]
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
                               :on-press (when on-press-event #(re-frame/dispatch [on-press-event]))}
            (i18n/label :t/waiting-for-wifi) " "
            [{:style {:text-decoration-line :underline}}
             (i18n/label :t/waiting-for-wifi-change)]]
           (when message
             [react/text {:style    styles/text
                          :on-press (when on-press-event #(re-frame/dispatch [on-press-event]))}
              (i18n/label message)]))])})))

;; timer updating the enqueued status
(def timer     (atom nil))

;; connectivity status change going to be persisted to :connectivity/ui-status-properties
(def enqueued-connectivity-status-properties     (atom nil))

(defn propagate-status
  "Smoothly propagate from :connectivity/status-properties subscription to
:ui-status-properties db. UI components will render based on :ui-status-properties"
  [{:keys [status-properties app-active-since ui-status-properties]}]
  (let [;; enqueued or immediate?
        ;; send immediately if we are transitioning to an offline state
        enqueue?  (cond
                    (and
                     (or
                      (nil? @enqueued-connectivity-status-properties)
                      (:connected? @enqueued-connectivity-status-properties)
                      (:connecting? @enqueued-connectivity-status-properties))
                     (not
                      (or (:connected? status-properties)
                          (:connecting? status-properties))))
                    false

                    :else
                    true)]
    (if enqueue?
      (when (or (and (nil? @enqueued-connectivity-status-properties)
                     (not= status-properties ui-status-properties))
                (and (some? @enqueued-connectivity-status-properties)
                     (not= status-properties @enqueued-connectivity-status-properties)))
        ;; reset queued with new state and start a timer if not yet started
        (reset! enqueued-connectivity-status-properties status-properties)
        (when-not @timer
          (reset! timer (utils/set-timeout #(do
                                              (reset! timer nil)
                                              (when @enqueued-connectivity-status-properties
                                                (re-frame/dispatch [:set :connectivity/ui-status-properties @enqueued-connectivity-status-properties])
                                                (reset! enqueued-connectivity-status-properties nil)))

                                           ;; if the app is in foreground for less than 5s, postpone state changes for 5s otherwise 1s
                                           (if
                                            (and app-active-since
                                                 (< (- (datetime/timestamp) app-active-since)
                                                    5000))
                                             5000
                                             1000)))))
      (when (not= status-properties ui-status-properties)
        ;; send immediately
        (reset! enqueued-connectivity-status-properties nil)
        (re-frame/dispatch [:set :connectivity/ui-status-properties status-properties])
        (when @timer
          (utils/clear-timeout @timer)
          (reset! timer nil))))))

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

(defview connectivity-view [anim-translate-y]
  (letsubs [status-properties [:connectivity/status-properties]
            app-active-since  [:app-active-since]
            ui-status-properties  [:connectivity/ui-status-properties]
            status-hidden (reagent/atom true)
            view-id           [:view-id]
            window-width (reagent/atom 0)]
    (let [loading-indicator? (:loading-indicator? ui-status-properties)]
      [react/view {:style     {:align-items :stretch
                               :z-index     1}
                   :on-layout #(reset! window-width (-> % .-nativeEvent .-layout .-width))}
       (when (and loading-indicator? @status-hidden)
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
        ;on startup default connected
        (merge (or ui-status-properties
                   {:connected? true :message :t/connected})
               {:view-id      view-id
                :window-width @window-width})
        anim-translate-y
        status-hidden]
       [status-propagator-dummy-view {:status-properties status-properties
                                      :app-active-since app-active-since
                                      :ui-status-properties ui-status-properties}]])))

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
