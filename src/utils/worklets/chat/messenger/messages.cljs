(ns utils.worklets.chat.messenger.messages)

(def ^:private worklets (js/require "../src/js/worklets/chat/messenger/messages.js"))

(defn messages-list-on-scroll
  [distance-from-list-top chat-list-scroll-y callback]
  (.messagesListOnScroll ^js worklets distance-from-list-top chat-list-scroll-y callback))

(defn use-messages-scrolled-to-top
  "Returns true if `distance-from-list-top` (animated value) crossed the threshold.
  e.g. reaching the very top would need a threshold of `0`"
  [distance-from-list-top threshold]
  (.useMessagesScrolledToTop ^js worklets distance-from-list-top threshold))
