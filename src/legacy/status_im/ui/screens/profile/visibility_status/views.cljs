(ns legacy.status-im.ui.screens.profile.visibility-status.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    ["react-native" :refer (BackHandler)]
    [legacy.status-im.ui.components.animation :as anim]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.profile.visibility-status.styles :as styles]
    [legacy.status-im.ui.screens.profile.visibility-status.utils :as utils]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [utils.re-frame :as rf]))

;; === Code Related to visibility-status-button ===

(def button-ref (atom nil))

(defn dispatch-popover
  [top]
  (re-frame/dispatch [:show-visibility-status-popover {:top top}]))

(defn dispatch-visibility-status-update
  [status-type]
  (re-frame/dispatch
   [:visibility-status-updates/delayed-visibility-status-update status-type]))

(defn calculate-button-height-and-dispatch-popover
  []
  (.measure
   ^js
   @button-ref
   (fn [_ _ _ _ _ page-y]
     (dispatch-popover page-y))))

(defn profile-visibility-status-dot
  [status-type color]
  (let [automatic?                      (= status-type
                                           constants/visibility-status-automatic)
        [border-width margin-left size] (if automatic? [1 -10 12] [0 6 10])
        new-ui?                         true]
    [:<>
     (when automatic?
       [rn/view
        {:style (styles/visibility-status-profile-dot
                 {:color        colors/color-inactive
                  :size         size
                  :border-width border-width
                  :margin-left  6
                  :new-ui?      new-ui?})}])
     [rn/view
      {:style (styles/visibility-status-profile-dot
               {:color        color
                :size         size
                :border-width border-width
                :margin-left  margin-left
                :new-ui?      new-ui?})}]]))

(defn visibility-status-button
  [on-press props]
  (let [logged-in?            (rf/sub [:multiaccount/logged-in?])
        {:keys [status-type]} (rf/sub [:multiaccount/current-user-visibility-status])
        status-type           (if (and logged-in? (nil? status-type))
                                (do
                                  (dispatch-visibility-status-update
                                   constants/visibility-status-automatic)
                                  constants/visibility-status-automatic)
                                status-type)
        {:keys [color title]} (get utils/visibility-status-type-data-old status-type)]
    [rn/touchable-opacity
     (merge
      {:on-press            on-press
       :accessibility-label :visibility-status-button
       :style               (styles/visibility-status-button-container)
       :ref                 #(reset! button-ref ^js %)}
      props)
     [profile-visibility-status-dot status-type color]
     [rn/text {:style (styles/visibility-status-text)} title]]))

;; === Code Related to visibility-status-popover ===
(def scale (anim/create-value 0))
(def position (anim/create-value 0))
(def alpha-value (anim/create-value 0))

(defn hide-options
  []
  (anim/start
   (anim/parallel
    [(anim/timing scale
                  {:toValue         0
                   :duration        140
                   :useNativeDriver true})
     (anim/timing position
                  {:toValue         50
                   :duration        210
                   :useNativeDriver true})
     (anim/timing alpha-value
                  {:toValue         0
                   :duration        200
                   :useNativeDriver true})])))

(defn show-options
  []
  (anim/start
   (anim/parallel
    [(anim/timing scale
                  {:toValue         1
                   :duration        210
                   :useNativeDriver true})
     (anim/timing position
                  {:toValue         80
                   :duration        70
                   :useNativeDriver true})
     (anim/timing alpha-value
                  {:toValue         0.4
                   :duration        200
                   :useNativeDriver true})])))

(defn status-option-pressed
  [request-close status-type]
  (request-close)
  (dispatch-visibility-status-update status-type))

(defn status-option
  [{:keys [request-close status-type]}]
  (let [{:keys [color title subtitle]}
        (get utils/visibility-status-type-data-old status-type)]
    [rn/touchable-opacity
     {:style               {:padding 6}
      :accessibility-label :visibility-status-option
      :on-press            #(status-option-pressed
                             request-close
                             status-type)}
     [rn/view {:style (styles/visibility-status-option)}
      [profile-visibility-status-dot status-type color]
      [rn/text {:style (styles/visibility-status-text)} title]]
     (when-not (nil? subtitle)
       [rn/text {:style (styles/visibility-status-subtitle)} subtitle])]))

(defn visibility-status-options
  [request-close top]
  [react/view
   {:position :absolute
    :top      (int top)}
   [visibility-status-button request-close {:ref nil :active-opacity 1}]
   [react/animated-view
    {:style
     (styles/visibility-status-options scale position)
     :accessibility-label :visibility-status-options}
    [status-option
     {:status-type   constants/visibility-status-always-online
      :request-close request-close}]
    [quo/separator {:style {:margin-top 8}}]
    [status-option
     {:status-type   constants/visibility-status-inactive
      :request-close request-close}]
    [quo/separator]
    [status-option
     {:status-type   constants/visibility-status-automatic
      :request-close request-close}]]])

(defn popover-view
  [_ window-height]
  (let [clear-timeout   (atom nil)
        current-popover (reagent/atom nil)
        update?         (reagent/atom nil)
        request-close   (fn []
                          (reset! clear-timeout
                            (js/setTimeout
                             #(do (reset! current-popover nil)
                                  (re-frame/dispatch
                                   [:hide-visibility-status-popover]))
                             200))
                          (hide-options)
                          true)
        on-show         (fn []
                          (show-options)
                          (when platform/android?
                            (.removeEventListener BackHandler
                                                  "hardwareBackPress"
                                                  request-close)
                            (.addEventListener BackHandler
                                               "hardwareBackPress"
                                               request-close)))
        on-hide         (fn []
                          (when platform/android?
                            (.removeEventListener BackHandler
                                                  "hardwareBackPress"
                                                  request-close)))]
    (reagent/create-class
     {:UNSAFE_componentWillUpdate
      (fn [_ [_ popover _]]
        (when @clear-timeout (js/clearTimeout @clear-timeout))
        (cond
          @update?
          (do (reset! update? false)
              (on-show))

          (and @current-popover popover)
          (do (reset! update? true)
              (js/setTimeout #(reset! current-popover popover) 600)
              (hide-options))

          popover
          (do (reset! current-popover popover)
              (on-show))

          :else
          (do (reset! current-popover nil)
              (on-hide))))
      :component-will-unmount on-hide
      :reagent-render
      (fn []
        (when @current-popover
          (let [{:keys [top]} @current-popover]
            [react/view
             {:style (styles/visibility-status-popover-container)}
             (when platform/ios?
               [react/animated-view
                {:style (styles/visibility-status-popover-ios-backdrop
                         alpha-value)}])
             [react/view
              {:style (styles/visibility-status-popover-child-container
                       window-height)}
              [react/touchable-highlight
               {:style    {:flex 1}
                :on-press request-close}
               [visibility-status-options request-close top]]]])))})))

(views/defview visibility-status-popover
  []
  (views/letsubs [popover                 [:visibility-status-popover/popover]
                  {window-height :height} [:dimensions/window]]
    [popover-view popover window-height]))
