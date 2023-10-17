(ns quo2.components.settings.reorder-item.items.item-placeholder
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.settings.reorder-item.style :as style]
    [react-native.core :as rn]))

(defn view
  [item]
  (let [label (:label item)]
    [rn/view
     {:accessibility-label :reorder-placerholder-drag-handle
      :style               (style/placeholder-container)}
     [text/text
      {:style  (style/placeholder-text)
       :weight :regular}
      label]]))
