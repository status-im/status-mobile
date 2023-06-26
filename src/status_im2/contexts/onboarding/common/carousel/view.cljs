(ns status-im2.contexts.onboarding.common.carousel.view
  (:require [quo2.core :as quo]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.navigation :as navigation]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.onboarding.common.carousel.style :as style]
            [status-im2.contexts.onboarding.common.carousel.animation :as animation]
            [react-native.gesture :as gesture]))

(defn header-text-view
  [index window-width header-text]
  [rn/view {:style (style/header-text-view window-width)}
   [quo/text
    {:style  style/carousel-text
     :weight :semi-bold
     :size   :heading-2}
    (get-in header-text [index :text])]
   [quo/text
    {:style style/carousel-sub-text
     :size  :paragraph-1}
    (get-in header-text [index :sub-text])]])

(defn content-view
  [{:keys [window-width status-bar-height index header-text header-background]} content]
  (let [content-width (* 4 window-width)]
    [:<>
     (when content content)
     [rn/view {:style (style/header-container status-bar-height content-width index header-background)}
      (for [index (range 4)]
        ^{:key index}
        [header-text-view index window-width header-text])]]))

(defn progress-bar
  [{:keys [static? progress-bar-width]}]
  [rn/view
   {:style (style/progress-bar progress-bar-width)}
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? false)}]
   [rn/view {:style (style/progress-bar-item static? true)}]])

(defn f-dynamic-progress-bar
  [{:keys [progress-bar-width animate? progress]}]
  (let [width          (animation/dynamic-progress-bar-width progress-bar-width animate? progress)
        container-view (if animate? reanimated/view rn/view)]
    [container-view {:style (style/dynamic-progress-bar width animate?)}
     [progress-bar
      {:static?            false
       :progress-bar-width progress-bar-width}]]))

(defn f-view
  [{:keys [animate? progress paused? header-text background header-background gesture]}]
  (let [window-width       (rf/sub [:dimensions/window-width])
        status-bar-height  (:status-bar-height @navigation/constants)
        progress-bar-width (- window-width 40)
        carousel-left      (animation/carousel-left-position window-width animate? progress)
        container-view     (if animate? reanimated/view rn/view)
        identified-gesture (case gesture
                             :swipeable (animation/drag-gesture progress paused?)
                             :tappable  (animation/tap-gesture progress paused?)
                             nil)]
    [:<>
     [gesture/gesture-detector {:gesture identified-gesture}
      [container-view {:style (style/carousel-container carousel-left animate?)}
       (for [index (range 2)]
         ^{:key index}
         [content-view
          {:window-width      window-width
           :status-bar-height status-bar-height
           :index             index
           :header-text       header-text
           :header-background header-background}
          background])]]
     [rn/view
      {:style (style/progress-bar-container
               progress-bar-width
               status-bar-height)}
      [progress-bar
       {:static?            true
        :progress-bar-width progress-bar-width}]
      [:f> f-dynamic-progress-bar
       {:progress-bar-width progress-bar-width
        :animate?           animate?
        :progress           progress}]]]))

(defn view [props] [:f> f-view props])
