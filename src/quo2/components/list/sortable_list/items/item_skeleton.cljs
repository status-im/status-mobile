(ns quo2.components.list.sortable-list.items.item-skeleton 
  (:require [quo.react-native :as rn]
            [quo2.components.list.sortable-list.style :as style]))

(defn view
  []
  [rn/view
   {:style style/skeleton-container}])