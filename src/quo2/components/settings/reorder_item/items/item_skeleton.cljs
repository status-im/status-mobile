(ns quo2.components.settings.reorder-item.items.item-skeleton
  (:require [react-native.core :as rn]
            [quo2.components.settings.reorder-item.style :as style]))

(defn view
  []
  [rn/view
   {:style (style/skeleton-container)}])
