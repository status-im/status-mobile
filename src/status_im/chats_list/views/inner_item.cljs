(ns status-im.chats-list.views.inner-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view image icon text]]
            [status-im.components.chat-icon.screen :refer [chat-icon-view-chat-list]]
            [status-im.chats-list.styles :as st]
            [status-im.utils.utils :refer [truncate-str]]
            [status-im.utils.datetime :as time]))

(defn chat-list-item-inner-view
  [{:keys [chat-id name color new-messages-count
           online group-chat contacts] :as chat}]
  (let [last-message (first (:messages chat))]
    [view st/chat-container
     [view st/chat-icon-container
      [chat-icon-view-chat-list chat-id group-chat name color online]]
     [view st/item-container
      [view st/name-view
       [text {:style st/name-text} (truncate-str name 20)]
       (when group-chat
         [icon :group st/group-icon])
       (when group-chat
         [text {:style st/memebers-text}
          (if (< 0 (count contacts))
            (str (inc (count contacts)) " members")
            "1 member")])]
      [text {:style         st/last-message-text
             :numberOfLines 2}
       (when last-message
         (let [content (:content last-message)]
           (if (string? content)
             content
             (:content content))))]]
     [view
      (when last-message
        [view st/status-container
         ;; TODO currently there is not :delivery-status in last-message
         (when (:delivery-status last-message)
           [image {:source (if (= (keyword (:delivery-status last-message)) :seen)
                             {:uri :icon_ok_small}
                             ;; todo change icon
                             {:uri :icon_ok_small})
                   :style  st/status-image}])
         (when (:timestamp last-message)
           [text {:style st/datetime-text}
            (time/to-short-str (:timestamp last-message))])])
      (when (pos? new-messages-count)
        [view st/new-messages-container
         [text {:style st/new-messages-text} new-messages-count]])]]))
