(ns status-im.ui.components.chat
  (:require [status-im.ui.components.react :as react]))

(defn message-author-name [style {:keys [from user-name generated-name]}]
  [react/view {:flex-direction :row}
   (when user-name
     [react/text {:style style} user-name])
   (when (and user-name generated-name)
     [react/text {:style style} " :: "])
   (when generated-name
     [react/text {:style style} generated-name])])
