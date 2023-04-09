(ns status-im2.contexts.chat.messages.bottom-sheet-composer.images.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.bottom-sheet-composer.images.style :as style]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]))

(defn image
  [item]
  ;; there is a bug on Android https://github.com/facebook/react-native/issues/12534
  ;; so we need some magic here with paddings so close button isn't cut
  [rn/view {:padding-top 12 :padding-bottom 8 :padding-right 12}
   [rn/image
    {:source {:uri (:resized-uri (val item))}
     :style  style/small-image}]
   [rn/touchable-opacity
    {:on-press (fn [] (rf/dispatch [:chat.ui/image-unselected (val item)]))
     :style    style/remove-photo-container
     :hit-slop {:right  5
                :left   5
                :top    10
                :bottom 10}}
    [quo/icon :i/close {:color colors/white :size 12}]]])

(defn images-list
  []
  [:f>
   (fn []
     (let [images (rf/sub [:chats/sending-image])
           height (reanimated/use-shared-value (if (seq images) c/images-container-height 0))]
       (rn/use-effect (fn []
                        (if (seq images)
                          (reanimated/animate height c/images-container-height)
                          (reanimated/animate height 0))) [images])
       [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
        [rn/flat-list
         {:key-fn                       first
          :render-fn                    image
          :data                         images
          :horizontal                   true
          :keyboard-should-persist-taps :handled}]]))])
