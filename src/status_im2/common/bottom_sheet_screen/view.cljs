(ns status-im2.common.bottom-sheet-screen.view
  (:require
    [react-native.gesture :as gesture]
<<<<<<< HEAD
    [react-native.hooks :as hooks]
    [react-native.navigation :as navigation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [react-native.safe-area :as safe-area]
    [status-im2.common.bottom-sheet-screen.style :as style]
    [react-native.core :as rn]
    [reagent.core :as reagent]
=======
    [react-native.navigation :as navigation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :refer [oget]]
    [status-im2.common.bottom-sheet-screen.style :as style]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [quo.react]
>>>>>>> 52b8d487a (feat: bottom sheet screen)
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 100)

(defn drag-gesture
  [translate-y opacity scroll-enabled curr-scroll]
  (->
    (gesture/gesture-pan)
    (gesture/on-start (fn [e]
<<<<<<< HEAD
                        (when (< (oops/oget e "velocityY") 0)
                          (reset! scroll-enabled true))))
    (gesture/on-update (fn [e]
                         (let [translation (oops/oget e "translationY")
=======
                        (when (< (oget e "velocityY") 0)
                          (reset! scroll-enabled true))))
    (gesture/on-update (fn [e]
                         (let [translation (oget e "translationY")
>>>>>>> 52b8d487a (feat: bottom sheet screen)
                               progress    (Math/abs (/ translation drag-threshold))]
                           (when (pos? translation)
                             (reanimated/set-shared-value translate-y translation)
                             (reanimated/set-shared-value opacity (- 1 (/ progress 5)))))))
    (gesture/on-end (fn [e]
<<<<<<< HEAD
                      (if (> (oops/oget e "translationY") drag-threshold)
=======
                      (if (> (oget e "translationY") drag-threshold)
>>>>>>> 52b8d487a (feat: bottom sheet screen)
                        (do
                          (reanimated/set-shared-value opacity (reanimated/with-timing-duration 0 100))
                          (rf/dispatch [:navigate-back]))
                        (do
                          (reanimated/set-shared-value opacity (reanimated/with-timing 1))
                          (reanimated/set-shared-value translate-y (reanimated/with-timing 0))
                          (reset! scroll-enabled true)))))
    (gesture/on-finalize (fn [e]
<<<<<<< HEAD
                           (when (and (>= (oops/oget e "velocityY") 0)
=======
                           (when (and (>= (oget e "velocityY") 0)
>>>>>>> 52b8d487a (feat: bottom sheet screen)
                                      (<= @curr-scroll (if platform/ios? -1 0)))
                             (reset! scroll-enabled false))))))

(defn on-scroll
  [e curr-scroll]
<<<<<<< HEAD
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (reset! curr-scroll y)))

(defn view
  [content skip-background?]
  [:f>
   (let [scroll-enabled (reagent/atom true)
         curr-scroll    (atom 0)]
     (fn []
       (let [sb-height   (navigation/status-bar-height)
             insets      (safe-area/use-safe-area)
             padding-top (Math/max sb-height (:top insets))
             padding-top (if platform/ios? padding-top (+ padding-top 10))
             opacity     (reanimated/use-shared-value 0)
=======
  (let [y (oget e "nativeEvent.contentOffset.y")]
    (reset! curr-scroll y)))

(defn consumer
  [children skip-background?]
  [:f>
   (let [scroll-enabled (reagent/atom true)
         curr-scroll    (atom 0)
         padding-top    (navigation/status-bar-height)
         padding-top    (if platform/ios? padding-top (+ padding-top 20))]
     (fn []
       (let [opacity     (reanimated/use-shared-value 0)
>>>>>>> 52b8d487a (feat: bottom sheet screen)
             translate-y (reanimated/use-shared-value 0)
             close       (fn []
                           (reanimated/set-shared-value opacity (reanimated/with-timing-duration 0 100))
                           (rf/dispatch [:navigate-back]))]
         (rn/use-effect
          (fn []
<<<<<<< HEAD
            (reanimated/animate-delay opacity 1 (if platform/ios? 300 100))))
         (hooks/use-back-handler close)
=======
            (reanimated/animate-delay opacity 1 (if platform/ios? 300 100))
            (rn/hw-back-add-listener close)))
>>>>>>> 52b8d487a (feat: bottom sheet screen)
         [rn/view
          {:style {:flex        1
                   :padding-top padding-top}}
          (when-not skip-background?
            [reanimated/view {:style (style/background opacity)}])

          [reanimated/view {:style (style/main-view translate-y)}
           [gesture/gesture-detector
            {:gesture (drag-gesture translate-y opacity scroll-enabled curr-scroll)}
            [rn/view {:style style/handle-container}
<<<<<<< HEAD
             [rn/view {:style (style/handle)}]]]
           [content
            {:insets         insets
             :close          close
             :scroll-enabled @scroll-enabled
             :on-scroll      #(on-scroll % curr-scroll)}]]])))])
=======
<<<<<<< HEAD
             [rn/view {:style (style/handle)}]]
            [content
             {:close          close
              :scroll-enabled @scroll-enabled
              :on-scroll      #(on-scroll % curr-scroll)}]]]])))])
=======
             [rn/view {:style style/handle}]]
            (children close @scroll-enabled #(on-scroll % curr-scroll))]]])))])
>>>>>>> 52b8d487a (feat: bottom sheet screen)
>>>>>>> fb1ba49a5 (feat: bottom sheet screen)
