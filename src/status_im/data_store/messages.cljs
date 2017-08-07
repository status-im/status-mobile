(ns status-im.data-store.messages
  (:require [status-im.data-store.realm.messages :as data-store]
            [clojure.string :refer [join split]]
            [status-im.utils.random :refer [timestamp]]
            [status-im.utils.utils :refer [update-if-present]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [cljs.reader :refer [read-string]]
            [status-im.constants :as c])
  (:refer-clojure :exclude [update]))

(defn- user-statuses-to-map
  [user-statuses]
  (->> (vals user-statuses)
       (mapv (fn [{:keys [whisper-identity] :as status}]
               [whisper-identity status]))
       (into {})))

(defn- command-type?
  [type]
  (contains?
   #{c/content-type-command c/content-type-command-request
     c/content-type-wallet-request c/content-type-wallet-command}
   type))

(def default-values
  {:outgoing       false
   :to             nil
   :same-author    false
   :same-direction false
   :preview        nil})

(defn get-by-id
  [message-id]
  (some-> (data-store/get-by-id message-id)
          (clojure.core/update :user-statuses user-statuses-to-map)))

(defn get-message-content-by-id [message-id]
  (when-let [{:keys [content-type content] :as message} (get-by-id message-id)]
    (when (command-type? content-type)
      (read-string content))))

(defn get-messages
  [messages]
  (->> messages
       (mapv #(clojure.core/update % :user-statuses user-statuses-to-map))
       (into '())
       reverse
       (keep (fn [{:keys [content-type] :as message}]
               (if (command-type? content-type)
                 (clojure.core/update message :content read-string)
                 message)))))

(defn get-count-by-chat-id
  [chat-id]
  (data-store/get-count-by-chat-id chat-id))

(defn get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id 0))
  ([chat-id from]
   (->> (data-store/get-by-chat-id chat-id from c/default-number-of-messages)
        (mapv #(clojure.core/update % :user-statuses user-statuses-to-map))
        (into '())
        reverse
        (keep (fn [{:keys [content-type preview] :as message}]
                (if (command-type? content-type)
                  (clojure.core/update message :content read-string)
                  message))))))

(defn get-log-messages
  [chat-id]
  (->> (data-store/get-by-chat-id chat-id 0 100)
       (filter #(= (:content-type %) c/content-type-log-message))
       (map #(select-keys % [:content :timestamp]))))

(defn get-last-message
  [chat-id]
  (if-let [{:keys [content-type] :as message} (data-store/get-last-message chat-id)]
    (if (command-type? content-type)
      (clojure.core/update message :content read-string)
      message)))

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

(defn get-previews
  []
  (->> (data-store/get-all-as-list)
       (filter :preview)
       (reduce (fn [acc {:keys [message-id preview]}]
                 (assoc acc message-id (read-string preview)))
               {})))

(defn save
  ;; todo remove chat-id parameter
  [chat-id {:keys [message-id content] :as message}]
  (when-not (data-store/exists? message-id)
    (let [content' (if (string? content)
                     content
                     (pr-str content))
          message' (merge default-values
                          message
                          {:chat-id   chat-id
                           :content   content'
                           :timestamp (timestamp)})]
      (data-store/save message'))))

(defn update
  [{:keys [message-id] :as message}]
  (when (data-store/exists? message-id)
    (let [message (update-if-present message :user-statuses vals)]
      (data-store/save message))))

(defn delete-by-chat-id [chat-id]
  (data-store/delete-by-chat-id chat-id))
