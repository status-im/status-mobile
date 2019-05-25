(ns status-im.ui.components.list-header.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn list-header [title]
  [react/view {:style {:padding-top 14 :padding-bottom 4 :padding-horizontal 16}}
   [react/text {:style {:color colors/gray}} title]])