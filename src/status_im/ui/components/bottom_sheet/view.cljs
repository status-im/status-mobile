(ns status-im.ui.components.bottom-sheet.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-sheet.styles :as styles]
            [status-im.utils.platform :as platform]
            ["react-native" :refer (BackHandler)]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]))

(def initial-animation-duration 400)
(def release-animation-duration 150)
(def cancellation-animation-duration 100)
(def swipe-opacity-range 100)
(def cancellation-coefficient 0.3)
(def min-opacity 0.05)
(def min-velocity 0.4)

(defn- animate
  [{:keys [opacity new-opacity-value
           bottom new-bottom-value
           duration callback]}]
  (when (fn? callback)
    (js/setTimeout callback duration))
  (animation/start
   (animation/parallel
    [(animation/timing opacity
                       {:toValue         new-opacity-value
                        :duration        duration
                        :useNativeDriver true})
     (animation/spring bottom
                       {:toValue         new-bottom-value
                        :duration        duration
                        :tension         40
                        :friction        6
                        :useNativeDriver true})])))

(defn- on-move
  [{:keys [height bottom-value opacity-value]}]
  (fn [_ ^js state]
    (let [dy (.-dy state)]
      (cond (pos? dy)
            (let [opacity (max min-opacity (- 1 (/ dy (- height swipe-opacity-range))))]
              (animation/set-value bottom-value dy)
              (animation/set-value opacity-value opacity))
            (neg? dy)
            (animation/set-value bottom-value dy)))))

(defn- cancelled? [height dy vy]
  (or
   (<= min-velocity vy)
   (> (* cancellation-coefficient height) (- height dy))))

(defn- cancel
  ([opts] (cancel opts nil))
  ([{:keys [height bottom-value show-sheet? opacity-value]} callback]
   (animate {:bottom            bottom-value
             :new-bottom-value  height
             :opacity           opacity-value
             :new-opacity-value 0
             :duration          cancellation-animation-duration
             :callback          #(do (reset! show-sheet? false)
                                     (animation/set-value bottom-value height)
                                     (when (fn? callback) (callback)))})))

(defn- on-release
  [{:keys [height bottom-value close-sheet opacity-value]}]
  (fn [_ state]
    (let [{:strs [dy vy]} (js->clj state)]
      (if (cancelled? height dy vy)
        (close-sheet)
        (animate {:bottom            bottom-value
                  :new-bottom-value  0
                  :opacity           opacity-value
                  :new-opacity-value 1
                  :duration          release-animation-duration})))))

(defn- swipe-pan-responder [opts]
  (.create
   ^js react/pan-responder
   (clj->js
    {:onMoveShouldSetPanResponder (fn [_ ^js state]
                                    (or (< 10 (js/Math.abs (.-dx state)))
                                        (< 5 (js/Math.abs (.-dy state)))))
     :onPanResponderMove          (on-move opts)
     :onPanResponderRelease       (on-release opts)
     :onPanResponderTerminate     (on-release opts)})))

(defn- pan-handlers [^js pan-responder]
  (js->clj (.-panHandlers pan-responder)))

(defn- on-open [{:keys [bottom-value internal-atom opacity-value]}]
  (when-not @internal-atom
    (react/dismiss-keyboard!)
    (reset! internal-atom true)
    (animate {:bottom            bottom-value
              :new-bottom-value  0
              :opacity           opacity-value
              :new-opacity-value 1
              :duration          initial-animation-duration})))

(defn- on-close
  [{:keys [bottom-value opacity-value on-cancel internal-atom height]}]
  (when @internal-atom
    (animate {:bottom            bottom-value
              :new-bottom-value  height
              :opacity           opacity-value
              :new-opacity-value 0
              :duration          cancellation-animation-duration
              :callback          (fn []
                                   (when (fn? on-cancel)
                                     (animation/set-value bottom-value height)
                                     (animation/set-value opacity-value 0)
                                     (reset! internal-atom false)
                                     (on-cancel)))})))

