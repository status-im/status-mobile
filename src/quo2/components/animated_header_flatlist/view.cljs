(ns quo2.components.animated-header-flatlist.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [quo2.foundations.colors :as colors]
    [status-im.ui.components.fast-image :as fast-image]
    [quo2.components.animated-header-flatlist.style :as style]
    [oops.core :as oops]
    [utils.re-frame :as rf]))

(def header-height 234)
(def cover-height 192)
(def blur-view-height 100)
(def threshold (- header-height blur-view-height))

(defn scroll-handler
  [event initial-y scroll-y]
  (let [content-size-y (- (oops/oget event "nativeEvent.contentSize.height")
                          (oops/oget event "nativeEvent.layoutMeasurement.height"))
        current-y (- (oops/oget event "nativeEvent.contentOffset.y") initial-y)]
    (reanimated/set-shared-value scroll-y (- content-size-y current-y))))

(defn header
  [{:keys [theme-color cover-uri cover-bg-color title-comp]} top-inset scroll-y]
  (let [input-range [0 (* threshold 0.33)]
        border-animation (reanimated/interpolate scroll-y input-range [12 0]
                                                 {:extrapolateLeft  "clamp"
                                                  :extrapolateRight "clamp"})]
    [rn/view
     {:style {:background-color (or theme-color (colors/theme-colors colors/white colors/neutral-95))
              :margin-top       (when platform/ios? (- top-inset))}}
     (when cover-uri
       [fast-image/fast-image
        {:style  {:width  "100%"
                  :height cover-height}
         :source {:uri cover-uri}}])
     (when cover-bg-color
       [rn/view
        {:style {:width            "100%"
                 :height           cover-height
                 :background-color cover-bg-color}}])
     [reanimated/view {:style (style/header-bottom-part border-animation)}
      [title-comp]]]))

(defn animated-header-list
  [{:keys [header-comp main-comp footer-comp] :as parameters}]
  [safe-area/consumer
   (fn [insets]
     (let [window-height (:height (rn/get-window))
           status-bar-height (rn/status-bar-height)
           bottom-inset (:bottom insets)
           initial-y (if platform/ios? (- (:top insets)) 0)]
       [:f>
        (fn []
          (let [scroll-y (reanimated/use-shared-value initial-y)
                opacity-animation (reanimated/interpolate scroll-y
                                                          [(* threshold 0.33) (* threshold 0.66)]
                                                          [0 1]
                                                          {:extrapolateLeft  "clamp"
                                                           :extrapolateRight "extend"})
                translate-animation (reanimated/interpolate scroll-y [(* threshold 0.66) threshold] [50 0]
                                                            {:extrapolateLeft  "clamp"
                                                             :extrapolateRight "clamp"})
                title-opacity-animation (reanimated/interpolate scroll-y [(* threshold 0.66) threshold] [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})]
            [rn/keyboard-avoiding-view
             {:style                  {:position :absolute
                                       :flex     1
                                       :top      0
                                       :left     0
                                       :height   window-height
                                       :bottom   0
                                       :right    0}
              :keyboardVerticalOffset (- bottom-inset)}

             [reanimated/blur-view
              {:blurAmount   32
               :blurType     :light
               :overlayColor (if platform/ios? colors/white-opa-70 :transparent)
               :style        (style/blur-view opacity-animation)}]

             [rn/view {:style {:position       :absolute
                               :top            56
                               :left           0
                               :right          0
                               :padding-bottom 12
                               :width          "100%"
                               :display        :flex
                               :flex-direction :row
                               :z-index        2
                               :overflow       :hidden}}
              [rn/touchable-opacity
               {:active-opacity 1
                :on-press       #(rf/dispatch [:navigate-back])
                :style          (style/button-container {:margin-left 20})}
               [quo/icon :i/arrow-left {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
              [reanimated/view {:style (style/header-comp translate-animation title-opacity-animation)}
               [header-comp]]
              [rn/touchable-opacity
               {:active-opacity 1
                :style          (style/button-container {:margin-right 20})}
               [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]

             [reanimated/flat-list
              {:data                         [nil]
               :render-fn                    main-comp
               :key-fn                       str
               :inverted                     (when platform/ios? true)
               :style                        (when platform/android? {:scaleY -1})
               :footer                       (reagent/as-element (header parameters (:top insets) scroll-y))
               ;; TODO: https://github.com/status-im/status-mobile/issues/14924
               :scroll-event-throttle        8
               :keyboard-dismiss-mode        :interactive
               :keyboard-should-persist-taps :handled
               :on-scroll                    (fn [event] (scroll-handler event initial-y scroll-y))}]

             (when footer-comp
               (footer-comp insets))]))]))])
