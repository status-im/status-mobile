(ns status-im.data-store.transport
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(defn deserialize-chat [serialized-chat]
  (-> serialized-chat
      (dissoc :chat-id)
      (update :ack edn/read-string)
      (update :seen edn/read-string)
      (update :pending-ack edn/read-string)
      (update :pending-send edn/read-string)))

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
    (core/create realm
                 :transport
                 (-> chat
                     (assoc :chat-id chat-id)
                     (update :ack pr-str)
                     (update :seen pr-str)
                     (update :pending-ack pr-str)
                     (update :pending-send pr-str))
                 true)))

(defn delete-transport-tx
  "Returns tx function for deleting transport"
  [chat-id]
  (fn [realm]
    (let [transport (core/single
                     (core/get-by-field realm :transport :chat-id chat-id))]
      (core/delete realm transport))))

(defn deserialize-inbox-topic [serialized-inbox-topic]
  (-> serialized-inbox-topic
      (dissoc :topic)
      (update :chat-ids edn/read-string)))

(re-frame/reg-cofx
 :data-store/transport-inbox-topics
 (fn [cofx _]
   (assoc cofx
          :data-store/transport-inbox-topics
          (reduce (fn [acc {:keys [topic] :as inbox-topic}]
                    (assoc acc topic (deserialize-inbox-topic inbox-topic)))
                  {}
                  (-> @core/account-realm
                      (core/get-all :transport-inbox-topic)
                      (core/all-clj :transport-inbox-topic))))))

(defn save-transport-inbox-topic-tx
  "Returns tx function for saving transport inbox topic"
  [{:keys [topic inbox-topic]}]
  (fn [realm]
    (core/create realm
                 :transport-inbox-topic
                 (-> inbox-topic
                     (assoc :topic topic)
                     (update :chat-ids pr-str))
                 true)))

(defn delete-transport-inbox-topic-tx
  "Returns tx function for deleting transport inbox-topic"
  [topic]
  (fn [realm]
    (let [transport-inbox-topic (core/single
                                 (core/get-by-field realm :transport-inbox-topic :topic topic))]
      (core/delete realm transport-inbox-topic))))
