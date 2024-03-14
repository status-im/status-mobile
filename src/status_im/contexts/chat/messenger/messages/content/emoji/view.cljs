(ns status-im.contexts.chat.messenger.messages.content.emoji.view
  (:require [react-native.core :as rn]
            [status-im.contexts.chat.messenger.messages.content.emoji.style :as style]))

(defn emoji-message
  [{:keys [content last-in-group? pinned]} {:keys [in-pinned-view?]}]
  (let [margin-top (if (or last-in-group? in-pinned-view? pinned) 8 0)]
    [rn/view {:style (style/emoji-container margin-top)}
     [rn/text {:style style/emoji-text}
      (:text content)]]))
