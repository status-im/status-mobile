(ns status-im2.common.bottom-sheet-screen.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [react-native.safe-area :as safe-area]
    [status-im2.common.bottom-sheet-screen.style :as style]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 100)

(defn drag-gesture
  [translate-y scroll-enabled curr-scroll]
  (->
    (gesture/gesture-pan)
    (gesture/on-start (fn [e]
                        (when (< (oops/oget e "velocityY") 0)
                          (reset! scroll-enabled true))))
    (gesture/on-update (fn [e]
                         (let [translation (oops/oget e "translationY")]
                           (when (pos? translation)
                             (reanimated/set-shared-value translate-y translation)))))
    (gesture/on-end (fn [e]
                      (if (> (oops/oget e "translationY") drag-threshold)
                        (rf/dispatch [:navigate-back])
                        (do
                          (reanimated/set-shared-value translate-y (reanimated/with-timing 0))
                          (reset! scroll-enabled true)))))
    (gesture/on-finalize (fn [e]
                           (when (and (>= (oops/oget e "velocityY") 0)
                                      (<= @curr-scroll (if platform/ios? -1 0)))
                             (reset! scroll-enabled false))))))

(defn on-scroll
  [e curr-scroll]
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (reset! curr-scroll y)))

(defn f-view
  [content skip-background?]
  (let [scroll-enabled (reagent/atom true)
        curr-scroll    (atom 0)]
    (fn []
      (let [insets           (safe-area/get-insets)
            {:keys [height]} (rn/get-window)
            padding-top      (:top insets)
            padding-top      (if platform/ios? padding-top (+ padding-top 10))
            translate-y      (reanimated/use-shared-value height)
            close            (fn []
                               (reanimated/animate translate-y height 300)
                               (rf/dispatch [:navigate-back]))]
        (rn/use-effect #(reanimated/animate translate-y 0 300))
        (hooks/use-back-handler close)
        [rn/view
         {:style {:flex        1
                  :padding-top padding-top}}
         (when-not skip-background?
           [reanimated/view {:style style/background}])
         [gesture/gesture-detector
          {:gesture (drag-gesture translate-y scroll-enabled curr-scroll)}
          [reanimated/view {:style (style/main-view translate-y)}
           [rn/view {:style style/handle-container}
            [rn/view {:style (style/handle)}]]
           [content
            {:insets         insets
             :close          close
             :scroll-enabled scroll-enabled
             :on-scroll      #(on-scroll % curr-scroll)}]]]]))))
