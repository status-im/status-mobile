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
                 (core/get-by-field realm :mailserver :id (name id)))))

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

(defn save-chat-requests-range
  [chat-requests-range]
  (fn [realm]
    (core/create realm :chat-requests-range chat-requests-range true)))

(re-frame/reg-cofx
 :data-store/all-chat-requests-ranges
 (fn [cofx _]
   (assoc cofx
          :data-store/all-chat-requests-ranges
          (reduce (fn [acc {:keys [chat-id] :as range}]
                    (assoc acc chat-id range))
                  {}
                  (-> @core/account-realm
                      (core/get-all :chat-requests-range)
                      (core/all-clj :chat-requests-range))))))
(re-frame/reg-cofx
 :data-store/all-gaps
 (fn [cofx _]
   (assoc cofx
          :data-store/all-gaps
          (fn [chat-id]
            (reduce (fn [acc {:keys [id] :as gap}]
                      (assoc acc id gap))
                    {}
                    (-> @core/account-realm
                        (core/get-by-field :mailserver-requests-gap :chat-id chat-id)
                        (core/all-clj :mailserver-requests-gap)))))))

(defn save-mailserver-requests-gap
  [gap]
  (fn [realm]
    (core/create realm :mailserver-requests-gap gap true)))

(defn delete-mailserver-requests-gaps
  [ids]
  (fn [realm]
    (doseq [id ids]
      (core/delete
       realm
       (core/get-by-field realm :mailserver-requests-gap :id id)))))

(defn delete-all-gaps-by-chat
  [chat-id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :mailserver-requests-gap :chat-id chat-id))))

(defn delete-range
  [chat-id]
  (fn [realm]
    (core/delete realm
                 (core/get-by-field realm :chat-requests-range :chat-id chat-id))))
