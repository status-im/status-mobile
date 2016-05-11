(ns syng-im.chats-list.views.chat-list-item
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font]]
            [syng-im.chats-list.views.inner-item :refer
             [chat-list-item-inner-view]]))

(defn chat-list-item [chat-obj]
  [touchable-highlight
   {:on-press #(dispatch [:show-chat (aget chat-obj "chat-id") :push])}
   ;; TODO add [photo-path delivery-status new-messages-count online] values to chat-obj
   ;; TODO should chat-obj be clj-map?
   [view [chat-list-item-inner-view (merge (js->clj chat-obj :keywordize-keys true)
                                           {:photo-path         nil
                                            :delivery-status    :seen
                                            :new-messages-count 3
                                            :timestamp          "13:54"
                                            :online             true})]]])
