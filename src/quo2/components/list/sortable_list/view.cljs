(ns quo2.components.list.sortable-list.view 
  (:require [quo.react-native :as rn]
            [react-native.platform :as platform]
            [quo2.components.list.sortable-list.items.item :as item]
            [reagent.core :as reagent]
            [quo2.components.list.sortable-list.style :as style]
            [quo2.components.list.sortable-list.items.item-placeholder :as placeholder]
            [quo2.components.list.sortable-list.items.item-skeleton :as skeleton]
            [quo2.components.list.sortable-list.items.item-tabs :as tab]))


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
  (reagent/flush)
)

(defn reorder-list [data] 
  (let [atom-data (reagent/atom data)]
  [rn/view
   {:style style/container}
   [rn/draggable-flat-list
    {:key-fn               :id
     :test-ID              "sortable-list"
     :data                 @atom-data
     :render-fn            render-fn
     :autoscroll-threshold (if platform/android? 150 250)
     :autoscroll-speed     (if platform/android? 10 150)
     :content-container-style {:padding-vertical 20}
     :shows-vertical-scroll-indicator false
     :on-drag-end-fn       (fn [_ _ data-js]
                             (on-drag-end-fn data-js atom-data))}]]))