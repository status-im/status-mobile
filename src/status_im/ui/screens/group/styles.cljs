(ns status-im.ui.screens.group.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn toolbar-icon [enabled?]
  {:width   20
   :height  18
   :opacity (if enabled? 1 0.3)})

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color colors/white})

(def contact
  {:padding-left 0})

(def contacts-list
  {:background-color colors/white})

(def no-contact-text
  {:margin-bottom     20
   :margin-horizontal 50
   :text-align        :center
   :color             colors/gray})

(def number-of-participants-disclaimer
  {:margin-top        20
   :margin-bottom     5
   :font-size         12
   :margin-horizontal 17})

(def bottom-container
  {:padding-horizontal 12
   :padding-vertical   15})

(def toolbar-header-container
  {:align-items :center})

(def toolbar-sub-header
  {:color colors/gray})

(def no-contacts
  {:flex 1
   :justify-content :center
   :align-items :center})
