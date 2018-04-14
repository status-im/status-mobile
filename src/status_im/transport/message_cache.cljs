(ns ^{:doc "Message caching API for message deduplication"}
    status-im.transport.message-cache
  (:refer-clojure :exclude [exists?]))

(defonce messages-set (atom #{}))

(defn init!
  [messages]
  (reset! messages-set (set (map :message-id messages))))

(defn add!
  [message-id]
  (when message-id
    (swap! messages-set conj message-id)))

(defn exists?
  [message-id]
  (when message-id
    (@messages-set message-id)))
