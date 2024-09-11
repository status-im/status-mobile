(ns status-im.contexts.chat.messenger.composer.images.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.images.style :as style]
    [utils.re-frame :as rf]))

(defn image
  [item theme]
  [rn/view style/image-container
   [rn/image
    {:source {:uri (:resized-uri (val item))}
     :style  style/small-image}]
   [rn/touchable-opacity
    {:on-press #(rf/dispatch [:chat.ui/image-unselected (val item)])
     :style    (style/remove-photo-container theme)
     :hit-slop {:right  5
                :left   5
                :top    10
                :bottom 10}}
    [rn/view {:style style/remove-photo-inner-container}
     [quo/icon :i/clear {:size 20 :color colors/neutral-50 :color-2 colors/white}]]]])

(defn images-list
  []
  (let [theme  (quo.theme/use-theme)
        images (rf/sub [:chats/sending-image])
        height (reanimated/use-shared-value (if (seq images) constants/images-container-height 0))]
    (rn/use-effect (fn []
                     (reanimated/animate height
                                         (if (seq images) constants/images-container-height 0)))
                   [images])
    [reanimated/view
     {:style {:height            height
              :margin-horizontal -20
              :z-index           1}}
     [gesture/flat-list
      {:key-fn                            first
       :render-fn                         (fn [item]
                                            (image item theme))
       :data                              images
       :content-container-style           {:padding-horizontal 20}
       :horizontal                        true
       :shows-horizontal-scroll-indicator false
       :keyboard-should-persist-taps      :handled}]]))
