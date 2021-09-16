(ns status-im.ui.components.emoji-thumbnail.color-picker
  (:require [status-im.ui.components.list.views :as list]
            [status-im.ui.components.emoji-thumbnail.styles :as styles]))

(defn colors-row1 [color-circle]
  [list/flat-list {:data                   styles/emoji-picker-colors-row1
                   :render-fn              color-circle
                   :contentContainerStyle  styles/emoji-picker-color-row-container
                   :horizontal             true
                   :style                  styles/emoji-picker-row1-style}])

(defn colors-row2 [color-circle]
  [list/flat-list {:data                   styles/emoji-picker-colors-row2
                   :render-fn              color-circle
                   :contentContainerStyle  styles/emoji-picker-color-row-container
                   :horizontal             true
                   :style                  styles/emoji-picker-row2-style}])

(defn colors-row3 [color-circle]
  [list/flat-list {:data                   styles/emoji-picker-colors-row3
                   :render-fn              color-circle
                   :contentContainerStyle  styles/emoji-picker-color-row-container
                   :horizontal             true
                   :style                  styles/emoji-picker-row3-style}])

(defn color-picker-section [color-circle]
  [:<>
   [colors-row1 color-circle]
   [colors-row2 color-circle]
   [colors-row3 color-circle]])
