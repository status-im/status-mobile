(ns status-im2.contexts.chat.messages.list.state)

(defonce first-not-visible-item (atom nil))

(defonce scrolling (atom nil))

(defn start-scrolling
  []
  (reset! scrolling true))

(defn stop-scrolling
  []
  (reset! scrolling false))

(defn reset-visible-item
  []
  (reset! first-not-visible-item nil))
