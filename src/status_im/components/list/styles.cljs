(ns status-im.components.list.styles
  (:require [status-im.components.styles :as st]
            [status-im.utils.platform :as p]))

(def item
  {:flex            1
   :flex-direction  :row})

(def item-text-view
  {:flex            1
   :flex-direction  :column})

(def primary-text-base
  {:font-size   17
   :color       st/color-black})

(def primary-text
  (merge primary-text-base
         {:padding-top 12}))

(def primary-text-only
  (merge primary-text-base
         {:padding-vertical 22}))

(def secondary-text
  {:font-size   16
   :color       st/color-gray4
   :padding-top 4})

(def item-icon
  {:width   40
   :height  40
   :margin  14})

(def primary-action item-icon)

(def secondary-action item-icon)

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
  {:font-size       14
   :margin-vertical 2
   :margin-top      16
   :margin-left     16})