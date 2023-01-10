(ns status-im.ui2.screens.chat.composer.images.view
  (:require [quo2.core :as quo2]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im.ui2.screens.chat.composer.style :as style]))

(defn image
  [item]
  [rn/view
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
    [quo2/icon :i/close {:color colors/white :size 12}]]])

(defn images-list
  [images]
  [rn/flat-list
   {:key-fn                  first
    :render-fn               image
    :data                    images
    :horizontal              true
    :style                   {:bottom 50 :position :absolute :z-index 5 :elevation 5}
    :content-container-style {:padding-horizontal 20 :margin-top 12 :padding-top 8}
    :separator               [rn/view {:style {:width 12}}]}])
