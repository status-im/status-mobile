(ns status-im2.contexts.chat.composer.images.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.composer.images.style :as style]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.composer.constants :as constants]))

(defn image
  [item]
  [rn/view style/image-container
   [rn/image
    {:source {:uri (:resized-uri (val item))}
     :style  style/small-image}]
   [rn/touchable-opacity
    {:on-press #(rf/dispatch [:chat.ui/image-unselected (val item)])
     :style    style/remove-photo-container
     :hit-slop {:right  5
                :left   5
                :top    10
                :bottom 10}}
    [quo/icon :i/close {:color colors/white :size 12}]]])

(defn f-images-list
  []
  (let [images (rf/sub [:chats/sending-image])
        height (reanimated/use-shared-value (if (seq images) constants/images-container-height 0))]
    (rn/use-effect (fn []
                     (reanimated/animate height
                                         (if (seq images) constants/images-container-height 0)))
                   [images])
    [reanimated/view
     {:style (reanimated/apply-animations-to-style {:height height}
                                                   {:margin-horizontal -20
                                                    :z-index 1})}
     [gesture/flat-list
      {:key-fn                            first
       :render-fn                         image
       :data                              images
       :content-container-style           {:padding-horizontal 20}
       :horizontal                        true
       :shows-horizontal-scroll-indicator false
       :keyboard-should-persist-taps      :handled}]]))

(defn images-list
  []
  [:f> f-images-list])
