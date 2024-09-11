(ns legacy.status-im.ui.components.tabs
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]))

(defn tab-title
  [state k label active?]
  [react/view {:align-items :center}
   [react/touchable-highlight
    {:on-press            #(swap! state assoc :tab k)
     :underlay-color      colors/gray-lighter
     :accessibility-label (str label "-item-button")
     :style               {:border-radius 8}}
    [react/view {:padding-horizontal 12 :padding-vertical 8}
     [react/text
      {:style {:font-weight "500" :color (if active? colors/blue colors/gray) :line-height 22}}
      label]]]
   (when active?
     [react/view {:width 24 :height 3 :border-radius 4 :background-color colors/blue}])])

(defn tab-button
  [state k label active?]
  [react/view
   {:flex             1
    :align-items      :center
    :border-radius    8
    :background-color (if active? colors/blue colors/blue-light)}
   [react/touchable-highlight
    {:on-press            #(swap! state assoc :tab k)
     :accessibility-label (str label "-item-button")
     :style               {:border-radius 8}
     :flex                1}
    [react/view {:padding-horizontal 12 :padding-vertical 8}
     [react/text
      {:style {:font-weight "500" :color (if active? colors/white colors/blue) :line-height 22}}
      label]]]])
