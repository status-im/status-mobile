(ns status-im.data-store.messages
  (:require [clojure.set :as clojure.set]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.core :as utils]))

(defn get-message-by-id
  [message-id realm]
  (.objectForPrimaryKey realm "message" message-id))

(defn- transform-message
  [{:keys [content outgoing-status] :as message}]
  (when-let [parsed-content (utils/safe-read-message-content content)]
    (let [outgoing-status (when-not (empty? outgoing-status)
                            (keyword outgoing-status))]
      (-> message
          (update :message-type keyword)
          (assoc :content parsed-content
                 :outgoing-status outgoing-status
                 :outgoing outgoing-status)))))

(defn- exclude-messages [query message-ids]
  (let [string-queries (map #(str "message-id != \"" % "\"") message-ids)]))

(defn- get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id nil))
  ([chat-id {:keys [last-clock-value message-ids]}]))

(defn get-message-id-by-old [old-message-id])

(defn- get-references-by-ids
  [message-ids])

(def default-values
  {:to nil})

(re-frame/reg-cofx
 :data-store/get-messages
 (fn [cofx _]
   (assoc cofx :get-stored-messages get-by-chat-id)))

(re-frame/reg-cofx
 :data-store/get-referenced-messages
 (fn [cofx _]
   (assoc cofx :get-referenced-messages get-references-by-ids)))

(defn get-user-messages
  [public-key])

(re-frame/reg-cofx
 :data-store/get-user-messages
 (fn [cofx _]
   (assoc cofx :get-user-messages get-user-messages)))

(defn get-unviewed-message-ids
  [])

(re-frame/reg-cofx
 :data-store/get-unviewed-message-ids
 (fn [cofx _]
   (assoc cofx :get-unviewed-message-ids get-unviewed-message-ids)))

(defn prepare-content [content]
  (if (string? content)
    content
    (pr-str content)))

(defn- prepare-message [message]
  (utils/update-if-present message :content prepare-content))

(defn save-message-tx
  "Returns tx function for saving message"
  [{:keys [message-id from] :as message}]
  (fn [realm]))

(defn delete-message-tx
  "Returns tx function for deleting message"
  [message-id]
  (fn [realm]))

(defn delete-chat-messages-tx
  "Returns tx function for deleting messages with user statuses for given chat-id"
  [chat-id]
  (fn [realm]))

(defn message-exists? [message-id])

(defn mark-messages-seen-tx
  "Returns tx function for marking messages as seen"
  [message-ids]
  (fn [realm]
    (doseq [message-id message-ids]
      (let [message (get-message-by-id message-id realm)]
        (aset message "seen" true)))))

(defn mark-message-seen-tx
  "Returns tx function for marking messages as seen"
  [message-id]
  (fn [realm]
    (let [message (get-message-by-id message-id realm)]
      (aset message "seen" true))))

(defn update-outgoing-status-tx
  "Returns tx function for marking messages as seen"
  [message-id outgoing-status]
  (fn [realm]
    (let [message (get-message-by-id message-id realm)]
      (aset message "outgoing-status" (name outgoing-status)))))
