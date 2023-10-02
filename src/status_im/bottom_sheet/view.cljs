(ns status-im.bottom-sheet.view
  (:require [oops.core :refer [oget]]
            [quo.react :as react]
            [status-im.bottom-sheet.styles :as styles]
            [re-frame.core :as re-frame]
            [react-native.background-timer :as timer]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.hooks :as hooks]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [utils.worklets.bottom-sheet :as worklets.bottom-sheet]))

(def animation-delay 450)

(defn with-animation
  [value]
  (reanimated/with-spring
   value
   (clj->js {:mass      2
             :stiffness 500
             :damping   200})))

(defn get-bottom-sheet-gesture
  [pan-y translate-y bg-height bg-height-expanded
   window-height keyboard-shown disable-drag? expandable?
   show-bottom-sheet? expanded? close-bottom-sheet gesture-running?]
  (-> (gesture/gesture-pan)
      (gesture/on-start
       (fn [_]
         (reset! gesture-running? true)
         (when (and keyboard-shown (not disable-drag?) show-bottom-sheet?)
           (re-frame/dispatch [:dismiss-keyboard]))))
      (gesture/on-update
       (fn [evt]
         (when (and (not disable-drag?) show-bottom-sheet?)
           (let [max-pan-up   (if (or @expanded? (not expandable?))
                                0
                                (- (- bg-height-expanded bg-height)))
                 max-pan-down (if @expanded?
                                bg-height-expanded
                                bg-height)]
             (reanimated/set-shared-value pan-y
                                          (max
                                           (min
                                            (.-translationY evt)
                                            max-pan-down)
                                           max-pan-up))))))
      (gesture/on-end
       (fn [_]
         (reset! gesture-running? false)
         (when (and (not disable-drag?) show-bottom-sheet?)
           (let [end-pan-y                  (- window-height (.-value translate-y))
                 expand-threshold           (min (* bg-height 1.1) (+ bg-height 50))
                 collapse-threshold         (max (* bg-height-expanded 0.9) (- bg-height-expanded 50))
                 should-close-bottom-sheet? (< end-pan-y (max (* bg-height 0.7) 50))]
             (cond
               should-close-bottom-sheet?
               (close-bottom-sheet)

               (and (not @expanded?) (> end-pan-y expand-threshold))
               (reset! expanded? true)

               (and @expanded? (< end-pan-y collapse-threshold))
               (reset! expanded? false))))))))

(defn handle-view
  [window-width override-theme]
  [rn/view
   {:style {:width            window-width
            :position         :absolute
            :background-color :transparent
            :top              0
            :height           20}}
   [rn/view {:style (styles/handle override-theme)}]])

