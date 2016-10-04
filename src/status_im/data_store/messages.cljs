(ns status-im.data-store.messages
  (:require [status-im.data-store.realm.messages :as data-store]
            [clojure.string :refer [join split]]
            [status-im.utils.random :refer [timestamp]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [status-im.commands.utils :refer [generate-hiccup]]
            [cljs.reader :refer [read-string]]
            [status-im.constants :as c])
  (:refer-clojure :exclude [update]))

(defn- map-to-str
  [m]
  (join ";" (map #(join "=" %) (stringify-keys m))))

(defn- str-to-map
  [s]
  (keywordize-keys (apply hash-map (split s #"[;=]"))))

(defn- user-statuses-to-map
  [user-statuses]
  (->> (vals user-statuses)
       (mapv (fn [{:keys [whisper-identity] :as status}]
               [whisper-identity status]))
       (into {})))

(defn- command-type?
  [type]
  (contains?
    #{c/content-type-command c/content-type-command-request}
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
                  (-> message
                      (clojure.core/update :content str-to-map)
                      (assoc :rendered-preview
                             (when preview
                               (generate-hiccup (read-string preview)))))
                  message))))))

(defn get-last-message
  [chat-id]
  (data-store/get-last-message chat-id))

(defn get-unviewed
  []
  (data-store/get-unviewed))

(defn save
  ;; todo remove chat-id parameter
  [chat-id {:keys [message-id content]
            :as   message}]
  (when-not (data-store/exists? message-id)
    (let [content' (if (string? content)
                     content
                     (map-to-str content))
          message' (merge default-values
                          message
                          {:chat-id         chat-id
                           :content         content'
                           :timestamp       (timestamp)})]
      (data-store/save message'))))

(defn update
  [{:keys [message-id] :as message}]
  (when (data-store/exists? message-id)
    (let [message (clojure.core/update message :user-statuses vals)]
      (data-store/save message))))

(defn delete-by-chat-id [chat-id]
  (data-store/delete-by-chat-id chat-id))

