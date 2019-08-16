(ns fiddle.views.typography
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.typography :as typography]))

(defn typo [name key]
  [react/view {:flex-direction :row :align-items :center :margin-bottom 40}
   [react/text {:style {:typography key :margin-right 20 :width 200}} name]
   [react/text {:style {:margin-right 20 :width 200}} (str key)]
   [react/text {:style {:color colors/gray}} (get typography/typography-styles key)]])

(defn typography []
  [react/view {:flex-direction :row :flex 1}
   [react/view {:padding 20}
    [typo "Header" :header]
    [typo "Title Bold" :title-bold]
    [typo "Title" :title]
    [typo "Main Medium" :main-medium]
    [typo "Main" nil]
    [typo "Caption" :caption]
    [typo "TIMESTAMP" :timestamp]]])