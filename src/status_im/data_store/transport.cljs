(ns status-im.data-store.transport
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.realm.core :as core]))

(defn deserialize-chat [serialized-chat]
  (dissoc serialized-chat :chat-id))

(re-frame/reg-cofx
 :data-store/transport
 (fn [cofx _]
   (assoc cofx
          :data-store/transport
          (reduce (fn [acc {:keys [chat-id] :as chat}]
                    (assoc acc chat-id (deserialize-chat chat)))
                  {}
                  (-> @core/account-realm
                      (core/get-all :transport)
                      (core/all-clj :transport))))))

(defn save-transport-tx
  "Returns tx function for saving transport"
  [{:keys [chat-id chat]}]
  (fn [realm]
    (log/debug "saving transport, chat-id:" chat-id "chat" chat)
    (core/create realm
                 :transport
                 (assoc chat :chat-id chat-id)
                 true)))

(defn delete-transport-tx
  "Returns tx function for deleting transport"
  [chat-id]
  (fn [realm]
    (log/debug "deleting transport, chat-id:" chat-id)
    (let [transport (.objectForPrimaryKey realm "transport" chat-id)]
      (core/delete realm transport))))
