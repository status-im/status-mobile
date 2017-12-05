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
   :to             nil
   :preview        nil})

(defn exists? [message-id]
  (data-store/exists? message-id))

(defn get-by-id
  [message-id]
  (data-store/get-by-id message-id))

(defn get-message-content-by-id [message-id]
  (when-let [{:keys [content-type content] :as message} (get-by-id message-id)]
    (when (command-type? content-type)
      (reader/read-string content))))

(defn get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id 0))
  ([chat-id from]
   (->> (data-store/get-by-chat-id chat-id from constants/default-number-of-messages)
        reverse
        (keep (fn [{:keys [content-type preview] :as message}]
                (if (command-type? content-type)
                  (update message :content reader/read-string)
                  message))))))

(defn get-log-messages
  [chat-id]
  (->> (data-store/get-by-chat-id chat-id 0 100)
       (filter #(= (:content-type %) constants/content-type-log-message))
       (map #(select-keys % [:content :timestamp]))))

(defn get-last-outgoing
  [chat-id number-of-messages]
  (data-store/get-by-fields {:chat-id  chat-id
                             :outgoing true}
                            0
                            number-of-messages))

(defn get-last-clock-value
  [chat-id]
  (if-let [message (data-store/get-last-message chat-id)]
    (:clock-value message)
    0))

(defn get-unviewed
  []
  (data-store/get-unviewed))

(defn- prepare-content [content]
  (if (string? content)
    content
    (pr-str
     ;; TODO janherich: this is ugly and not systematic, define something like `:not-persisent`
     ;; option for command params instead 
     (update content :params dissoc :password :password-confirmation))))

(defn save
  ;; todo remove chat-id parameter
  [chat-id {:keys [message-id content] :as message}]
  (when-not (data-store/exists? message-id)
    (let [content' (prepare-content content)
          message' (merge default-values
                          message
                          {:chat-id   chat-id
                           :content   content'
                           :timestamp (random/timestamp)})]
      (data-store/save message'))))

(defn update-message
  [{:keys [message-id] :as message}]
  (when (data-store/exists? message-id)
    (let [message  (-> message
                       (utils/update-if-present :user-statuses vals)
                       (utils/update-if-present :content prepare-content))]
      (data-store/save message))))

(defn delete-by-chat-id [chat-id]
  (data-store/delete-by-chat-id chat-id))
