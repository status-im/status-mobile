(ns status-im.models.messages
  (:require [status-im.persistence.realm.core :as r]
            [re-frame.core :refer [dispatch]]
            [cljs.reader :refer [read-string]]
            [status-im.utils.random :refer [timestamp]]
            [status-im.utils.logging :as log]
            [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [status-im.constants :as c]
            [status-im.commands.utils :refer [generate-hiccup]]))

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

(def default-values
  {:outgoing       false
   :to             nil
   :same-author    false
   :same-direction false
   :preview        nil})

(defn save-message
  ;; todo remove chat-id parameter
  [chat-id {:keys [message-id content]
            :as   message}]
  (when-not (r/exists? :account :message {:message-id message-id})
    (r/write :account
      (fn []
        (let [content' (if (string? content)
                         content
                         (map-to-str content))
              message' (merge default-values
                              message
                              {:chat-id         chat-id
                               :content         content'
                               :timestamp       (timestamp)})]
          (r/create :account :message message' true))))))

(defn command-type? [type]
  (contains?
    #{c/content-type-command c/content-type-command-request}
    type))

(defn get-messages
  ([chat-id] (get-messages chat-id 0))
  ([chat-id from]
    (->> (-> (r/get-by-field :account :message :chat-id chat-id)
             (r/sorted :timestamp :desc)
             (r/page from (+ from c/default-number-of-messages))
             (r/realm-collection->list))
         (mapv #(update % :user-statuses user-statuses-to-map))
         (into '())
         reverse
         (keep (fn [{:keys [content-type preview] :as message}]
                 (if (command-type? content-type)
                   (-> message
                       (update :content str-to-map)
                       (assoc :rendered-preview
                              (when preview
                                (generate-hiccup (read-string preview)))))
                   message))))))

(defn update-message! [{:keys [message-id] :as message}]
  (r/write :account
    (fn []
      (when (r/exists? :account :message {:message-id message-id})
        (let [message (update message :user-statuses vals)]
          (r/create :account :message message true))))))

(defn get-message [id]
  (some-> (r/get-one-by-field :account :message :message-id id)
          (update :user-statuses user-statuses-to-map)))

(defn get-last-message [chat-id]
  (-> (r/get-by-field :account :message :chat-id chat-id)
      (r/sorted :timestamp :desc)
      (r/single)
      (js->clj :keywordize-keys true)))
