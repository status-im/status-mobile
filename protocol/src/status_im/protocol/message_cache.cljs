(ns status-im.protocol.message-cache
  (:refer-clojure :exclude [exists?]))

(defonce messages-set (atom #{}))
(defonce messages-map (atom {}))

(defn init!
  [messages]
  (reset! messages-set (set messages))
  (reset! messages-map (->> messages
                            (map (fn [{:keys [message-id type] :as message}]
                                   [[message-id type] message]))
                            (into {}))))

(defn add!
  [{:keys [message-id type] :as message}]
  (swap! messages-set conj message)
  (swap! messages-map conj [[message-id type] message]))

(defn exists?
  [message-id type]
  (get @messages-map [message-id type]))
