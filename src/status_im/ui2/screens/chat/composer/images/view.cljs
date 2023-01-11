(ns status-im.ui2.screens.chat.composer.images.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im.ui2.screens.chat.composer.images.style :as style]))

(defn image
  [item]
  [rn/view
   [rn/image
    {:source {:uri (first item)}
     :style  style/small-image}]
   [rn/touchable-opacity
    {:on-press #(rf/dispatch [:chat.ui/image-unselected (first item)])
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
      :content-container-style      {:margin-top 12 :padding-bottom 8}
      :separator                    [rn/view {:style {:width 12}}]
      :keyboard-should-persist-taps :handled}]))
