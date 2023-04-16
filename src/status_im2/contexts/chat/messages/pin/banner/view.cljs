(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn resolve-message
  [parsed-text]
  (reduce
   (fn [acc {:keys [type literal destination] :as some-text}]
     (str acc
          (case type
            "paragraph"
            (resolve-message (:children some-text))

            "mention"
            (rf/sub [:messages/resolve-mention literal])

            "status-tag"
            (str "#" literal)

            "link"
            destination

            literal)))
   ""
   parsed-text))

(defn banner
  [chat-id]
  (let [pinned-messages (rf/sub [:chats/pinned-sorted-list chat-id])
        latest-pinned-message-id (-> pinned-messages last :message-id)
        latest-pinned-message (get (rf/sub [:chats/chat-messages chat-id]) latest-pinned-message-id)
        latest-pin-text (get-in latest-pinned-message [:content :parsed-text])
        {:keys [deleted? deleted-for-me?]} latest-pinned-message
        pins-count (count pinned-messages)

        latest-pin-text
        (cond deleted?        (i18n/label :t/message-deleted-for-everyone)
              deleted-for-me? (i18n/label :t/message-deleted-for-you)
              :else           (resolve-message latest-pin-text))]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        (fn []
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]))


