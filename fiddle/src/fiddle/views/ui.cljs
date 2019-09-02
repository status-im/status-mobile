(ns fiddle.views.ui
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.checkbox.view :as checkbox]
            [reagent.core :as reagent]
            [status-im.ui.components.radio :as radio]))

(def sw (reagent/atom true))
(def ch (reagent/atom true))
(def ch2 (reagent/atom false))

(defn ui []
  [react/view {:flex-direction :row :flex-wrap :wrap :flex 1}
   [react/view
    [react/text {:style {:typography :main-medium}} "Main button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     [button/button {:label "Label"}]
     [button/button {:label "Label" :disabled? true}]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Secondary button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     [button/button {:label "Label" :type :secondary}]
     [button/button {:label "Label" :type :secondary :disabled? true}]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Next and previous buttons"]
    [react/view {:padding 40 :background-color :while :border-radius 20 :flex-direction :row}
     [react/view
      [button/button {:label "Label" :type :next}]
      [button/button {:label "Label" :type :previous}]]
     [react/view
      [button/button {:label "Label" :type :next :disabled? true}]
      [button/button {:label "Label2" :type :previous :disabled? true}]]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Switch"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     [react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       @sw
       :on-value-change #(swap! sw not)
       :disabled    false}]
     [react/view {:height 20}]
     [react/switch
      {:track-color #js {:true colors/blue :false nil}
       :value       false
       :disabled    true}]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Badge"]
    [react/view {:padding 40 :background-color :while :border-radius 20 :flex-direction :row :flex-wrap :wrap}
     [react/view {:margin 10}
      [badge/badge "8"]]
     [react/view {:margin 10}
      [badge/badge "8" true]]
     [react/view {:margin 10}
      [badge/badge "128"]]
     [react/view {:margin 10}
      [badge/badge "338" true]]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Checkbox"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     [checkbox/checkbox
      {:checked? @ch :on-value-change #(swap! ch not)}]
     [react/view {:height 20}]
     [checkbox/checkbox
      {:checked? @ch2 :on-value-change #(swap! ch2 not)}]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Radio button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     [radio/radio true]
     [react/view {:height 20}]
     [radio/radio false]]]])