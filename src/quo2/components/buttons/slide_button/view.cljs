(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.icon :as icon]
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts :as consts]
   [quo2.components.buttons.slide-button.style :as style]
   [quo2.components.buttons.slide-button.animations :as anim]
   [react-native.gesture :as gesture]
   [react-native.core :as rn]
   [reagent.core :as reagent]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn- f-slider [{:keys [disabled?]}]
  (let [track-width (reagent/atom nil)
        sliding-complete? (reagent/atom false)
        gestures-disabled? (reagent/atom disabled?)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))]

    (fn [{:keys [on-complete
                 track-text
                 track-icon
                 disabled?
                 size]}]
      (let [x-pos (reanimated/use-shared-value 0)
            dimensions  (let [default-dimensions (case size
                                                   :small consts/small-dimensions
                                                   :large consts/large-dimensions
                                                   consts/large-dimensions)]
                          (merge default-dimensions
                                 {:track-width (anim/calc-usable-track
                                                track-width
                                                (:thumb default-dimensions))}))
            interpolate-track (partial anim/interpolate-track
                                       x-pos
                                       (:track-width dimensions)
                                       (:thumb dimensions))]

        (rn/use-effect (fn []
                         (when @sliding-complete?
                           (on-complete)))
                       [@sliding-complete?])

        [gesture/gesture-detector {:gesture (anim/drag-gesture x-pos
                                                               gestures-disabled?
                                                               (:track-width dimensions)
                                                               sliding-complete?)}
         [reanimated/view {:style (style/track-container (:height dimensions))}
          [reanimated/view {:style (style/track disabled?)
                            :on-layout (when-not (some? @track-width)
                                         on-track-layout)}
           [reanimated/view {:style (style/track-cover interpolate-track)}
            [rn/view {:style (style/track-cover-text-container @track-width)}
             [icon/icon track-icon {:color (:text consts/slide-colors)
                                    :size  20}]
             [rn/view {:width 4}]
             [rn/text {:style style/track-text} track-text]]]
           [reanimated/view {:style (style/thumb-container interpolate-track)}
            [rn/view {:style (style/thumb-placeholder (:thumb dimensions))}]
            [reanimated/view {:style (style/thumb (:thumb dimensions))}
             [reanimated/view {:style (style/arrow-icon-container interpolate-track (:thumb dimensions))}
              [icon/icon :arrow-right {:color colors/white
                                       :size  20}]]
             [reanimated/view {:style (style/action-icon interpolate-track
                                                         (:thumb dimensions))}
              [icon/icon track-icon {:color colors/white
                                     :size  20}]]]]]]]))))

(defn slide-button
  "Options
  - `on-complete`     Callback called when the sliding is complete
  - `disabled?`       Boolean that disables the button
                      (_and gestures_)
  - `size`            `:small`/`:large`
  - `track-text`      Text that is shown on the track
  - `track-icon`      Key of the icon shown on the track
                      (e.g. `:face-id`)
  "
  [props]
  [:f> f-slider props])


