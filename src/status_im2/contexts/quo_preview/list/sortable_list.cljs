(ns status-im2.contexts.quo-preview.list.sortable-list
  (:require
   [quo2.components.list.sortable-list.view :as list]
   [quo2.components.list.sortable-list.mock-data :as mock-data]
   [quo.react-native :as rn]))

(defn preview-sortable-list 
  []
    [rn/view
     [list/reorder-list mock-data/data]])