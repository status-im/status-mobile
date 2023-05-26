(ns status-im2.contexts.quo-preview.list.sortable-list
  (:require
   [quo2.components.list.sortable-list.view :as list]
   [quo.react-native :as rn]))

(defn preview-sortable-list 
  []
    [rn/view
     [list/reorder-list]])