(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.icon :as icon]
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.style :as style]
   [quo2.components.buttons.slide-button.utils :as utils]
   [quo2.components.buttons.slide-button.animations :as animations]
   [react-native.gesture :as gesture]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]
   [quo2.components.buttons.slide-button.constants :as constants]
   [quo2.theme :as quo.theme]))

(defn- f-slider
  [{:keys [disabled?]}]
  (let [track-width        (reagent/atom nil)
        sliding-complete?  (reagent/atom false)
        gestures-disabled? (reagent/atom disabled?)
        on-track-layout    (fn [evt]
                             (let [width (oops/oget evt "nativeEvent.layout.width")]
                               (reset! track-width width)))]
    (fn [{:keys [on-reset
                 on-complete
                 track-text
                 track-icon
                 disabled?
                 customization-color
                 size
                 container-style
                 theme]}]
      (let [x-pos             (reanimated/use-shared-value 0)
            dimensions        (partial utils/get-dimensions
                                       (or @track-width constants/default-width)
                                       size)
            interpolate-track (partial animations/interpolate-track
                                       x-pos
                                       (dimensions :usable-track)
                                       (dimensions :thumb))]
        (rn/use-effect (fn []
                         (when @sliding-complete?
                           (on-complete)))
                       [@sliding-complete?])

        (rn/use-effect (fn []
                         (when on-reset
                           (reset! sliding-complete? false)
                           (reset! gestures-disabled? false)
                           (animations/reset-track-position x-pos)
                           (on-reset)))
                       [on-reset])

        [gesture/gesture-detector
         {:gesture (animations/drag-gesture x-pos
                                            gestures-disabled?
                                            disabled?
                                            (dimensions :usable-track)
                                            sliding-complete?)}
         [reanimated/view
          {:test-ID   :slide-button-track
           :style     (merge (style/track {:disabled?           disabled?
                                           :customization-color customization-color
                                           :height              (dimensions :track-height)
                                           :theme               theme}) container-style)
           :on-layout (when-not (some? @track-width)
                        on-track-layout)}
          [reanimated/view {:style (style/track-cover interpolate-track)}
           [rn/view {:style (style/track-cover-text-container @track-width)}
            [icon/icon track-icon
             {:color (utils/slider-color :main customization-color theme)
              :size  20}]
            [rn/view {:width 4}]
            [rn/text {:style (style/track-text customization-color theme)} track-text]]]
          [reanimated/view
           {:style (style/thumb-container {:interpolate-track   interpolate-track
                                           :thumb-size          (dimensions :thumb)
                                           :customization-color customization-color
                                           :theme               theme})}
           [reanimated/view {:style (style/arrow-icon-container interpolate-track)}
            [icon/icon :arrow-right
             {:color colors/white
              :size  20}]]
           [reanimated/view
            {:style (style/action-icon interpolate-track
                                       (dimensions :thumb))}
            [icon/icon track-icon
             {:color colors/white
              :size  20}]]]]]))))

(defn- view-internal
  "Options
  - `on-complete`         Callback called when the sliding is complete
  - `disabled?`           Boolean that disables the button
                          (_and gestures_)
  - `size`                `40`/`48`
  - `track-text`          Text that is shown on the track
  - `track-icon`          Key of the icon shown on the track
                          (e.g. `:face-id`)
  - `customization-color` Customization color
  - `on-reset`            A callback which can be used to reset the component and run required functionality
  "
  [props]
  [:f> f-slider props])

(def view (quo.theme/with-theme view-internal))
