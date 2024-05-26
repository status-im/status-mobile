(ns quo.components.settings.reorder-item.items.item-placeholder
  (:require
    [quo.components.markdown.text :as text]
    [quo.components.settings.reorder-item.style :as style]
    [react-native.core :as rn]))

(defn view
  [item theme]
  (let [label (:label item)]
    [rn/view
     {:accessibility-label :reorder-placerholder-drag-handle
      :style               (style/placeholder-container theme)}
     [text/text
      {:style  (style/placeholder-text theme)
       :weight :regular}
      label]]))
