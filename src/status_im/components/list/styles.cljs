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
  {:font-size   20
   :color       st/color-black
   :padding-top 13})

(def secondary-text
  {:font-size   16
   :color       st/color-gray4
   :padding-top 6})

(def item-icon
  {:width   40
   :height  40
   :margin  14})

(def primary-action item-icon)

(def secondary-action item-icon)

(def action-buttons
  {:flex             1
   :flex-direction   :row
   :padding-vertical 12})

(def base-separator
  {:height           1
   :background-color st/color-gray5
   :opacity          0.5
   :margin-top       12
   :margin-bottom    16})

(def separator
  (merge
    base-separator
    {:margin-left   70}))

(def section-separator base-separator)

(def section-header
  {:margin-vertical   2
   :margin-bottom     12
   :margin-top        16
   :margin-left       16})