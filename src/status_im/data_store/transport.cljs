(ns status-im.data-store.transport
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn deserialize-chat [serialized-chat]
  (dissoc serialized-chat :chat-id))

(re-frame/reg-cofx
 :data-store/transport
 (fn [cofx _]))

(defn save-transport-tx
  "Returns tx function for saving transport"
  [{:keys [chat-id chat]}]
  (fn [realm]
    (log/debug "saving transport, chat-id:" chat-id "chat" chat)))

(defn delete-transport-tx
  "Returns tx function for deleting transport"
  [chat-id]
  (fn [realm]
    (log/debug "deleting transport, chat-id:" chat-id)))
