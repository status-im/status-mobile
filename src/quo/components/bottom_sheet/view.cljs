(ns quo.components.bottom-sheet.view
  (:require [cljs-bean.core :as bean]
            [quo.animated :as animated]
            [quo.components.bottom-sheet.style :as styles]
            [quo.design-system.colors :as colors]
            [quo.gesture-handler :as gesture-handler]
            [quo.platform :as platform]
            [quo.react :as react]
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [react-native.safe-area :as safe-area]))

(def opacity-coeff 0.8)
(def close-duration 150)
(def spring-config
  {:damping                   15
   :mass                      0.7
   :stiffness                 150
   :overshootClamping         false
   :restSpeedThreshold        0.1
   :restDisplacementThreshold 0.1})

(defn bottom-sheet-hooks
  [props]
  (let [{disable-drag?      :disableDrag?
         show-handle?       :showHandle?
         visible?           :visible?
         backdrop-dismiss?  :backdropDismiss?
         back-button-cancel :backButtonCancel
         children           :children
         :or                {show-handle?       true
                             backdrop-dismiss?  true
                             back-button-cancel true}}
        (bean/bean props)
        body-ref (react/create-ref)
        master-ref (react/create-ref)

        height (react/state 0)
        {window-height :height} (rn/use-window-dimensions)
        {:keys [keyboard-shown
                keyboard-height]}
        (rn/use-keyboard)
        keyboard-height-android-delta
        (if (and platform/android? keyboard-shown) (+ keyboard-height 20) 0)
        safe-area (safe-area/get-insets)
        window-height (- window-height
                         (if platform/android?
                           (+ 50 keyboard-height-android-delta) ;; TODO : remove 50 when
                           ;; react-native-navigation v8 will be
                           ;; implemented
                           ;; https://github.com/wix/react-native-navigation/issues/7225
                           0))
        max-height (- window-height (:top safe-area))
        visible (react/state false)

        master-translation-y 0
        master-velocity-y (animated/use-value (:undetermined gesture-handler/states))
        master-state (animated/use-value (:undetermined gesture-handler/states))
        tap-state 0
        manual-close 0
        tap-gesture-handler (animated/use-gesture {:state tap-state})
        on-master-event (animated/use-gesture
                         {:translationY master-translation-y
                          :state        master-state
                          :velocityY    master-velocity-y})
        on-body-event on-master-event
        sheet-height (min max-height
                          (+ styles/border-radius @height))
        close-sheet (fn []
                      (animated/set-value manual-close 1))
        opacity 0.3
       ]
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
     [rn/view
      {:style          styles/container
       :pointer-events :box-none}
      [gesture-handler/tap-gesture-handler
       (merge {:enabled backdrop-dismiss?}
              tap-gesture-handler)
       [animated/view
        {:style (merge (styles/backdrop)
                       (when platform/ios?
                         {:opacity          opacity
                          :background-color (:backdrop @colors/theme)}))}]]
      [animated/view
       {:style (merge (styles/content-container window-height))}
       [gesture-handler/pan-gesture-handler
        (merge on-master-event
               {:ref      master-ref
                :wait-for body-ref
                :enabled  (not disable-drag?)})
        [animated/view {:style styles/content-header}
         (when show-handle?
           [rn/view {:style styles/handle}])]]
       [gesture-handler/pan-gesture-handler
        (merge on-body-event
               {:ref      body-ref
                :wait-for master-ref
                :enabled  (and (not disable-drag?)
                               (not= sheet-height max-height))})
        [animated/view
         {:height sheet-height
          :flex   1}
         [animated/view
          {:style     {:padding-top    styles/vertical-padding
                       :padding-bottom (+ styles/vertical-padding
                                          (if (and platform/ios? keyboard-shown)
                                            keyboard-height
                                            (:bottom safe-area)))}
           :on-layout #(reset! height (.-nativeEvent.layout.height ^js %))}
          (into [:<>]
                (react/get-children children))]]]]])))

(def bottom-sheet (reagent/adapt-react-class (react/memo bottom-sheet-hooks)))
