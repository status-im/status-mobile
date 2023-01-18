(ns status-im2.common.bottom-sheet.view
  (:require [oops.core :refer [oget]]
            [quo.react :as react]
            [status-im2.common.bottom-sheet.styles :as styles]
            [re-frame.core :as re-frame]
            [react-native.background-timer :as timer]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.hooks :as hooks]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [utils.re-frame :as rf]
            [status-im2.constants :as constants]))

(def bottom-sheet-js (js/require "../src/js/bottom_sheet.js"))

(defn- with-animation
  [value & [options callback]]
  (reanimated/with-spring
   value
   (clj->js (merge {:mass      2
                    :stiffness 500
                    :damping   200})
            options)
   callback))

(defn get-bottom-sheet-gesture
  [pan-y translate-y bg-height bg-height-expanded
   window-height keyboard-shown disable-drag? expandable?
   show-bottom-sheet? expanded? close-bottom-sheet]
  (-> (gesture/gesture-pan)
      (gesture/on-start
       (fn [_]
         (rf/dispatch [:bottom-sheet/update-config {:config :gesture-running? :value true}])
         (when (and keyboard-shown (not disable-drag?) show-bottom-sheet?)
           (re-frame/dispatch [:dismiss-keyboard]))))
      (gesture/on-update
       (fn [evt]
         (when (and (not disable-drag?) show-bottom-sheet?)
           (let [max-pan-up   (if (or expanded? (not expandable?))
                                0
                                (- (- bg-height-expanded bg-height)))
                 max-pan-down (if expanded?
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
         (rf/dispatch [:bottom-sheet/update-config {:config :gesture-running? :value false}])
         (when (and (not disable-drag?) show-bottom-sheet?)
           (let [end-pan-y                  (- window-height (.-value translate-y))
                 expand-threshold           (min (* bg-height 1.1) (+ bg-height 50))
                 collapse-threshold         (max (* bg-height-expanded 0.9) (- bg-height-expanded 50))
                 should-close-bottom-sheet? (< end-pan-y (max (* bg-height 0.7) 50))]
             (cond
               should-close-bottom-sheet?
               (close-bottom-sheet)

               (and (not expanded?) (> end-pan-y expand-threshold))
               (rf/dispatch [:bottom-sheet/update-config {:config :expanded? :value true}])

               (and expanded? (< end-pan-y collapse-threshold))
               (rf/dispatch [:bottom-sheet/update-config {:config :expanded? :value false}]))))))))

(defn handle-comp
  [window-width]
  [rn/view
   {:style {:width            window-width
            :position         :absolute
            :background-color :transparent
            :top              0
            :height           20}}
   [rn/view {:style (styles/handle)}]])

(defn bottom-sheet
  [props children]
  (let [{on-cancel         :on-cancel
         disable-drag?     :disable-drag?
         show-handle?      :show-handle?
         visible?          :visible?
         backdrop-dismiss? :backdrop-dismiss?
         expandable?       :expandable?
         selected-item     :selected-item
         :or               {show-handle?      true
                            backdrop-dismiss? true
                            expandable?       false}}
        props
        close-bottom-sheet #(re-frame/dispatch [:bottom-sheet/hide-and-dispatch on-cancel])]
    [safe-area/consumer
     (fn [insets]
       [:f>
        (fn []
          (let [{height       :height
                 window-width :width}
                (rn/use-window-dimensions)
                window-height (if selected-item (- height 72) height)
                {:keys [keyboard-shown]} (hooks/use-keyboard)
                bg-height-expanded (- window-height (:top insets))
                {:keys [content-height show-bottom-sheet? keyboard-was-shown? expanded? gesture-running?
                        animation-delay]
                 :or   {content-height      nil
                        show-bottom-sheet?  nil
                        keyboard-was-shown? false
                        expanded?           false
                        gesture-running?    false}}
                (rf/sub [:bottom-sheet/config])
                bg-height (max (min content-height bg-height-expanded) 150)
                bottom-sheet-dy (reanimated/use-shared-value 0)
                pan-y (reanimated/use-shared-value 0)
                translate-y (.useTranslateY ^js bottom-sheet-js window-height bottom-sheet-dy pan-y)
                bg-opacity
                (.useBackgroundOpacity ^js bottom-sheet-js translate-y bg-height window-height)
                on-content-layout (fn [evt]
                                    (let [height (oget evt "nativeEvent" "layout" "height")]
                                      (rf/dispatch [:bottom-sheet/update-config
                                                    {:config :content-height :value height}])))
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
                                      close-bottom-sheet)
                handle-comp [gesture/gesture-detector {:gesture bottom-sheet-gesture}
                             [handle-comp window-width]]]

            (react/effect!
             #(do
                (cond
                  (and
                   (nil? show-bottom-sheet?)
                   visible?
                   (some? content-height)
                   (> content-height 0))
                  (rf/dispatch [:bottom-sheet/update-config {:config :show-bottom-sheet? :value true}])

                  (and show-bottom-sheet? (not visible?))
                  (close-bottom-sheet)))
             [show-bottom-sheet? content-height visible?])
            (react/effect!
             #(do
                (when show-bottom-sheet?
                  (cond
                    keyboard-shown
                    (do
                      (rf/dispatch [:bottom-sheet/update-config
                                    {:config :keyboard-was-shown? :value true}])
                      (rf/dispatch [:bottom-sheet/update-config {:config :expanded? :value true}]))
                    (and keyboard-was-shown? (not keyboard-shown))
                    (rf/dispatch [:bottom-sheet/update-config {:config :expanded? :value false}]))))
             [show-bottom-sheet? keyboard-was-shown?])
            (react/effect!
             #(do
                (when-not gesture-running?
                  (cond
                    show-bottom-sheet?
                    (if expanded?
                      (do
                        (reanimated/set-shared-value
                         bottom-sheet-dy
                         (with-animation (+ bg-height-expanded (.-value pan-y))))
                        ;; Workaround for
                        ;; https://github.com/software-mansion/react-native-reanimated/issues/1758#issue-817145741
                        ;; withTiming/withSpring callback not working
                        ;; on-expanded should be called as a callback of
                        ;; with-animation instead, once this issue has been resolved
                        (timer/set-timeout on-expanded
                                           (or animation-delay constants/bottom-sheet-animation-delay)))
                      (do
                        (reanimated/set-shared-value
                         bottom-sheet-dy
                         (with-animation (+ bg-height (.-value pan-y))))
                        ;; Workaround for
                        ;; https://github.com/software-mansion/react-native-reanimated/issues/1758#issue-817145741
                        ;; withTiming/withSpring callback not working
                        ;; on-collapsed should be called as a callback of
                        ;; with-animation instead, once this issue has been resolved
                        (timer/set-timeout on-collapsed
                                           (or animation-delay
                                               constants/bottom-sheet-animation-delay))))

                    (= show-bottom-sheet? false)
                    (reanimated/set-shared-value bottom-sheet-dy (with-animation 0)))))
             [show-bottom-sheet? expanded? gesture-running?])

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
                          [rn/view {:style (styles/selected-background)}
                           [selected-item]])
                        [rn/view {:style (styles/background)}
                         [rn/keyboard-avoiding-view
                          {:behaviour (if platform/ios? :padding :height)
                           :style     {:flex 1}}
                          [rn/view
                           {:style     (styles/content-style insets)
                            :on-layout (when-not (and
                                                  (some? @content-height)
                                                  (> @content-height 0))
                                         on-content-layout)}
                           children]]
                         (when show-handle?
                           handle-comp)]]]
                      (not show-handle?)
                      (conj [gesture/gesture-detector {:gesture bottom-sheet-gesture}]))]))])]))
