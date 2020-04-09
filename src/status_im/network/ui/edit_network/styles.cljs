(ns status-im.network.ui.edit-network.styles
  (:require [status-im.ui.components.styles :as styles]))

(def edit-network-view
  {:flex              1
   :margin-horizontal 16
   :margin-vertical   15})

(def input-container
  {:margin-bottom 15})

(def network-type
  {:flex-direction :row
   :align-items    :center})

(def bottom-container
  {:flex-direction   :row
   :padding-vertical 4})

(def container
  styles/flex)
