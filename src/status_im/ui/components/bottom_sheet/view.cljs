(ns status-im.ui.components.bottom-sheet.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.bottom-sheet.styles :as styles]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]))

(def initial-animation-duration 300)
(def release-animation-duration 150)
(def cancellation-animation-duration 100)
(def swipe-opacity-range 100)
(def cancellation-height 180)
(def min-opacity 0.05)
(def min-velocity 0.1)

(defn- animate
  [{:keys [opacity new-opacity-value
           bottom new-bottom-value
           duration callback]}]
  (animation/start
   (animation/parallel
    [(animation/timing opacity
                       {:toValue         new-opacity-value
                        :duration        duration
                        :useNativeDriver true})
     (animation/timing bottom
                       {:toValue         new-bottom-value
                        :duration        duration
                        :useNativeDriver true})])
   (when (fn? callback) callback)))

(defn animate-sign-panel
  [opacity-value bottom-value]
  (animate {:bottom            bottom-value
            :new-bottom-value  0
            :opacity           opacity-value
            :new-opacity-value 1
            :duration          initial-animation-duration}))

(defn- on-move
  [{:keys [height bottom-value opacity-value]}]
  (fn [_ state]
    (let [dy (.-dy state)]
      (cond (pos? dy)
            (let [opacity (max min-opacity (- 1 (/ dy (- height swipe-opacity-range))))]
              (animation/set-value bottom-value dy)
              (animation/set-value opacity-value opacity))
            (neg? dy)
            (animation/set-value bottom-value (/ dy 2))))))

(defn cancelled? [height dy vy]
  (or
   (<= min-velocity vy)
   (> cancellation-height (- height dy))))

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
                                     (animation/set-value opacity-value 0)
                                     (when (fn? callback) (callback)))})))

(defn- on-release
  [{:keys [height bottom-value opacity-value on-cancel] :as opts}]
  (fn [_ state]
    (let [{:strs [dy vy]} (js->clj state)]
      (if (cancelled? height dy vy)
        (cancel opts on-cancel)
        (animate {:bottom            bottom-value
                  :new-bottom-value  0
                  :opacity           opacity-value
                  :new-opacity-value 1
                  :duration          release-animation-duration})))))

(defn swipe-pan-responder [opts]
  (.create
   (react/pan-responder)
   (clj->js
    {:onMoveShouldSetPanResponder (fn [_ state]
                                    (or (< 10 (js/Math.abs (.-dx state)))
                                        (< 5 (js/Math.abs (.-dy state)))))
     :onPanResponderMove          (on-move opts)
     :onPanResponderRelease       (on-release opts)
     :onPanResponderTerminate     (on-release opts)})))

(defn pan-handlers [pan-responder]
  (js->clj (.-panHandlers pan-responder)))

(defn- bottom-sheet-view
  [{:keys [opacity-value bottom-value]}]
  (reagent.core/create-class
   {:component-did-mount
    #(animate-sign-panel opacity-value bottom-value)
    :reagent-render
    (fn [{:keys [opacity-value bottom-value
                 height content on-cancel]
          :or   {on-cancel #(re-frame/dispatch [:bottom-sheet/hide])}
          :as   opts}]
      [react/keyboard-avoiding-view
       (merge
        (pan-handlers (swipe-pan-responder opts))
        {:style styles/container})
       [react/touchable-highlight
        {:on-press #(cancel opts on-cancel)
         :style    styles/container}

        [react/animated-view (styles/shadow opacity-value)]]
       [react/animated-view
        {:style (styles/content-container height bottom-value)}
        [react/view styles/content-header
         [react/view styles/handle]]
        [react/view {:style {:flex   1
                             :height height}}
         [content]]
        [react/view {:style styles/bottom-view}]]])}))

(defn bottom-sheet
  [{:keys [show? content-height on-cancel]}]
  (let [show-sheet?          (reagent/atom show?)
        total-content-height (+ content-height styles/border-radius
                                styles/bottom-padding)
        bottom-value         (animation/create-value total-content-height)
        opacity-value        (animation/create-value 0)
        opts                 {:height        total-content-height
                              :bottom-value  bottom-value
                              :opacity-value opacity-value
                              :show-sheet?   show-sheet?
                              :on-cancel     on-cancel}]
    (reagent.core/create-class
     {:component-will-update
      (fn [this [_ new-args]]
        (let [old-args             (second (.-argv (.-props this)))
              old-show?            (:show? old-args)
              new-show?            (:show? new-args)
              old-height           (:content-height old-args)
              new-height           (:content-height new-args)
              total-content-height (+ new-height
                                      styles/border-radius
                                      styles/bottom-padding)
              opts'                (assoc opts :height total-content-height)]
          (when (and new-show? (not= old-height new-height))
            (animation/set-value bottom-value new-height))
          (cond (and (not old-show?) new-show?)
                (reset! show-sheet? true)

                (and old-show? (false? new-show?) (true? @show-sheet?))
                (cancel opts'))))
      :reagent-render
      (fn [{:keys [content content-height]}]
        (let [total-content-height (+ content-height
                                      styles/border-radius
                                      styles/bottom-padding)]
          (when @show-sheet?
            [bottom-sheet-view (assoc opts
                                      :content content
                                      :height total-content-height)])))})))
