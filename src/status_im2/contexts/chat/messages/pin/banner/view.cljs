(ns status-im2.contexts.chat.messages.pin.banner.view
  (:require [quo2.core :as quo]
            [utils.re-frame :as rf]))

(defn banner
  [chat-id]
  (let [pinned-messages (rf/sub [:chats/pinned-sorted-list chat-id])
        latest-pin-text (->> pinned-messages
                             last
                             :content
                             :text)
        pins-count      (count pinned-messages)]
    [quo/banner
     {:latest-pin-text latest-pin-text
      :pins-count      pins-count
      :on-press        (fn []
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id]))}]))


