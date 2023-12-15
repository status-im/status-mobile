(ns status-im2.contexts.chat.effects
  (:require
    [react-native.async-storage :as async-storage]
    [status-im2.contexts.shell.jump-to.constants :as shell.constants]
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
             (rf/dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id
                           shell.constants/open-screen-without-animation])))))))))
