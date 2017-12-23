(ns status-im.data-store.messages
  (:refer-clojure :exclude [exists?])
  (:require [cljs.reader :as reader]
            [status-im.constants :as constants]
            [status-im.data-store.realm.messages :as data-store]
            [status-im.utils.random :as random]
            [status-im.utils.utils :as utils]))

(defn- command-type?
  [type]
  (contains?
   #{constants/content-type-command constants/content-type-command-request}
   type))

(def default-values
  {:outgoing       false
   :to             nil})

(defn exists? [message-id]
  (data-store/exists? message-id))

(defn get-by-id
  [message-id]
  (data-store/get-by-id message-id))

(defn get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id 0))
  ([chat-id from]
   (->> (data-store/get-by-chat-id chat-id from constants/default-number-of-messages)
        (keep (fn [{:keys [content-type preview] :as message}]
                (if (command-type? content-type)
                  (update message :content reader/read-string)
                  message))))))

(defn get-log-messages
  [chat-id]
  (->> (data-store/get-by-chat-id chat-id 0 100)
       (filter #(= (:content-type %) constants/content-type-log-message))
       (map #(select-keys % [:content :timestamp]))))

(defn get-last-clock-value
  [chat-id]
  (if-let [message (data-store/get-last-message chat-id)]
    (:clock-value message)
    0))

(defn get-unviewed
  [current-public-key]
  (into {}
        (map (fn [[chat-id user-statuses]]
               [chat-id (into #{} (map :message-id) user-statuses)]))
        (group-by :chat-id (data-store/get-unviewed current-public-key))))

(defn- prepare-content [content]
  (if (string? content)
    content
    (pr-str
     ;; TODO janherich: this is ugly and not systematic, define something like `:not-persisent`
     ;; option for command params instead
     (update content :params dissoc :password :password-confirmation))))

(defn- prepare-statuses [{:keys [chat-id message-id] :as message}]
  (utils/update-if-present message
                           :user-statuses
                           (partial map (fn [[whisper-identity status]]
                                          {:whisper-identity whisper-identity
                                           :status           status
                                           :chat-id          chat-id
                                           :message-id       message-id}))))

(defn- prepare-message [message]
  (-> message
      prepare-statuses
      (utils/update-if-present :content prepare-content)))

(defn save 
  [{:keys [message-id content from] :as message}]
  (when-not (data-store/exists? message-id)
    (data-store/save (prepare-message (merge default-values
                                             message
                                             {:from      (or from "anonymous")
                                              :timestamp (random/timestamp)})))))

(defn update-message
  [{:keys [message-id] :as message}]
  (when-let [{:keys [chat-id]} (data-store/get-by-id message-id)]
    (data-store/save (prepare-message (assoc message :chat-id chat-id)))))

(defn delete-by-chat-id [chat-id]
  (data-store/delete-by-chat-id chat-id))
