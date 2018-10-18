(ns status-im.data-store.mailservers
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/get-all-mailservers
 (fn [cofx _]
   (assoc cofx :data-store/mailservers (mapv #(-> %
                                                  (update :id keyword)
                                                  (update :fleet keyword))
                                             (-> @core/account-realm
                                                 (core/get-all :mailserver)
                                                 (core/all-clj :mailserver))))))

(defn save-tx
  "Returns tx function for saving a mailserver"
  [{:keys [id] :as mailserver}]
  (fn [realm]
    (core/create realm
                 :mailserver
                 mailserver
                 true)))

(defn delete-tx
  "Returns tx function for deleting a mailserver"
  [id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :mailserver :id id))))

(defn deserialize-mailserver-topic [serialized-mailserver-topic]
  (-> serialized-mailserver-topic
      (dissoc :topic)
      (update :chat-ids edn/read-string)))

(re-frame/reg-cofx
 :data-store/mailserver-topics
 (fn [cofx _]
   (assoc cofx
          :data-store/mailserver-topics
          (reduce (fn [acc {:keys [topic] :as mailserver-topic}]
                    (assoc acc topic (deserialize-mailserver-topic mailserver-topic)))
                  {}
                  (-> @core/account-realm
                      (core/get-all :mailserver-topic)
                      (core/all-clj :mailserver-topic))))))

(defn save-mailserver-topic-tx
  "Returns tx function for saving mailserver topic"
  [{:keys [topic mailserver-topic]}]
  (fn [realm]
    (core/create realm
                 :mailserver-topic
                 (-> mailserver-topic
                     (assoc :topic topic)
                     (update :chat-ids pr-str))
                 true)))

(defn delete-mailserver-topic-tx
  "Returns tx function for deleting mailserver topic"
  [topic]
  (fn [realm]
    (let [mailserver-topic (core/single
                            (core/get-by-field realm :mailserver-topic :topic topic))]
      (core/delete realm mailserver-topic))))
