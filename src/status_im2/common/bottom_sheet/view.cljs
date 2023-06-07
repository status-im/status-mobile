(ns status-im2.common.bottom-sheet.view
<<<<<<< HEAD
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im2.common.bottom-sheet.styles :as styles]
            [react-native.gesture :as gesture]
            [oops.core :as oops]
            [react-native.hooks :as hooks]
            [react-native.blur :as blur]
            [reagent.core :as reagent]))
=======
  (:require
    [react-native.safe-area :as safe-area]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [quo2.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im2.common.bottom-sheet.styles :as styles]
    [react-native.gesture :as gesture]
    [oops.core :as oops]
    [react-native.hooks :as hooks]
    [react-native.blur :as blur]))
>>>>>>> 72fabd4b2 (fix: bottom sheet padding)

(def duration 450)
(def timing-options #js {:duration duration})

(defn hide
  [translate-y bg-opacity window-height on-close]
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

<<<<<<< HEAD
(defn f-view
  [_ _]
  (let [sheet-height (reagent/atom 0)]
    (fn [{:keys [hide? insets]}
         {:keys [content override-theme selected-item padding-bottom-override on-close shell?]}]
      (let [{window-height :height} (rn/get-window)
            bg-opacity              (reanimated/use-shared-value 0)
            translate-y             (reanimated/use-shared-value window-height)
            sheet-gesture           (get-sheet-gesture translate-y bg-opacity window-height on-close)]
        (rn/use-effect
         #(if hide? (hide translate-y bg-opacity window-height on-close) (show translate-y bg-opacity))
         [hide?])
        (hooks/use-back-handler #(do (when (fn? on-close)
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
           {:style     (reanimated/apply-animations-to-style
                        {:transform [{:translateY translate-y}]}
                        (styles/sheet insets
                                      window-height
                                      override-theme
                                      padding-bottom-override
                                      shell?))
            :on-layout #(reset! sheet-height (oops/oget % "nativeEvent" "layout" "height"))}
           (when shell? [blur/ios-view {:style styles/shell-bg}])
=======
(defn view
  [{:keys [hide? insets]}
   {:keys [content override-theme selected-item on-close padding-bottom-override shell?]}]
  (let [{window-height :height} (rn/get-window)
        bg-opacity              (reanimated/use-shared-value 0)
        translate-y             (reanimated/use-shared-value window-height)
        sheet-gesture           (get-sheet-gesture translate-y bg-opacity window-height on-close)]
    (rn/use-effect
     #(if hide? (hide translate-y bg-opacity window-height on-close) (show translate-y bg-opacity))
     [hide?])
    (hooks/use-back-handler #(do (when (fn? on-close)
                                   (on-close))
                                 (rf/dispatch [:hide-bottom-sheet])
                                 true))
    [rn/view {:flex 1}
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
<<<<<<< HEAD
                (styles/sheet insets window-height override-theme shell?))}
>>>>>>> 72fabd4b2 (fix: bottom sheet padding)
=======
                (styles/sheet insets window-height override-theme padding-bottom-override shell?))}
>>>>>>> 78179301b (review)

           (when selected-item
             [rn/view
              [rn/view {:style (styles/selected-item override-theme window-height @sheet-height insets)}
               [selected-item]]])

           ;; handle
           [rn/view {:style (styles/handle override-theme)}]
           ;; content
           [content]]]]))))
