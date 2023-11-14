(ns quo.components.settings.reorder-item.items.item-skeleton
  (:require
    [quo.components.settings.reorder-item.style :as style]
    [react-native.core :as rn]))

(defn view
  []
  [rn/view
   {:style (style/skeleton-container)}])
