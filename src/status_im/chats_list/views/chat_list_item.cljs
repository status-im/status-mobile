(ns status-im.chats-list.views.chat-list-item
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.chats-list.views.inner-item :refer [chat-list-item-inner-view]]))

(defn chat-list-item [[chat-id chat]]
  [touchable-highlight
   {:on-press #(dispatch [:navigate-to :chat chat-id])}
   [view [chat-list-item-inner-view (merge chat
                                           ;; TODO stub data
                                           {:chat-id            chat-id
                                            :new-messages-count 3
                                            :online             true})]]])
