(ns status-im2.contexts.chat.messages.content.unknown.view
  (:require [react-native.core :as rn]))

(defn unknown-content
  [{:keys [content-type content]}]
  [rn/text
   (if (seq (:text content))
     (:text content)
     (str "Unhandled content-type " content-type))])
