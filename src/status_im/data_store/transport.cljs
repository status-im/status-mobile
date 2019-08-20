(ns status-im.data-store.transport
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(defn deserialize-chat [serialized-chat]
  (dissoc serialized-chat :chat-id))

(re-frame/reg-cofx
 :data-store/transport
 (fn [cofx _]
   (assoc cofx :data-store/transport {})))

(defn save-transport-tx
  "Returns tx function for saving transport"
  [{:keys [chat-id chat]}]
  (fn [realm]))

(defn delete-transport-tx
  "Returns tx function for deleting transport"
  [chat-id]
  (fn [realm]))
