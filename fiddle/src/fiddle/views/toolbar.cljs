(ns fiddle.views.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar-comp]
            [status-im.ui.components.colors :as colors]))

(defn toolbar []
  [react/view
   {:background-color colors/gray-lighter
    :flex             1
    :padding          20
    :flex-direction   :row
    :flex-wrap        :wrap}
   [react/view {:width 375}
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:center {:type :secondary :label "Label"}}]]
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:left {:type :previous :label "Label"} :right {:type :next :label "Label"}}]]
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:left {:type :secondary :label "Label"} :right {:label "Label"}}]]
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:left {:type :previous :label "Label"}}]]
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:right {:type :next :label "Label"}}]]
    [react/view {:background-color :white :margin 10}
     [toolbar-comp/toolbar {:center {:label "Label"}}]]]])