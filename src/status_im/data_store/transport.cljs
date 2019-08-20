(ns status-im.data-store.transport
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn deserialize-chat [serialized-chat]
  (dissoc serialized-chat :chat-id))

(re-frame/reg-cofx
 :data-store/transport
 (fn [cofx _]
   {}))

(defn save-transport-tx
  "Returns tx function for saving transport"
  [{:keys [chat-id chat]}])

(defn delete-transport-tx
  "Returns tx function for deleting transport"
  [chat-id])
