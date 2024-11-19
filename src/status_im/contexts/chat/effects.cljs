(ns status-im.contexts.chat.effects
  (:require
    [react-native.async-storage :as async-storage]
    [status-im.contexts.chat.messenger.messages.list.state :as chat.state]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.chat/open-last-chat
 (fn [key-uid]
   (async-storage/get-item
    :chat-id
    (fn [chat-id]
      (when chat-id
        (async-storage/get-item
         :key-uid
         (fn [stored-key-uid]
           (when (= stored-key-uid key-uid)
             (rf/dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id])))))))))

(rf/reg-fx :effects.chat/scroll-to-bottom
 (fn []
   (some-> ^js @chat.state/messages-list-ref
           (.scrollToOffset #js
                             {:animated true}))))