(defn bottom-sheet
  [props children]
  (let [{on-cancel                 :on-cancel
         disable-drag?             :disable-drag?
         show-handle?              :show-handle?
         visible?                  :visible?
         backdrop-dismiss?         :backdrop-dismiss?
         expandable?               :expandable?
         bottom-safe-area-spacing? :bottom-safe-area-spacing?
         selected-item             :selected-item
         is-initially-expanded?    :expanded?
         override-theme            :override-theme
         :or                       {show-handle?              true
                                    backdrop-dismiss?         true
                                    expandable?               false
                                    bottom-safe-area-spacing? true
                                    is-initially-expanded?    false}}
        props
        content-height (reagent/atom nil)
        show-bottom-sheet? (reagent/atom nil)
        keyboard-was-shown? (reagent/atom false)
        expanded? (reagent/atom is-initially-expanded?)
        gesture-running? (reagent/atom false)
        reset-atoms (fn []
                      (reset! show-bottom-sheet? nil)
                      (reset! content-height nil)
                      (reset! expanded? false)
                      (reset! keyboard-was-shown? false)
                      (reset! gesture-running? false))
        close-bottom-sheet (fn []
                             (reset! show-bottom-sheet? false)
                             (when (fn? on-cancel) (on-cancel))
                             (timer/set-timeout
                              #(do
                                 (re-frame/dispatch [:bottom-sheet/hide-old-navigation-overlay])
                                 (reset-atoms))
                              animation-delay))]
    [:f>
     (fn []
       (let [{height       :height
              window-width :width}
             (rn/get-window)
             window-height (if selected-item (- height 72) height)
             {:keys [keyboard-shown]} (hooks/use-keyboard)
             insets (safe-area/get-insets)
             bg-height-expanded (- window-height (:top insets))
             bg-height (max (min @content-height bg-height-expanded) 109)
             bottom-sheet-dy (reanimated/use-shared-value 0)
             pan-y (reanimated/use-shared-value 0)
             translate-y (worklets.bottom-sheet/use-translate-y window-height bottom-sheet-dy pan-y)
             bg-opacity
             (worklets.bottom-sheet/use-background-opacity translate-y bg-height window-height 0.7)
             on-content-layout (fn [evt]
                                 (let [height (oget evt "nativeEvent" "layout" "height")]
                                   (reset! content-height height)))
             on-expanded (fn []
                           (reanimated/set-shared-value bottom-sheet-dy bg-height-expanded)
                           (reanimated/set-shared-value pan-y 0))
             on-collapsed (fn []
                            (reanimated/set-shared-value bottom-sheet-dy bg-height)
                            (reanimated/set-shared-value pan-y 0))
             bottom-sheet-gesture (get-bottom-sheet-gesture
                                   pan-y
                                   translate-y
                                   bg-height
                                   bg-height-expanded
                                   window-height
                                   keyboard-shown
                                   disable-drag?
                                   expandable?
                                   show-bottom-sheet?
                                   expanded?
                                   close-bottom-sheet
                                   gesture-running?)
             handle-comp [gesture/gesture-detector {:gesture bottom-sheet-gesture}
                          [handle-view window-width override-theme]]]

         (react/effect! #(do
                           (cond
                             (and
                              (nil? @show-bottom-sheet?)
                              visible?
                              (some? @content-height)
                              (> @content-height 0))
                             (reset! show-bottom-sheet? true)

                             (and @show-bottom-sheet? (not visible?))
                             (close-bottom-sheet)))
                        [@show-bottom-sheet? @content-height visible?])
         (react/effect! #(do
                           (when @show-bottom-sheet?
                             (cond
                               keyboard-shown
                               (do
                                 (reset! keyboard-was-shown? true)
                                 (reset! expanded? true))
                               (and @keyboard-was-shown? (not keyboard-shown))
                               (reset! expanded? false))))
                        [@show-bottom-sheet? @keyboard-was-shown? keyboard-shown])
         (react/effect! #(do
                           (when-not @gesture-running?
                             (cond
                               @show-bottom-sheet?
                               (if @expanded?
                                 (do
                                   (reanimated/set-shared-value
                                    bottom-sheet-dy
                                    (with-animation (+ bg-height-expanded (.-value pan-y))))
                                   ;; Workaround for
                                   ;; https://github.com/software-mansion/react-native-reanimated/issues/1758#issue-817145741
                                   ;; withTiming/withSpring callback not working on-expanded should
                                   ;; be called as a callback of with-animation instead, once this
                                   ;; issue has been resolved
                                   (timer/set-timeout on-expanded animation-delay))
                                 (do
                                   (reanimated/set-shared-value
                                    bottom-sheet-dy
                                    (with-animation (+ bg-height (.-value pan-y))))
                                   ;; Workaround for
                                   ;; https://github.com/software-mansion/react-native-reanimated/issues/1758#issue-817145741
                                   ;; withTiming/withSpring callback not working on-collapsed should
                                   ;; be called as a callback of with-animation instead, once this
                                   ;; issue has been resolved
                                   (timer/set-timeout on-collapsed animation-delay)))

                               (= @show-bottom-sheet? false)
                               (reanimated/set-shared-value bottom-sheet-dy (with-animation 0)))))
                        [@show-bottom-sheet? @expanded? @gesture-running?])

         [:<>
          [rn/touchable-without-feedback {:on-press (when backdrop-dismiss? close-bottom-sheet)}
           [reanimated/view
            {:style (reanimated/apply-animations-to-style
                     {:opacity bg-opacity}
                     styles/backdrop)}]]
          (cond->> [reanimated/view
                    {:style (reanimated/apply-animations-to-style
                             {:transform [{:translateY translate-y}]}
                             {:width  window-width
                              :height window-height})}
                    [rn/view {:style styles/container}
                     (when selected-item
                       [rn/view {:style (styles/selected-background override-theme)}
                        [selected-item]])
                     [rn/view {:style (styles/background override-theme)}
                      [rn/keyboard-avoiding-view
                       {:behaviour (if platform/ios? :padding :height)
                        :style     {:flex 1}}
                       [rn/view
                        {:style     (styles/content-style insets bottom-safe-area-spacing?)
                         :on-layout (when-not (and
                                               (some? @content-height)
                                               (> @content-height 0))
                                      on-content-layout)}
                        children]]
                      (when show-handle?
                        handle-comp)]]]
            (not show-handle?)
            (conj [gesture/gesture-detector {:gesture bottom-sheet-gesture}]))]))]))
