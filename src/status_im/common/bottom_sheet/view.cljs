(ns status-im.common.bottom-sheet.view
  (:require
    [oops.core :as oops]
    [quo.context :as quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [status-im.common.bottom-sheet.style :as style]
    [utils.number]
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

(defn- get-layout-height
  [event]
  (oops/oget event "nativeEvent.layout.height"))

(defn view
  [{:keys [hide? insets]}
   {:keys [content selected-item padding-bottom-override border-radius on-close shell?
           gradient-cover? customization-color hide-handle? blur-radius
           hide-on-background-press?]
    :or   {border-radius             12
           hide-on-background-press? true}}]
  (let [theme                             (quo.context/use-theme)
        {window-height :height}           (rn/get-window)
        [sheet-height set-sheet-height]   (rn/use-state 0)
        [layout-height set-layout-height] (rn/use-state window-height)
        handle-sheet-height               (rn/use-callback (fn [e]
                                                             (when (= sheet-height 0)
                                                               (set-sheet-height
                                                                (get-layout-height e))))
                                                           [sheet-height])
        handle-layout-height              (rn/use-callback (fn [e]
                                                             (-> (get-layout-height e)
                                                                 (set-layout-height))))
        [item-height set-item-height]     (rn/use-state 0)
        handle-item-height                (rn/use-callback (fn [e]
                                                             (when (= item-height 0)
                                                               (set-item-height
                                                                (get-layout-height e))))
                                                           [item-height])
        bg-opacity                        (reanimated/use-shared-value 0)
        translate-y                       (reanimated/use-shared-value window-height)
        sheet-gesture                     (rn/use-memo #(get-sheet-gesture translate-y
                                                                           bg-opacity
                                                                           window-height
                                                                           on-close)
                                                       [window-height on-close])
        selected-item-smaller-than-sheet? (< item-height
                                             (- window-height
                                                sheet-height
                                                (:top insets)
                                                (:bottom insets)
                                                bottom-margin))
        top                               (- window-height (:top insets) sheet-height)
        bottom                            (if selected-item-smaller-than-sheet?
                                            (+ sheet-height bottom-margin)
                                            (:bottom insets))
        sheet-max-height                  (- layout-height
                                             (:top insets))
        content-padding-bottom            (or padding-bottom-override
                                              (+ (:bottom insets) bottom-margin))]
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
    [rn/view
     {:style     {:flex 1}
      :on-layout handle-layout-height}
     ;; backdrop
     [rn/pressable
      {:on-press #(when hide-on-background-press? (rf/dispatch [:hide-bottom-sheet]))
       :style    {:flex 1}}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:opacity bg-opacity}
                {:flex             1
                 :background-color (if shell? colors/neutral-100-opa-60 colors/neutral-100-opa-70)})}]]
     ;; sheet
     [gesture/gesture-detector {:gesture sheet-gesture}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:transform [{:translateY translate-y}]}
                (style/sheet {:max-height sheet-max-height}))}
       (when shell?
         [rn/view {:style style/shell-bg-container}
          [quo/blur
           {:style         style/shell-bg
            :blur-radius   (or blur-radius 20)
            :blur-amount   15
            :blur-type     :transparent
            :overlay-color :transparent}]])
       (when selected-item
         [rn/view
          {:on-layout handle-item-height
           :style
           (style/selected-item theme top bottom selected-item-smaller-than-sheet? border-radius)}
          [selected-item]])
       [rn/view
        {:on-layout handle-sheet-height
         :style     (style/sheet-content {:theme          theme
                                          :shell?         shell?
                                          :padding-bottom content-padding-bottom})}
        (when (and gradient-cover? customization-color)
          [quo/gradient-cover
           {:customization-color customization-color
            :opacity             0.4}])
        (when-not hide-handle?
          [quo/drawer-bar])
        [content]]]]]))
