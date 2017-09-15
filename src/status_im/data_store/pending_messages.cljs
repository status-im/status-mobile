(ns status-im.data-store.pending-messages
  (:require [status-im.data-store.realm.pending-messages :as data-store]
            [status-im.utils.hex :as i]))

(defn- get-id
  [message-id to]
  (let [to'  (i/normalize-hex to)
        to'' (when to' (subs to' 0 7))
        id'  (if to''
               (str message-id "-" (subs to'' 0 7))
               message-id)]
    id'))

(defn get-all
  []
  (data-store/get-all-as-list))

(defn get-by-chat-id
  [chat-id]
  (data-store/get-by-chat-id chat-id))

(defn get-by-message-id
  [message-id]
  (data-store/get-by-message-id message-id))

(defn save
  [{:keys [id to group-id message] :as pending-message}]
  (let [{:keys [sig symKeyID pubKey topic payload]} message
        id'      (get-id id to)
        chat-id  (or group-id to)
        message' (-> pending-message
                     (assoc :id id'
                            :sig sig
                            :sym-key-id symKeyID
                            :pub-key pubKey
                            :message-id id
                            :chat-id chat-id
                            :payload payload
                            :topic topic)
                     (dissoc :message))]
    (data-store/save message')))

(defn delete
  [pending-message]
  (data-store/delete pending-message))

(defn delete-all-by-chat-id
  [chat-id]
  (data-store/delete-all-by-chat-id chat-id))
