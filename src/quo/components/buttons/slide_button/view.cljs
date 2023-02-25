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
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    utils.schema))

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
                 theme
                 type
                 blur?]}]
      (let [x-pos             (reanimated/use-shared-value 0)
            dimensions        (partial utils/get-dimensions
                                       (or @track-width constants/default-width)
                                       size)
            interpolate-track (partial animations/interpolate-track
                                       x-pos
                                       (dimensions :usable-track)
                                       (dimensions :thumb))
            custom-color      (if (= type :danger) :danger customization-color)]
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
                                           :customization-color custom-color
                                           :height              (dimensions :track-height)
                                           :blur?               blur?})
                             container-style)
           :on-layout (when-not (some? @track-width)
                        on-track-layout)}
          [reanimated/view {:style (style/track-cover interpolate-track)}
           [rn/view {:style (style/track-cover-text-container @track-width)}
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
            {:style (style/action-icon interpolate-track
                                       (dimensions :thumb))}
            [icon/icon track-icon
             {:color colors/white
              :size  20}]]]]]))))

(defn- view-internal
  [props]
  [:f> f-slider props])

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:on-complete {:optional true} fn?]
     [:disabled? {:optional true} :boolean]
     [:size {:optional true} [:enum :small :large]]
     [:track-text :string]
     [:track-icon :schema.common/icon-name]
     [:customization-color {:optional true} :schema.common/color]]]
   :any])

(def view (utils.schema/instrument ::slide-button ?schema (quo.theme/with-theme view-internal)))
