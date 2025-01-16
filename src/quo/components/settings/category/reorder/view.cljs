(ns quo.components.settings.category.reorder.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.category.style :as style]
    [quo.components.settings.reorder-item.types :as types]
    [quo.components.settings.reorder-item.view :as reorder-item]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.draggable-flatlist :as draggable-flatlist]))

(defn key-fn [item index] (str (:title item) index))

(defn reorder-category
  [{:keys [label data blur? container-style]}]
  (let [theme                     (quo.theme/use-theme)
        [atom-data set-atom-data] (rn/use-state data)
        render-fn                 (rn/use-callback
                                   (fn [item _ _ _ _ drag]
                                     [reorder-item/reorder-item item types/item
                                      {:blur? blur? :drag drag}])
                                   [blur?])
        on-drag-end-fn            (rn/use-callback (fn [_ _ data] (set-atom-data data)))
        separator                 (rn/use-memo (fn [] [rn/view
                                                       {:style (style/reorder-separator blur? theme)}])
                                               [blur? theme])]
    [rn/view {:style [(style/container label) container-style]}
     [text/text
      {:weight :medium
       :size   :paragraph-2
       :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
      label]
     [draggable-flatlist/draggable-flatlist
      {:data           atom-data
       :key-fn         key-fn
       :style          style/reorder-items
       :render-fn      render-fn
       :on-drag-end-fn on-drag-end-fn
       :separator      separator}]]))
