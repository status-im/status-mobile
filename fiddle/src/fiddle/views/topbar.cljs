(ns fiddle.views.topbar
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.colors :as colors]
            cljs.pprint
            [reagent.core :as reagent])
  (:require-macros [fiddle.snippets :as snippets]))

(defn topbar-view []
  [react/view
   {:background-color colors/gray-lighter
    :flex             1
    :padding          20
    :flex-direction   :row
    :flex-wrap        :wrap}
   [react/view {:width 375}
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Chat Invites"}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Chat Invites tile tile title  tile tile title tile tile title tile tile title tile tile title tile tile title 3"}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Chat" :navigation :none :show-border? true}])
     [react/view {:height 10}]]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Blocked Users" :accessories [{:label "Done" :handler #(js/alert "Done")}]}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Blocked Users" :accessories [{:label "Done" :handler #(js/alert "Done")}] :navigation {:label "Cancel" :handler #(js/alert "Cancel")}}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Chat Invites" :accessories [{:icon :more :handler #(js/alert "More")}]}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "Title" :accessories [{:icon :more :handler #(js/alert "More")} {:icon :share :handler #(js/alert "Share")}]}])]
    [react/view {:background-color :white :margin 10}
     (snippets/code-snippet [topbar/topbar {:title "test"}])]]])