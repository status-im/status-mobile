(ns quo.components.settings.reorder-item.view
  (:require
    [quo.components.settings.reorder-item.items.item :as item]
    [quo.components.settings.reorder-item.items.item-placeholder :as placeholder]
    [quo.components.settings.reorder-item.items.item-skeleton :as skeleton]
    [quo.components.settings.reorder-item.items.item-tabs :as tab]
    [quo.components.settings.reorder-item.types :as types]))

(defn reorder-item
  [item type {:keys [blur? drag]}]
  (condp = type
    types/item        [item/view item blur? drag]
    types/placeholder [placeholder/view item]
    types/skeleton    [skeleton/view]
    types/tab         [tab/view item]
    nil))
