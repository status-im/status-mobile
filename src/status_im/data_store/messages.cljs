(ns status-im.data-store.messages
  (:require [cljs.reader :as reader]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.realm.core :as core]
            [status-im.utils.core :as utils]))

(defn- command-type?
  [type]
  (contains?
   #{constants/content-type-command constants/content-type-command-request}
   type))

(defn- transform-message [{:keys [content-type] :as message}]
  (cond-> (update message :message-type keyword)
    (command-type? content-type)
    (update :content reader/read-string)))

(defn- get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id 0))
  ([chat-id from]
   (let [messages (-> (core/get-by-field @core/account-realm :message :chat-id chat-id)
                      (core/sorted :timestamp :desc)
                      (core/page from (+ from constants/default-number-of-messages))
                      (core/all-clj :message))]
     (map transform-message messages))))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-log-messages
  [chat-id]
  (->> (get-by-chat-id chat-id 0)
       (filter #(= (:content-type %) constants/content-type-log-message))
       (map #(select-keys % [:content :timestamp]))))

(def default-values
  {:to             nil})

(re-frame/reg-cofx
 :data-store/get-messages
 (fn [cofx _]
   (assoc cofx :get-stored-messages get-by-chat-id)))

(re-frame/reg-cofx
 :data-store/message-ids
 (fn [cofx _]
   (assoc cofx :stored-message-ids (let [chat-id->message-id (volatile! {})]
                                     (-> @core/account-realm
                                         (.objects "message")
                                         (.map (fn [msg _ _]
                                                 (vswap! chat-id->message-id
                                                         #(update %
                                                                  (aget msg "chat-id")
                                                                  (fnil conj #{})
                                                                  (aget msg "message-id"))))))
                                     @chat-id->message-id))))

(defn- get-unviewed-messages
  [public-key]
  (into {}
        (map (fn [[chat-id user-statuses]]
               [chat-id (into #{} (map :message-id) user-statuses)]))
        (group-by :chat-id
                  (-> @core/account-realm
                      (core/get-by-fields
                       :user-status
                       :and {:whisper-identity public-key
                             :status           "received"})
                      (core/all-clj :user-status)))))

(re-frame/reg-cofx
 :data-store/get-unviewed-messages
 (fn [cofx _]
   (assoc cofx
          :get-stored-unviewed-messages
          get-unviewed-messages)))

(defn- prepare-content [content]
  (if (string? content)
    content
    (pr-str
     ;; TODO janherich: this is ugly and not systematic, define something like `:not-persisent`
     ;; option for command params instead
     (update content :params dissoc :password :password-confirmation))))

(defn- prepare-message [message]
  (utils/update-if-present message :content prepare-content))

(defn save-message-tx
  "Returns tx function for saving message"
  [{:keys [message-id from] :as message}]
  (fn [realm]
    (core/create realm
                 :message
                 (prepare-message (merge default-values
                                         message
                                         {:from (or from "anonymous")}))
                 true)))

(defn delete-message-tx
  "Returns tx function for deleting message"
  [message-id]
  (fn [realm]
    (when-let [message (core/get-by-field realm :message :message-id message-id)]
      (core/delete realm message)
      (core/delete realm (core/get-by-field realm :user-status :message-id message-id)))))

(defn delete-messages-tx
  "Returns tx function for deleting messages with user statuses for given chat-id"
  [chat-id]
  (fn [realm]
    (core/delete realm (core/get-by-field realm :message :chat-id chat-id))
    (core/delete realm (core/get-by-field realm :user-status :chat-id chat-id))))

(defn hide-messages-tx
  "Returns tx function for hiding messages for given chat-id"
  [chat-id]
  (fn [realm]
    (.map (core/get-by-field realm :message :chat-id chat-id)
          (fn [msg _ _]
            (aset msg "show?" false)))))
