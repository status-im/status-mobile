(ns status-im.ui.components.chat-preview
  (:require [status-im.ui.components.react :as components]
            [status-im.ui.screens.chats-list.styles :as st]
            [status-im.utils.utils :as utils]))

(def default-attributes
  {:style           st/last-message-text
   :number-of-lines 1})

(defn text [attributes s]
  (-> default-attributes
      (utils/deep-merge attributes)
      (components/text s)))
