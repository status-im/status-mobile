(ns status-im.ui.components.tabs
  (:require [status-im.ui.components.react :as react]
            [quo.design-system.colors :as colors]))

(defn tab-title [state key label active?]
  [react/view {:align-items :center}
   [react/touchable-highlight {:on-press            #(swap! state assoc :tab key)
                               :underlay-color      colors/gray-lighter
                               :accessibility-label (str label "-item-button")
                               :style               {:border-radius 8}}
    [react/view {:padding-horizontal 12 :padding-vertical 8}
     [react/text {:style {:font-weight "500" :color (if active? colors/blue colors/gray) :line-height 22}}
      label]]]
   (when active?
     [react/view {:width 24 :height 3 :border-radius 4 :background-color colors/blue}])])

(defn tab-button [state key label active?]
  [react/view {:flex 1 :align-items :center :border-radius 8
               :background-color (if active? colors/blue colors/blue-light)}
   [react/touchable-highlight {:on-press            #(swap! state assoc :tab key)
                               :accessibility-label (str label "-item-button")
                               :style               {:border-radius 8}
                               :flex                1}
    [react/view {:padding-horizontal 12 :padding-vertical 8}
     [react/text {:style {:font-weight "500" :color (if active? colors/white colors/blue) :line-height 22}}
      label]]]])