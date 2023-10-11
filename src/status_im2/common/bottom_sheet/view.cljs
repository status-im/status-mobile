(ns status-im2.common.bottom-sheet.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.hooks :as hooks]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im2.common.bottom-sheet.style :as style]
            [utils.re-frame :as rf]))

(def duration 450)
(def timing-options #js {:duration duration})
(def bottom-margin 8)

(defn hide
  [translate-y bg-opacity window-height on-close]
  (rf/dispatch [:dismiss-keyboard])
  (when (fn? on-close)
    (on-close))
  ;; it will be better to use animation callback, but it doesn't work
  ;; so we have to use timeout, also we add 50ms for safety
  (js/setTimeout #(rf/dispatch [:bottom-sheet-hidden]) (+ duration 50))
  (reanimated/set-shared-value translate-y
                               (reanimated/with-timing window-height timing-options))
  (reanimated/set-shared-value bg-opacity (reanimated/with-timing 0 timing-options)))

(defn show
  [translate-y bg-opacity]
  (reanimated/set-shared-value translate-y (reanimated/with-timing 0 timing-options))
  (reanimated/set-shared-value bg-opacity (reanimated/with-timing 1 timing-options)))

(def gesture-values (atom {}))

(defn get-sheet-gesture
  [translate-y bg-opacity window-height on-close]
  (-> (gesture/gesture-pan)
      (gesture/on-start
       (fn [_]
         (swap! gesture-values assoc :pan-y (reanimated/get-shared-value translate-y))))
      (gesture/on-update
       (fn [evt]
         (let [tY (oops/oget evt "translationY")]
           (swap! gesture-values assoc :dy (- tY (:pdy @gesture-values)))
           (swap! gesture-values assoc :pdy tY)
           (when (pos? tY)
             (reanimated/set-shared-value
              translate-y
              (+ tY (:pan-y @gesture-values)))))))
      (gesture/on-end
       (fn [_]
         (if (< (:dy @gesture-values) 0)
           (show translate-y bg-opacity)
           (hide translate-y bg-opacity window-height on-close))))))

(defn- f-view
  [_ _]
  (let [sheet-height (reagent/atom 0)
        item-height  (reagent/atom 0)]
    (fn [{:keys [hide? insets theme]}
         {:keys [content selected-item padding-bottom-override border-radius on-close shell?
                 gradient-cover? customization-color]
          :or   {border-radius 12}}]
      (let [{window-height :height}           (rn/get-window)
            bg-opacity                        (reanimated/use-shared-value 0)
            translate-y                       (reanimated/use-shared-value window-height)
            sheet-gesture                     (get-sheet-gesture translate-y
                                                                 bg-opacity
                                                                 window-height
                                                                 on-close)
            selected-item-smaller-than-sheet? (< @item-height
                                                 (- window-height
                                                    @sheet-height
                                                    (:top insets)
                                                    (:bottom insets)
                                                    bottom-margin))
            top                               (- window-height (:top insets) @sheet-height)
            bottom                            (if selected-item-smaller-than-sheet?
                                                (+ @sheet-height bottom-margin)
                                                (:bottom insets))]
        (rn/use-effect
         #(if hide?
            (hide translate-y bg-opacity window-height on-close)
            (show translate-y bg-opacity))
         [hide?])
        (hooks/use-back-handler (fn []
                                  (when (fn? on-close)
                                    (on-close))
                                  (rf/dispatch [:hide-bottom-sheet])
                                  true))
        [rn/view {:style {:flex 1}}
         ;; backdrop
         [rn/touchable-without-feedback {:on-press #(rf/dispatch [:hide-bottom-sheet])}
          [reanimated/view
           {:style (reanimated/apply-animations-to-style
                    {:opacity bg-opacity}
                    {:flex 1 :background-color colors/neutral-100-opa-70})}]]
         ;; sheet
         [gesture/gesture-detector {:gesture sheet-gesture}
          [reanimated/view
           {:style (reanimated/apply-animations-to-style
                    {:transform [{:translateY translate-y}]}
                    (style/sheet insets window-height selected-item))}
           (when shell?
             [blur/ios-view {:style style/shell-bg}])
           (when selected-item
             [rn/view
              {:on-layout #(reset! item-height (.-nativeEvent.layout.height ^js %))
               :style
               (style/selected-item theme top bottom selected-item-smaller-than-sheet? border-radius)}
              [selected-item]])

           [rn/view
            {:style     (style/sheet-content theme padding-bottom-override insets shell? bottom-margin)
             :on-layout #(reset! sheet-height (.-nativeEvent.layout.height ^js %))}
            (when gradient-cover?
              [rn/view {:style style/gradient-bg}
               [quo/gradient-cover
                {:customization-color customization-color
                 :opacity             0.4}]])
            [rn/view {:style (style/handle theme)}]
            [content]]]]]))))

(defn- internal-view
  [args sheet]
  [:f> f-view args sheet])

(def view (quo.theme/with-theme internal-view))
