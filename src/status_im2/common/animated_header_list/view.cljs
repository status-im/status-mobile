(ns status-im2.common.animated-header-list.view
  (:require
   [quo2.core :as quo]
   [react-native.core :as rn]
   [react-native.platform :as platform]
   [react-native.reanimated :as reanimated]
   [react-native.safe-area :as safe-area]
   [reagent.core :as reagent]
   [quo2.foundations.colors :as colors]
   [status-im.ui.components.fast-image :as fast-image]
   [status-im2.common.animated-header-list.style :as style]
   [oops.core :as oops]
   [utils.re-frame :as rf]))

(def header-height 192)

(def threshold (/ header-height 2))

(defn scroll-handler
  [event initial-y scroll-y opacity-value]
  (let [current-y (- (oops/oget event "nativeEvent.contentOffset.y") initial-y)]
    (reanimated/set-shared-value scroll-y current-y)
    (reanimated/set-shared-value opacity-value
                                 (reanimated/with-timing (if (> current-y threshold) 1 0)))))

(defn header
  [{:keys [theme-color display-picture-comp cover-uri]} top-inset scroll-y]
  (let [input-range     [0 threshold]
        output-range    [1 0.4]
        scale-animation (reanimated/interpolate scroll-y
                                                input-range
                                                output-range
                                                {:extrapolateLeft  "clamp"
                                                 :extrapolateRight "clamp"})]
    [rn/view
     {:style {:height           header-height
              :background-color (or theme-color (colors/theme-colors colors/white colors/neutral-95))
              :margin-top       (when platform/ios? (- top-inset))}}
     (when cover-uri
       [fast-image/fast-image
        {:style  {:width  "100%"
                  :height "100%"}
         :source {:uri cover-uri}}])
     [rn/view {:style style/header-bottom-part}]
     [reanimated/view {:style (style/entity-picture scale-animation)}
      [display-picture-comp]]]))

(defn animated-header-list
  [{:keys [title-comp theme-color main-comp] :as parameters}]
  [safe-area/consumer
   (fn [insets]
     (let [window-height     (:height (rn/get-window))
           status-bar-height (rn/status-bar-height)
           bottom-inset      (:bottom insets)
           ;; view height calculation is different because window height is different on iOS and Android:
           ;; https://i.stack.imgur.com/LSyW5.png
           view-height       (if platform/ios?
                               (- window-height bottom-inset)
                               (+ window-height status-bar-height))
           input-range       [0 threshold]
           output-range      [-100 0]
           initial-y         (if platform/ios? (- (:top insets)) 0)]
       [:f>
        (fn []
          (let [scroll-y            (reanimated/use-shared-value initial-y)
                translate-animation (reanimated/interpolate scroll-y
                                                            input-range
                                                            output-range
                                                            {:extrapolateLeft  "clamp"
                                                             :extrapolateRight "clamp"})
                opacity-value       (reanimated/use-shared-value 0)]
            [rn/view {:style (style/container-view view-height theme-color)}
             [rn/touchable-opacity
              {:active-opacity 1
               :on-press       #(rf/dispatch [:navigate-back])
               :style          (style/button-container {:left 20})}
              [quo/icon :i/arrow-left {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
             [rn/touchable-opacity
              {:active-opacity 1
               :style          (style/button-container {:right 20})}
              [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
             [reanimated/view {:style (style/title-comp opacity-value)}
              [title-comp]]
             [reanimated/blur-view
              {:blurAmount   32
               :blurType     :light
               :overlayColor (if platform/ios? colors/white-opa-70 :transparent)
               :style        (style/blur-view translate-animation)}]
             [reanimated/flat-list
              {:data [nil]
               :render-fn main-comp
               :key-fn (fn [i] (str i))
               :header (reagent/as-element (header parameters (:top insets) scroll-y))
               :scroll-event-throttle 8
               :on-scroll (fn [event] (scroll-handler event initial-y scroll-y opacity-value))}]]))]))])


