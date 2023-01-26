(ns status-im.ui2.screens.chat.composer.images.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im.ui2.screens.chat.composer.images.style :as style]))

(defn image
  [item]
  ;; there is a bug on Android https://github.com/facebook/react-native/issues/12534
  ;; so we need some magic here with paddings so close button isn't cut
  [rn/view {:padding-top 12 :padding-bottom 8 :padding-right 12}
   [rn/image
    {:source {:uri (:uri (val item))}
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
  [images]
  (when (seq images)
    [rn/flat-list
     {:key-fn                       first
      :render-fn                    image
      :data                         images
      :horizontal                   true
      :keyboard-should-persist-taps :handled}]))
