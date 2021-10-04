(ns status-im.ui.components.emoji-thumbnail.preview
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.emoji-thumbnail.styles :as styles]
            [clojure.string :as string]))

(defn emoji-thumbnail [emoji color size]
  (when-not (string/blank? emoji)
    [react/view (styles/emoji-thumbnail-icon color size)
     [react/text {:style (styles/emoji-thumbnail-icon-text size)} emoji]]))

(defn emoji-thumbnail-touchable [emoji color size func]
  (when-not (string/blank? emoji)
    [react/touchable-opacity {:on-press func}
     [emoji-thumbnail emoji color size]]))
