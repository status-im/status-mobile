(ns quo2.components.settings.category.reorder.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.settings.reorder-item.types :as types]
    [quo2.components.settings.reorder-item.view :as reorder-item]
    [quo2.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [quo2.components.settings.category.style :as style]
    [quo2.theme :as quo.theme]
    [react-native.draggable-flatlist :as draggable-flatlist]
    [reagent.core :as reagent]))

(defn on-drag-end-fn
  [data atom-data]
  (reset! atom-data data)
  (reagent/flush))

(defn- reorder-category-internal
  [{:keys [label data blur? theme]}]
  (reagent/with-let [atom-data (reagent/atom data)]
    [rn/view {:style style/container}
     (when blur?
       [rn/view (style/blur-container) [blur/view (style/blur-view)]])
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
      label]
     [draggable-flatlist/draggable-flatlist
      {:data           @atom-data
       :key-fn         (fn [item index] (str (:title item) index))
       :style          (style/reorder-items theme blur?)
       :render-fn      (fn [item _ _ _ _ drag] [reorder-item/reorder-item item types/item blur? drag])
       :on-drag-end-fn (fn [_ _ data]
                         (on-drag-end-fn data atom-data))
       :separator      [rn/view
                        {:style (style/reorder-separator blur?)}]}]]))

(def reorder-category (quo.theme/with-theme reorder-category-internal))
