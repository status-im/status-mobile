(ns status-im.common.bottom-sheet-screen.view
  (:require
    [oops.core :as oops]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.bottom-sheet-screen.style :as style]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 200)

(defn drag-gesture
  [{:keys [translate-y opacity scroll-enabled? curr-scroll close reset-open-sheet set-animating-true]}]
  (-> (gesture/gesture-pan)
      (gesture/on-start (fn [e]
                          (set-animating-true)
                          (when (< (oops/oget e "velocityY") 0)
                            (reset! scroll-enabled? true))))
      (gesture/on-update (fn [e]
                           (let [translation (oops/oget e "translationY")
                                 progress    (Math/abs (/ translation drag-threshold))]
                             (when (pos? translation)
                               (reanimated/set-shared-value translate-y translation)
                               (reanimated/set-shared-value opacity (- 1 (/ progress 5)))))))
      (gesture/on-end (fn [e]
                        (if (> (oops/oget e "translationY") drag-threshold)
                          (close)
                          (reset-open-sheet))))
      (gesture/on-finalize (fn [e]
                             (when (and (>= (oops/oget e "velocityY") 0)
                                        (<= @curr-scroll (if platform/ios? -1 0)))
                               (reset! scroll-enabled? false))))))

(defn on-scroll
  [e curr-scroll]
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (reset! curr-scroll y)))

(defn view
  [_]
  (let [scroll-enabled?     (reagent/atom true)
        curr-scroll         (reagent/atom 0)
        animating?          (reagent/atom true)
        set-animating-true  #(reset! animating? true)
        set-animating-false (fn [ms]
                              (js/setTimeout #(reset! animating? false) ms))
        on-scroll-update    #(on-scroll % curr-scroll)]
    (fn [{:keys [content skip-background?]}]
      (let [theme                    (quo.theme/use-theme)
            {:keys [top] :as insets} (safe-area/get-insets)
            alert-banners-top-margin (rf/sub [:alert-banners/top-margin])
            padding-top              (+ alert-banners-top-margin
                                        (if platform/ios? top (+ top 10)))
            {:keys [height]}         (rn/get-window)
            opacity                  (reanimated/use-shared-value 0)
            translate-y              (reanimated/use-shared-value height)
            close                    (fn []
                                       (set-animating-true)
                                       (reanimated/animate translate-y height 300)
                                       (reanimated/animate opacity 0 300)
                                       (rf/dispatch [:navigate-back])
                                       true)
            reset-open-sheet         (fn []
                                       (reanimated/animate translate-y 0 300)
                                       (reanimated/animate opacity 1 300)
                                       (set-animating-false 300)
                                       (reset! scroll-enabled? true))]
        (rn/use-mount
         (fn []
           (rn/hw-back-add-listener close)
           (reanimated/animate translate-y 0 300)
           (reanimated/animate opacity 1 300)
           (set-animating-false 300)
           #(rn/hw-back-remove-listener close)))
        [rn/view {:style (style/container padding-top)}
         (when-not skip-background?
           [reanimated/view {:style (style/background opacity)}])
         [gesture/gesture-detector
          {:gesture (drag-gesture {:translate-y        translate-y
                                   :opacity            opacity
                                   :scroll-enabled?    scroll-enabled?
                                   :curr-scroll        curr-scroll
                                   :close              close
                                   :reset-open-sheet   reset-open-sheet
                                   :set-animating-true set-animating-true})}
          [reanimated/view {:style (style/main-view translate-y theme)}
           [rn/view {:style style/handle-container}
            [rn/view {:style (style/handle theme)}]]
           [content
            {:insets           insets
             :close            close
             :scroll-enabled?  scroll-enabled?
             :current-scroll   curr-scroll
             :on-scroll        on-scroll-update
             :sheet-animating? animating?}]]]]))))
