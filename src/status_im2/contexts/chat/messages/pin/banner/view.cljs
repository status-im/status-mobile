(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.utils.message-resolver :as resolver]))

(defn banner
  [chat-id]
  (let [pinned-message (rf/sub [:chats/last-pinned-message chat-id])
        latest-pin-text (get-in pinned-message [:content :parsed-text])
        {:keys [deleted? deleted-for-me?]} pinned-message
        pins-count (rf/sub [:chats/pin-messages-count chat-id])

        latest-pin-text
        (cond deleted?        (i18n/label :t/message-deleted-for-everyone)
              deleted-for-me? (i18n/label :t/message-deleted-for-you)
              :else           (resolver/resolve-message latest-pin-text))]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        (fn []
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]))


