(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.resolver.message-resolver :as resolver]
            [status-im2.constants :as constants]))

(defn message-text
  [{:keys [content-type] :as message}]
  (cond (= content-type constants/content-type-audio)
        (i18n/label :audio-message)
        :else
        (get-in message [:content :parsed-text])))

(defn banner
  [chat-id]
  (let [pinned-message (rf/sub [:chats/last-pinned-message chat-id])
        latest-pin-text (message-text pinned-message)
        {:keys [deleted? deleted-for-me?]} pinned-message
        pins-count (rf/sub [:chats/pin-messages-count chat-id])
        content-type-text? (= (:content-type pinned-message) constants/content-type-text)
        latest-pin-text
        (cond deleted?           (i18n/label :t/message-deleted-for-everyone)
              deleted-for-me?    (i18n/label :t/message-deleted-for-you)
              content-type-text? (resolver/resolve-message latest-pin-text)
              :else              latest-pin-text)]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        (fn []
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))}]))


