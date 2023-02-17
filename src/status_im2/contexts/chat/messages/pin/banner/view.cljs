(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn banner
  [chat-id]
  (let [pinned-messages (rf/sub [:chats/pinned-sorted-list
                                 chat-id])
        latest-pinned-message (last pinned-messages)
        latest-pin-text (get-in latest-pinned-message [:content :text])
        {:keys [deleted? deleted-for-me?]} latest-pinned-message
        pins-count (count pinned-messages)

        latest-pin-text
        (cond deleted?        (i18n/label :t/message-deleted-for-everyone)
              deleted-for-me? (i18n/label :t/message-deleted-for-you)
              :else           latest-pin-text)]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        (fn []
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id]))}]))


