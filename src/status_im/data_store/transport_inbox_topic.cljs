(ns status-im.data-store.transport-inbox-topic
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/transport-inbox-topics
 (fn [cofx _]
   (assoc cofx
          :data-store/transport-inbox-topics
          (reduce (fn [acc {:keys [topic] :as inbox-topic}]
                    (assoc acc chat-id (dissoc inbox-topic :topic)))
                  {}
                  (-> @core/account-realm
                      (core/get-all :transport-inbox-topic)
                      (core/all-clj :transport-inbox-topic))))))

(defn save-transport-inbox-topic-tx
  "Returns tx function for saving transport inbox topic"
  [{:keys [topic inbox-topic]}]
  (fn [realm]
    (core/create realm
                 :transport
                 (assoc inbox-topic:topic topic)
                 true)))

(defn delete-transport-inbox-topic-tx
  "Returns tx function for deleting transport inbox-topic"
  [topic]
  (fn [realm]
    (let [transport-inbox-topic (core/single
                                 (core/get-by-field realm :transport-inbox-topic :topic topic))]
      (core/delete realm transport-inbox-topic))))
