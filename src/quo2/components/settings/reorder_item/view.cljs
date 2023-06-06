(ns  quo2.components.settings.reorder-item.view
  (:require [react-native.core :as rn]
            [react-native.platform :as platform]
            [quo2.components.settings.reorder-item.items.item :as item]
            [reagent.core :as reagent]
            [quo2.components.settings.reorder-item.style :as style]
            [quo2.components.settings.reorder-item.items.item-placeholder :as placeholder]
            [quo2.components.settings.reorder-item.items.item-skeleton :as skeleton]
            [quo2.components.settings.reorder-item.items.item-tabs :as tab]))


(defn render-fn
  [item _ _ _ _ drag]
  (let [label-value (:label item)
        type (:type item)
        data (:data item)
        default-active (:default-active item)]
    (case type
      "item" [item/view item drag]
      "placeholder" [placeholder/view label-value drag]
      "skeleton" [skeleton/view]
      "tab" [tab/view data default-active])))

(defn on-drag-end-fn
  [data-js data]
  (reset! data data-js)
  (reagent/flush))

(defn reorder-item [data]
  (let [atom-data (reagent/atom data)]
    [rn/view
     {:style style/container}
     [rn/draggable-flatlist
      {:key-fn               :id
       :accessibility-label  :sortable-list
       :data                []
       :render-fn            (fn [] [rn/view])
       :autoscroll-threshold (if platform/android? 150 250)
       :autoscroll-speed     (if platform/android? 10 150)
       :content-container-style {:padding-vertical 20}
       :shows-vertical-scroll-indicator false
       }]]))
