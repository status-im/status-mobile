(ns status-im.ui.components.large-toolbar
  (:require-macros [status-im.utils.views :as views])
  (:require [cljs-bean.core :refer [->clj ->js]]
            [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.utils.platform :as platform]))

(def hidden (reagent/atom 0))
(def shown (reagent/atom 100))
(def minimized-header-visible? (reagent/atom false))
(def initial-on-show-done? (volatile! false))

(defn animated-content-wrapper [header-in-toolbar has-nav? show?]
  (let [anim-opacity (animation/create-value 0)
        to-hide      (reagent/atom false)]
    (reagent/create-class
     {:component-did-update
      (fn [comp]
        (let [new-argv (rest (reagent/argv comp))
              show?    (last new-argv)]
          (cond
            (and (not @to-hide) show?)
            (animation/start
             (animation/timing
              anim-opacity
              {:toValue         1
               :duration        200
               :easing          (.-ease (animation/easing))
               :useNativeDriver true})
             #(reset! to-hide true))

            (and @to-hide (not show?))
            (animation/start
             (animation/timing
              anim-opacity
              {:toValue         0
               :duration        200
               :easing          (.-ease (animation/easing))
               :useNativeDriver true})
             #(reset! to-hide false)))))

      :reagent-render
      (fn [header-in-toolbar has-nav? _]
        [react/animated-view
         {:style (cond-> {:flex           1
                          :align-self     :stretch
                          :opacity        anim-opacity}
                   (false? has-nav?)
                   (assoc :margin-left -40 :margin-right 40))}
         header-in-toolbar])})))

(defn on-viewable-items-changed [threshold interporlation-step]
  (fn [info]
    (let [changed   (->> (->clj info)
                         :changed
                         (filter #(= 1 (:index %))))
          viewable? (when (seq changed)
                      (->> changed
                           first
                           :isViewable))]
      (when (and @initial-on-show-done? (not (nil? viewable?)))
        (if (= threshold 0)
          (if viewable?
            (reset! minimized-header-visible? false)
            (reset! minimized-header-visible? true))
          (if viewable?
            (do (swap! hidden - interporlation-step) (swap! shown + interporlation-step))
            (do (swap! hidden + interporlation-step) (swap! shown - interporlation-step))))))))

(defonce viewability-config-callback-pairs
  (let [interporlation-step 20]
    (->js
     (vec
      (for [threshold (range 0 (+ 100 interporlation-step) interporlation-step)]
        {:viewabilityConfig      {:itemVisiblePercentThreshold threshold}
         :onViewableItemsChanged (on-viewable-items-changed threshold interporlation-step)})))))

;; header-in-toolbar - component - small header in toolbar
;; nav-item          - component/nil - if nav-item like back button is needed, else nil
;; action-items      - status-im.ui.components.toolbar.view/actions 
(defn minimized-toolbar [header-in-toolbar nav-item action-items]
  (let [has-nav? (boolean nav-item)]
    [toolbar/toolbar
     {:transparent? true
      :style        {:z-index   100
                     :elevation 9}}
     nav-item
     [animated-content-wrapper header-in-toolbar has-nav? @minimized-header-visible?]
     action-items]))

;; header   - component that serves as large header without any top/bottom padding
;;            top(4px high) and bottom(16px high and with border) padding
;;            are assumed to be constant
;;            this is wrapped with padding components and merged with content
;; content  - vector - of the rest(from header) of the list components
;;            wrapped header and content form the data prop of flat-list
;; list-ref - atom - a reference to flat-list for the purpose of invoking its
;;            methods
(views/defview flat-list-with-large-header [header content list-ref]
  (views/letsubs [window-width [:dimensions/window-width]]
    {:component-did-mount #(do (reset! hidden 0) (reset! shown 100)
                               (reset! minimized-header-visible? false)
                               (vreset! initial-on-show-done? false))}
    (let [header-top-padding [react/view {:height 4}]
          ;; header bottom padding with border-bottom
          ;; fades out as it approaches toolbar shadow
          header-bottom      [react/animated-view
                              {:style {:height              16
                                       :opacity             (/ @shown 100)
                                       :border-bottom-width 1
                                       :border-bottom-color colors/gray-lighter}}]
          wrapped-data       (into [header-top-padding header header-bottom] content)
          status-bar-height  (get platform/platform-specific :status-bar-default-height)
          toolbar-shadow-component-height
          (+ 50 toolbar.styles/toolbar-height (if (zero? status-bar-height) 50 status-bar-height))]
      [react/view {:flex 1}
       ;; toolbar shadow
       [react/animated-view
        {:style
         (cond-> {:flex             1
                  :align-self       :stretch
                  :position         :absolute
                  :height           toolbar-shadow-component-height
                  :width            window-width
                  :top              (- toolbar-shadow-component-height)
                  :shadow-radius    8
                  :shadow-offset    {:width 0 :height 2}
                  :shadow-opacity   1
                  :shadow-color     "rgba(0, 9, 26, 0.12)"
                  :elevation        (if (>= @hidden 40) (- (/ @hidden 10) 2) 0)
                  :background-color colors/white}
           platform/ios?
           (assoc :opacity (if (>= @hidden 40) (/ @hidden 100) 0)))}]

       [list.views/flat-list
        {:style                          {:z-index -1}
         :data                           wrapped-data
         :initial-num-to-render          3
         :ref                            #(reset! list-ref %)
         :render-fn                      list.views/flat-list-generic-render-fn
         :key-fn                         (fn [item idx] (str idx))
         :on-scroll-begin-drag           #(when (false? @initial-on-show-done?)
                                            (vreset! initial-on-show-done? true))
         :viewabilityConfigCallbackPairs viewability-config-callback-pairs
         :keyboard-should-persist-taps   :handled}]])))
