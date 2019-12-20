(ns fiddle.views.toolbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.colors :as colors]
            cljs.pprint
            [reagent.core :as reagent]
            [status-im.ui.components.button :as button])
  (:require-macros [fiddle.snippets :as snippets]))

(defn toolbar-view []
  [react/view
   {:background-color colors/gray-lighter
    :flex             1
    :padding          20
    :flex-direction   :row
    :flex-wrap        :wrap}
   [react/view {:width 375}
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:center {:type :secondary :label "Label"}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:left {:type :previous :label "Label"} :right {:type :next :label "Label"}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:left {:type :secondary :label "Label"} :right {:label "Label"}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:left {:type :previous :label "Label"}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:right {:type :next :label "Label"}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [toolbar/toolbar {:center {:label "Label"}}])]]])