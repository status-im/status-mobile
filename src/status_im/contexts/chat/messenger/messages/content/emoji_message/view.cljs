(ns status-im.contexts.chat.messenger.messages.content.emoji-message.view
  (:require [react-native.core :as rn]
            [status-im.contexts.chat.messenger.messages.content.emoji-message.style :as style]))

(defn view
  [{:keys [content last-in-group? pinned in-pinned-view?]}]
  (let [margin-top (if (or last-in-group? in-pinned-view? pinned) 8 0)]
    [rn/view {:style (style/emoji-container margin-top)}
     [rn/text {:style style/emoji-text}
      (:text content)]]))
