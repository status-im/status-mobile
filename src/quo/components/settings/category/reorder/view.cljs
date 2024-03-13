(ns quo.components.settings.category.reorder.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.reorder-item.types :as types]
    [quo.components.settings.reorder-item.view :as reorder-item]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.draggable-flatlist :as draggable-flatlist]
    [reagent.core :as reagent]))

(defn on-drag-end-fn
  [data atom-data]
  (reset! atom-data data)
  (reagent/flush))

(defn- reorder-category-internal
  [{:keys [label data blur? theme container-style]}]
  (reagent/with-let [atom-data (reagent/atom data)]
    [rn/view {:style (merge (style/container label) container-style)}
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
      label]
     [draggable-flatlist/draggable-flatlist
      {:data           @atom-data
       :key-fn         (fn [item index] (str (:title item) index))
       :style          style/reorder-items
       :render-fn      (fn [item _ _ _ _ drag] [reorder-item/reorder-item item types/item
                                                {:blur? blur? :drag drag}])
       :on-drag-end-fn (fn [_ _ data]
                         (on-drag-end-fn data atom-data))
       :separator      [rn/view
                        {:style (style/reorder-separator blur? theme)}]}]]))

(def reorder-category (quo.theme/with-theme reorder-category-internal))
