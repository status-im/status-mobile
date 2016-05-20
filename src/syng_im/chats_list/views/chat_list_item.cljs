(ns syng-im.chats-list.views.chat-list-item
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font]]
            [syng-im.chats-list.views.inner-item :refer
             [chat-list-item-inner-view]]))

(defn chat-list-item [{:keys [chat-id] :as chat}]
  [touchable-highlight
   {:on-press #(dispatch [:show-chat chat-id :push])}
   [view [chat-list-item-inner-view (merge chat
                                           ;; TODO stub data
                                           {:new-messages-count 3
                                            :online             true})]]])
