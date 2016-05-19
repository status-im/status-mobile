(ns syng-im.chats-list.views.inner-item
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [syng-im.components.react :refer [view image icon text]]
            [syng-im.chats-list.styles :as st]))

(defn default-chat-icon [{:keys [name color]}]
  [view (st/default-chat-icon color)
   [text {:style st/default-chat-icon-text} (nth name 0)]])

(defview contact-photo [chat]
  [photo-path [:chat-photo (:chat-id chat)]]
  (if photo-path
    [view st/contact-photo-container
     [image {:source {:uri photo-path}
             :style  st/contact-photo-image}]]
    [default-chat-icon chat]))

(defn contact-online [online]
  (when online
    [view st/online-container
     [view st/online-dot-left]
     [view st/online-dot-right]]))

(defn chat-list-item-inner-view
  [{:keys [chat-id name photo-path delivery-status timestamp new-messages-count online
           group-chat contacts] :as chat}]
  [view st/chat-container
   [view st/photo-container
    [contact-photo chat]
    [contact-online online]]
   [view st/item-container
    [view st/name-view
     [text {:style st/name-text} name]
     (when group-chat
       [icon :group st/group-icon])
     (when group-chat
       [text {:style st/memebers-text}
        (if (< 1 (count contacts))
          (str (count contacts) " members")
          "1 member")])]
    [text {:style         st/last-message-text
           :numberOfLines 2}
     (when-let [last-message (first (:messages chat))]
       (:content last-message))]]
   [view
    [view st/status-container
     (when delivery-status
       [image {:source (if (= (keyword delivery-status) :seen)
                         {:uri :icon_ok_small}
                         ;; todo change icon
                         {:uri :icon_ok_small})
               :style  st/status-image}])
     [text {:style st/datetime-text} timestamp]]
    (when (pos? new-messages-count)
      [view st/new-messages-container
       [text {:style st/new-messages-text} new-messages-count]])]])
