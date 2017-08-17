(ns status-im.components.list.styles
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as p]))

(def item
  {:flex            1
   :flex-direction  :row})

(def item-text-view
  {:flex            1
   :flex-direction  :column})

(def primary-text
  {:font-size  20
   :color      st/color-black
   :margin-top 13})

(def secondary-text
  {:font-size  16
   :color      st/color-gray4
   :margin-top 6})

(def item-icon
  {:width  40
   :height 40
   :margin 14})

(def primary-action item-icon)

(def secondary-action item-icon)

(def action-buttons
  {:flex            1
   :flex-direction  :row})