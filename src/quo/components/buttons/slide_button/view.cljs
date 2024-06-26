(ns quo.components.buttons.slide-button.view
  (:require
    [oops.core :as oops]
    [quo.components.buttons.slide-button.animations :as animations]
    [quo.components.buttons.slide-button.constants :as constants]
    [quo.components.buttons.slide-button.style :as style]
    [quo.components.buttons.slide-button.utils :as utils]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]))

(defn drag-gesture
  [x-pos disabled? track-width sliding-complete? set-sliding-complete on-complete reset-fn]
  (-> (gesture/gesture-pan)
      (gesture/with-test-ID :slide-button-gestures)
      (gesture/enabled (not disabled?))
      (gesture/min-distance 0)
      (gesture/on-update (fn [event]
                           (let [x-translation (oops/oget event "translationX")
                                 clamped-x     (utils/clamp-value x-translation 0 track-width)
                                 reached-end?  (>= clamped-x track-width)]
                             (reanimated/set-shared-value x-pos clamped-x)
                             (when (and reached-end? (not sliding-complete?))
                               (set-sliding-complete true)
                               (when on-complete (on-complete reset-fn))))))
      (gesture/on-end (fn [event]
                        (let [x-translation (oops/oget event "translationX")
                              reached-end?  (>= x-translation track-width)]
                          (when (not reached-end?)
                            (animations/reset-track-position x-pos)))))))

(defn view
  "Options
  - `on-complete`         Callback called when the sliding is complete, returns reset fn as a parameter
  - `disabled?`           Boolean that disables the button
                          (_and gestures_)
  - `size`                :size/s-40`/`:size/s-48`
  - `track-text`          Text that is shown on the track
  - `track-icon`          Key of the icon shown on the track
                          (e.g. `:face-id`)
  - `customization-color` Customization color
  "
  [{:keys [on-complete track-text track-icon disabled? customization-color size
           container-style type blur?]}]
  (let [theme                         (quo.theme/use-theme)
        x-pos                         (reanimated/use-shared-value 0)
        [track-width set-track-width] (rn/use-state nil)
        [sliding-complete?
         set-sliding-complete]        (rn/use-state false)
        on-track-layout               (rn/use-callback
                                       #(set-track-width (oops/oget % "nativeEvent.layout.width")))
        reset-fn                      (rn/use-callback
                                       (fn []
                                         (set-sliding-complete false)
                                         (animations/reset-track-position x-pos)))
        dimensions                    (rn/use-callback
                                       (partial utils/get-dimensions
                                                (or track-width constants/default-width)
                                                size)
                                       [track-width])
        interpolate-track             (rn/use-callback
                                       (partial animations/interpolate-track
                                                x-pos
                                                (dimensions :usable-track)
                                                (dimensions :thumb))
                                       [dimensions])
        custom-color                  (if (= type :danger) :danger customization-color)
        gesture                       (rn/use-memo
                                       (fn []
                                         (drag-gesture x-pos
                                                       disabled?
                                                       (dimensions :usable-track)
                                                       sliding-complete?
                                                       set-sliding-complete
                                                       on-complete
                                                       reset-fn))
                                       [sliding-complete? disabled? on-complete])]
    [gesture/gesture-detector
     {:gesture gesture}
     [reanimated/view
      {:test-ID   :slide-button-track
       :style     (merge (style/track {:disabled?           disabled?
                                       :customization-color custom-color
                                       :height              (dimensions :track-height)
                                       :blur?               blur?})
                         container-style)
       :on-layout on-track-layout}
      [reanimated/view {:style (style/track-cover interpolate-track)}
       [rn/view {:style (style/track-cover-text-container track-width)}
        [icon/icon track-icon
         {:color (utils/text-color custom-color theme blur?)
          :size  20}]
        [rn/view {:width 4}]
        [text/text
         {:weight :medium
          :size   :paragraph-1
          :style  (style/track-text custom-color theme blur?)}
         track-text]]]
      [reanimated/view
       {:style (style/thumb-container {:interpolate-track   interpolate-track
                                       :thumb-size          (dimensions :thumb)
                                       :customization-color custom-color
                                       :theme               theme
                                       :blur?               blur?})}
       [reanimated/view {:style (style/arrow-icon-container interpolate-track)}
        [icon/icon :arrow-right
         {:color colors/white
          :size  20}]]
       [reanimated/view
        {:style (style/action-icon interpolate-track (dimensions :thumb))}
        [icon/icon track-icon
         {:color colors/white
          :size  20}]]]]]))
