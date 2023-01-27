(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.re-frame :as rf]))

(defn banner
  [chat-id]
  (let [pinned-messages (rf/sub [:chats/pinned chat-id])
        latest-pin-id   (-> pinned-messages
                            vals
                            first
                            (get :message-id))
        latest-pin-text (get-in (rf/sub [:chats/chat-messages chat-id])
                                [latest-pin-id :content :text])
        pins-count      (count (seq pinned-messages))]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        #(do
                          (rf/dispatch [:dismiss-keyboard])
                          (rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id]))}]))


