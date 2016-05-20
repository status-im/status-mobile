(ns status-im.chats-list.views.chat-list-item
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                              text
                                              image
                                              touchable-highlight]]
            [status-im.components.styles :refer [font]]
            [status-im.chats-list.views.inner-item :refer
             [chat-list-item-inner-view]]))

(defn chat-list-item [{:keys [chat-id] :as chat}]
  [touchable-highlight
   {:on-press #(dispatch [:show-chat chat-id :push])}
   ;; TODO add [photo-path delivery-status new-messages-count online] values to chat-obj
   [view [chat-list-item-inner-view (merge chat
                                           {:photo-path         nil
                                            :delivery-status    :seen
                                            :new-messages-count 3
                                            :timestamp          "13:54"
                                            :online             true})]]])
