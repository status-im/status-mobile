(ns quo2.components.settings.reorder-category.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.settings.reorder-item.types :as types]
    [quo2.components.settings.reorder-item.view :as reorder-item]
    [quo2.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [quo2.components.settings.reorder-category.style :as style]
    [quo2.theme :as quo.theme]
    [react-native.draggable-flatlist :as draggable-flatlist]))

(defn- reorder-category-internal
  [{:keys [label data blur? theme]}]
  [rn/view {:style style/container}
   (when blur?
     [rn/view (style/blur-container) [blur/view (style/blur-view)]])
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
    label]
   [draggable-flatlist/draggable-flatlist
    {:data      data
     :key-fn    (fn [item index] (str (:title item) index))
     :style     (style/items theme blur?)
     :render-fn (fn [item] [reorder-item/reorder-item item types/item blur?])
     :separator [rn/view
                 {:style {:height           4
                          :background-color (if blur?
                                              :transparent
                                              (colors/theme-colors colors/neutral-5
                                                                   colors/neutral-95))}}]}]])

(def reorder-category (quo.theme/with-theme reorder-category-internal))
