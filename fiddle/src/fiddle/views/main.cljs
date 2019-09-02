(ns fiddle.views.main
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.button :as button]
            [fiddle.views.colors :as colors]
            [fiddle.views.screens :as screens]
            [fiddle.views.typography :as typography]
            [fiddle.views.icons :as icons]
            [fiddle.views.list-items :as list-items]
            [fiddle.views.ui :as ui]
            [fiddle.views.toolbar :as toolbar]
            [re-frame.core :as re-frame]))

(defn btn [id label view-id]
  [react/view {:margin-bottom 5}
   [button/button
    {:label     label
     :disabled? (= id view-id)
     :type      :secondary
     :on-press  #(re-frame/dispatch [:set :view-id id])}]])

(views/defview main []
  (views/letsubs [view-id [:view-id]]
    [react/view {:flex 1 :flex-direction :row :padding 10}
     [react/view {:padding-right 20}
      [btn :colors "Colors" view-id]
      [btn :icons "Icons" view-id]
      [btn :typography "Typography" view-id]
      [btn :list-header "List Header" view-id]
      [btn :list-items "List Items" view-id]
      [btn :top-bar "Top Bar" view-id]
      [btn :ui "UI elements" view-id]
      [btn :tooltip "Tooltips" view-id]
      [btn :bottom "Bottom Sheet" view-id]
      [btn :popover "Popover" view-id]
      [btn :input "Text input" view-id]
      [btn :tabbar "Tab Bar" view-id]
      [btn :toolbar "Toolbar" view-id]
      [btn :snack "Snackbar" view-id]
      [btn :screens "Screens" view-id]]
     (case view-id
       :colors [colors/colors]
       :icons [icons/icons]
       :screens [screens/screens]
       :typography [typography/typography]
       :list-items [list-items/list-items]
       :toolbar [toolbar/toolbar]
       :list-header [list-items/list-header]
       :ui [ui/ui]
       [react/text "TODO"])]))