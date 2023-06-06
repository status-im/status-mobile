(ns  quo2.components.settings.reorder-item.items.item-placeholder 
  (:require [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [ quo2.components.settings.reorder-item.style :as style]))

(defn view
  [label drag]
  [rn/view
   {:on-long-press       drag
    :delay-long-press    100
    :accessibility-label :chat-drag-handle
    :style               style/placeholder-container}
   [text/text
    {:style style/placeholder-text
     :weight :regular}
    label]
   ])
