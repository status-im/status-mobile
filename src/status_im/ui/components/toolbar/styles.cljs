(ns status-im.ui.components.toolbar.styles
  (:require [status-im.ui.components.colors :as colors]))

(def toolbar-height 56)
(def toolbar-title-container
  {:justify-content :center
   :align-items     :center
   :flex-direction  :column})

(def toolbar-title-text
  {:typography :title-bold
   :text-align :center})

(def touchable-area
  {:width 56
   :height 56
   :justify-content :center
   :align-items :center})

(def item-text
  {:color colors/blue})

(def toolbar-text-action-disabled
  {:color colors/gray})