(ns quo2.components.settings.reorder-item.view
  (:require
    [quo2.components.settings.reorder-item.items.item :as item]
    [quo2.components.settings.reorder-item.items.item-placeholder :as placeholder]
    [quo2.components.settings.reorder-item.items.item-skeleton :as skeleton]
    [quo2.components.settings.reorder-item.items.item-tabs :as tab]
    [quo2.components.settings.reorder-item.types :as types]))

(defn reorder-item
  [item type blur? drag]
  (case type
    types/item        [item/view item blur? drag]
    types/placeholder [placeholder/view item]
    types/skeleton    [skeleton/view]
    types/tab         [tab/view item]
    nil))
