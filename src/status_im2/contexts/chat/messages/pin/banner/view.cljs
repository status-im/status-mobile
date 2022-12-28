(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [status-im.ui2.screens.chat.pinned-banner.view :as pinned-banner]
            [utils.re-frame :as rf]))

(defn banner
  [chat-id]
  (let [pinned-messages (rf/sub [:chats/pinned chat-id])
        latest-pin-id   (-> pinned-messages
                            vals
                            last
                            (get :message-id))
        latest-pin-text (get-in (rf/sub [:chats/chat-messages chat-id])
                                [latest-pin-id :content :text])
        pins-count      (count (seq pinned-messages))]
    (when (> pins-count 0)
      ;; TODO (flexsurfer) this should be banner component in quo2
      [pinned-banner/pinned-banner
       {:latest-pin-text latest-pin-text
        :pins-count      pins-count
        :on-press        #(rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id])}])))