(defn bottom-sheet-view [{:keys [window-height]}]
  (let [opacity-value    (animation/create-value 0)
        bottom-value     (animation/create-value window-height)
        content-height   (reagent/atom (* 0.4 window-height))
        internal-visible (reagent/atom false)
        external-visible (reagent/atom false)
        back-listener    (reagent/atom nil)]
    (fn [{:keys [content on-cancel disable-drag? show-handle? show?
                 backdrop-dismiss? safe-area window-height back-button-cancel]
          :or   {show-handle?       true
                 backdrop-dismiss?  true
                 back-button-cancel true
                 on-cancel          #(re-frame/dispatch [:bottom-sheet/hide])}}]
      (let [height       (+ @content-height
                            styles/border-radius)
            max-height   (- window-height
                            (:top safe-area)
                            styles/margin-top)
            sheet-height (min max-height height)
            close-sheet  (fn []
                           (when (and platform/android? @back-listener)
                             (.remove ^js @back-listener)
                             (reset! back-listener nil))
                           (on-close {:opacity-value opacity-value
                                      :bottom-value  bottom-value
                                      :height        height
                                      :internal-atom internal-visible
                                      :on-cancel     on-cancel}))
            handle-back  (fn []
                           (when back-button-cancel
                             (close-sheet))
                           true)]
        (when-not (= @external-visible show?)
          (reset! external-visible show?)
          (cond
            (true? show?)
            (do (on-open {:bottom-value  bottom-value
                          :opacity-value opacity-value
                          :internal-atom internal-visible
                          :height        height})
                (when platform/android?
                  (reset! back-listener (.addEventListener BackHandler
                                                           "hardwareBackPress"
                                                           handle-back))))

            (false? show?)
            (close-sheet)))
        (when @internal-visible
          [react/view {:style styles/container}
           [react/touchable-highlight (merge {:style styles/container}
                                             (when backdrop-dismiss?
                                               {:on-press #(close-sheet)}))
            [react/animated-view {:style (styles/shadow opacity-value)}]]

           [react/keyboard-avoiding-view {:pointer-events "box-none"
                                          :behaviour      "position"
                                          :style          styles/sheet-wrapper}
            [react/animated-view (merge
                                  {:style (styles/content-container window-height sheet-height bottom-value)}
                                  (when-not (or disable-drag? (= max-height sheet-height))
                                    (pan-handlers
                                     (swipe-pan-responder {:bottom-value  bottom-value
                                                           :opacity-value opacity-value
                                                           :height        height
                                                           :close-sheet   #(close-sheet)}))))
             [react/view (merge {:style styles/content-header}
                                (when (and (not disable-drag?)
                                           (= max-height sheet-height))
                                  (pan-handlers
                                   (swipe-pan-responder {:bottom-value  bottom-value
                                                         :opacity-value opacity-value
                                                         :height        height
                                                         :close-sheet   #(close-sheet)}))))
              (when show-handle?
                [react/view styles/handle])]
             [react/animated-view {:style {:height sheet-height}}
              ;; NOTE(Ferossgp): For a better UX on onScrollBeginDrag we can start dragging the sheet.
              [react/scroll-view {:bounces        false
                                  :scroll-enabled true
                                  :style          {:flex 1}}
               [react/view {:style     {:padding-top    styles/vertical-padding
                                        :padding-bottom (+ styles/vertical-padding
                                                           (:bottom safe-area))}
                            :on-layout #(->> ^js %
                                             .-nativeEvent
                                             .-layout
                                             .-height
                                             (reset! content-height))}
                [content]]]]]]])))))

(defn bottom-sheet [props]
  (let [props (assoc props :window-height @(re-frame/subscribe [:dimensions/window-height]))]
    [react/safe-area-consumer
     (fn [insets]
       (reagent/as-element
        [bottom-sheet-view (assoc props :safe-area (js->clj insets :keywordize-keys true))]))]))
