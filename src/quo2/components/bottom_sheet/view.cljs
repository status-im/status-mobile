(ns quo2.components.bottom-sheet.view
  (:require [reagent.core :as reagent]
            [quo.animated :as animated]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.platform :as platform]
            [cljs-bean.core :as bean]
            [quo.components.safe-area :as safe-area]
            [quo2.components.bottom-sheet.style :as styles]
            [quo.gesture-handler :as gesture-handler]
            [quo2.foundations.colors :as quo2.colors]))

(def opacity-coeff 0.8)
(def close-duration 150)
(def spring-config {:damping                   15
                    :mass                      0.7
                    :stiffness                 150
                    :overshootClamping         false
                    :restSpeedThreshold        0.1
                    :restDisplacementThreshold 0.1})

(defn bottom-sheet-hooks [props]
  (let [{on-cancel          :onCancel
         disable-drag?      :disableDrag?
         show-handle?       :showHandle?
         visible?           :visible?
         backdrop-dismiss?  :backdropDismiss?
         back-button-cancel :backButtonCancel
         children           :children
         :or                {show-handle?       true
                             backdrop-dismiss?  true
                             back-button-cancel true}}
        (bean/bean props)
        body-ref   (react/create-ref)
        master-ref (react/create-ref)

        height                    (react/state 0)
        {window-height :height}   (rn/use-window-dimensions)
        {:keys [keyboard-shown
                keyboard-height]} (rn/use-keyboard)
        keyboard-height-android-delta (if (and platform/android? keyboard-shown) (+ keyboard-height 20) 0)
        safe-area                 (safe-area/use-safe-area)
        window-height             (- window-height (if platform/android?
                                                     (+ 50 keyboard-height-android-delta) ;; TODO : remove 50 when react-native-navigation v8 will be implemented https://github.com/wix/react-native-navigation/issues/7225
                                                     0))
        min-height                (+ (* styles/vertical-padding 2) (:bottom safe-area))
        max-height                (- window-height (:top safe-area) styles/margin-top)
        visible                   (react/state false)

        master-translation-y (animated/use-value 0)
        master-velocity-y    (animated/use-value (:undetermined gesture-handler/states))
        master-state         (animated/use-value (:undetermined gesture-handler/states))
        tap-state            (animated/use-value 0)
        manual-open          (animated/use-value 0)
        manual-close         (animated/use-value 0)
        offset               (animated/use-value 0)
        drag-over            (animated/use-value 1)
        clock                (animated/use-clock)
        tap-gesture-handler  (animated/use-gesture {:state tap-state})
        on-master-event      (animated/use-gesture
                              {:translationY master-translation-y
                               :state        master-state
                               :velocityY    master-velocity-y})
        on-body-event        on-master-event
        sheet-height         (min max-height
                                  (+ styles/border-radius @height))

        open-snap-point  (animated/use-value 0)
        close-snap-point 0
        on-close         (fn []
                           (when @visible
                             (reset! visible false)
                             (reset! height 0)
                             (when on-cancel
                               (on-cancel))))
        close-sheet      (fn []
                           (animated/set-value manual-close 1))
        on-snap          (fn [pos]
                           (when (= close-snap-point (aget pos 0))
                             (on-close)))
        interrupted      (animated/and* (animated/eq master-state (:began gesture-handler/states))
                                        (animated/clock-running clock))
        translate-y      (react/use-memo
                          (fn []
                            (animated/with-easing
                              {:value          (animated/cond* (animated/less-or-eq master-translation-y 0)
                                                               (animated/divide master-translation-y 2)
                                                               master-translation-y)
                               :velocity       master-velocity-y
                               :offset         offset
                               :state          master-state
                               :animation-over drag-over
                               :snap-points    [open-snap-point close-snap-point]}))
                          [])
        opacity          (react/use-memo
                          (fn []
                            (animated/cond*
                             open-snap-point
                             (animated/interpolate
                              translate-y
                              {:inputRange  [(animated/multiply open-snap-point opacity-coeff) 0]
                               :outputRange [1 0]
                               :extrapolate (:clamp animated/extrapolate)})))
                          [])]
    (animated/code!
     (fn []
       (animated/cond* (animated/and* (animated/eq master-state (:end gesture-handler/states))
                                      (animated/not* drag-over))
                       (animated/call* [translate-y] on-snap)))
     [on-snap])
    (animated/code!
     (fn []
       (animated/block
        [(animated/cond* (animated/and* interrupted manual-open)
                         [(animated/set manual-open 0)
                          (animated/set offset open-snap-point)
                          (animated/stop-clock clock)])
         (animated/cond* (animated/and* manual-open
                                        (animated/not* manual-close))
                         [(animated/set offset
                                        (animated/re-spring {:from   offset
                                                             :to     open-snap-point
                                                             :clock  clock
                                                             :config spring-config}))
                          (animated/cond* (animated/not* (animated/clock-running clock))
                                          (animated/set manual-open 0))])]))
     [])
    (animated/code!
     (fn []
       (animated/block
        [(animated/cond* (animated/and* interrupted manual-close)
                         [(animated/set manual-close 0)
                          (animated/set offset close-snap-point)
                          (animated/call* [] on-close)
                          (animated/stop-clock clock)])
         (animated/cond* (animated/eq tap-state (:end gesture-handler/states))
                         [(animated/cond* (animated/and* (animated/not* manual-close))
                                          [(animated/stop-clock clock)
                                           (animated/set manual-close 1)])
                          (animated/set tap-state (:undetermined gesture-handler/states))])
         (animated/cond* manual-close
                         [(animated/set offset
                                        (animated/re-timing {:from     offset
                                                             :to       close-snap-point
                                                             :clock    clock
                                                             :easing   (:ease-out animated/easings)
                                                             :duration close-duration}))
                          (animated/cond* (animated/not* (animated/clock-running clock))
                                          [(animated/set manual-close 0)
                                           (animated/set manual-open 0)
                                           (animated/call* [] on-close)])])]))
     [on-cancel])
    (animated/code!
     (fn []
       (animated/cond* (animated/and* (animated/not* manual-close)
                                      (if @visible 1 0)
                                      (if (> @height min-height) 1 0))
                       [(animated/stop-clock clock)
                        (animated/set open-snap-point (* -1 sheet-height))
                        (animated/set manual-open 1)]))
     [@height @visible])
    ;; NOTE(Ferossgp): Remove me when RNGH will suport modal
    (rn/use-back-handler
     (fn []
       (when back-button-cancel
         (close-sheet))
       @visible))
    (react/effect!
     (fn []
       (cond
         visible?
         (do
           (rn/dismiss-keyboard!)
           (reset! visible visible?))

         @visible
         (close-sheet)))
     [visible?])
    (reagent/as-element
     [rn/view {:style          styles/container
               :pointer-events :box-none}
      [gesture-handler/tap-gesture-handler (merge {:enabled backdrop-dismiss?}
                                                  tap-gesture-handler)
       [animated/view {:style (merge (styles/backdrop)
                                     (when platform/ios?
                                       {:opacity          opacity
                                        :background-color (:backdrop (quo2.colors/theme-colors
                                                                      quo2.colors/white
                                                                      quo2.colors/neutral-90))}))}]]
      [animated/view {:style (merge (styles/content-container window-height)
                                    {:transform [{:translateY (if (= sheet-height max-height)
                                                                (animated/add translate-y keyboard-height-android-delta)
                                                                translate-y)}
                                                 {:translateY (* window-height 2)}]})}
       [gesture-handler/pan-gesture-handler (merge on-master-event
                                                   {:ref      master-ref
                                                    :wait-for body-ref
                                                    :enabled  (not disable-drag?)})
        [animated/view  {:style styles/content-header}
         (when show-handle?
           [rn/view {:style styles/handle}])]]
       [gesture-handler/pan-gesture-handler (merge on-body-event
                                                   {:ref      body-ref
                                                    :wait-for master-ref
                                                    :enabled  (and (not disable-drag?)
                                                                   (not= sheet-height max-height))})
        [animated/view {:height sheet-height}
         [animated/scroll-view {:bounces        false
                                :flex           1
                                :scroll-enabled (= sheet-height max-height)}
          [animated/view {:style     {:padding-top    styles/vertical-padding
                                      :padding-bottom (+ styles/vertical-padding
                                                         (if (and platform/ios? keyboard-shown)
                                                           keyboard-height
                                                           (:bottom safe-area)))}
                          :on-layout #(reset! height (.-nativeEvent.layout.height ^js %))}
           (into [:<>]
                 (react/get-children children))]]]]]])))

(def bottom-sheet (reagent/adapt-react-class (react/memo bottom-sheet-hooks)))