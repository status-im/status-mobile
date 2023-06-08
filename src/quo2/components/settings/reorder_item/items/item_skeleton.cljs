(ns  quo2.components.settings.reorder-item.items.item-skeleton 
  (:require [quo.react-native :as rn]
            [ quo2.components.settings.reorder-item.style :as style]))

(defn view
  []
  [rn/view
   {:style (style/skeleton-container)}])
