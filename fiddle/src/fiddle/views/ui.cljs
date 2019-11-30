(ns fiddle.views.ui
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.badge :as badge]
            [status-im.ui.components.checkbox.view :as checkbox]
            [reagent.core :as reagent]
            [status-im.ui.components.radio :as radio]
            cljs.pprint)
  (:require-macros [fiddle.snippets :as snippets]))

(def sw (reagent/atom true))
(def ch (reagent/atom true))
(def ch2 (reagent/atom false))

(defn ui []
  [react/view {:flex-direction :row :flex-wrap :wrap :flex 1}
   [react/view
    [react/text {:style {:typography :main-medium}} "Main button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     (snippets/code-snippet [button/button {:label "Label"}] true)
     (snippets/code-snippet [button/button {:label "Label" :disabled? true}] true)]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Secondary button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     (snippets/code-snippet [button/button {:label "Label" :type :secondary}] true)
     (snippets/code-snippet [button/button {:label "Label" :type :secondary :disabled? true}] true)]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Next and previous buttons"]
    [react/view {:padding 40 :background-color :while :border-radius 20 :flex-direction :row}
     [react/view
      (snippets/code-snippet [button/button {:label "Label" :type :next}] true)
      (snippets/code-snippet [button/button {:label "Label" :type :previous}] true)]
     [react/view
      (snippets/code-snippet [button/button {:label "Label" :type :next :disabled? true}] true)
      (snippets/code-snippet [button/button {:label "Label2" :type :previous :disabled? true}] true)]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Switch"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     (snippets/code-snippet
      [react/switch
       {:track-color #js {:true colors/blue :false nil}
        :value       @sw
        :on-value-change #(swap! sw not)
        :disabled    false}]
      true)
     [react/view {:height 20}]
     (snippets/code-snippet
      [react/switch
       {:track-color #js {:true colors/blue :false nil}
        :value       false
        :disabled    true}]
      true)]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Badge"]
    [react/view {:padding 40 :background-color :while :border-radius 20 :flex-direction :row :flex-wrap :wrap}
     [react/view {:margin 10}
      (snippets/code-snippet [badge/badge "8"] true)]
     [react/view {:margin 10}
      [badge/badge "8" true]]
     [react/view {:margin 10}
      [badge/badge "128"]]
     [react/view {:margin 10}
      [badge/badge "338" true]]]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Checkbox"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     (snippets/code-snippet
      [checkbox/checkbox
       {:checked? @ch :on-value-change #(swap! ch not)}]
      true)
     [react/view {:height 20}]
     (snippets/code-snippet
      [checkbox/checkbox
       {:checked? @ch2 :on-value-change #(swap! ch2 not)}]
      true)]]
   [react/view
    [react/text {:style {:typography :main-medium}} "Radio button"]
    [react/view {:padding 40 :background-color :while :border-radius 20}
     (snippets/code-snippet [radio/radio true] true)
     [react/view {:height 20}]
     (snippets/code-snippet [radio/radio false] true)]]])