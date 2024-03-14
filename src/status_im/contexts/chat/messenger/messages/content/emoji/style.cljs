(ns status-im.contexts.chat.messenger.messages.content.emoji.style)

(defn emoji-container
  [margin-top]
  {:flex-direction :row
   :margin-top     margin-top})

(def emoji-text
  {:font-size   36
   :line-height 42})
