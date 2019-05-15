(ns status-im.data-store.messages
  (:require [re-frame.core :as re-frame]
            [clojure.set :as clojure.set]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.data-store.realm.core :as core]
            [status-im.utils.core :as utils]
            [status-im.js-dependencies :as dependencies]))

(defn- transform-message [{:keys [content] :as message}]
  (when-let [parsed-content (utils/safe-read-message-content content)]
    (-> message
        (update :message-type keyword)
        (assoc :content parsed-content))))

(defn- exclude-messages [query message-ids]
  (let [string-queries (map #(str "message-id != \"" % "\"") message-ids)]
    (core/filtered query (string/join " AND " string-queries))))

(defn- get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id nil))
  ([chat-id {:keys [last-clock-value message-ids]}]
   (let [messages (cond-> (core/get-by-field @core/account-realm :message :chat-id chat-id)
                    :always (core/multi-field-sorted [["clock-value" true] ["message-id" true]])
                    last-clock-value    (core/filtered (str "clock-value <= \"" last-clock-value "\""))
                    (seq message-ids)   (exclude-messages message-ids)
                    :always (core/page 0  constants/default-number-of-messages)
                    :always (core/all-clj :message))
         clock-value (-> messages last :clock-value)
         new-message-ids (->> messages
                              (filter #(= clock-value (:clock-value %)))
                              (map :message-id)
                              (into #{}))]
     {:all-loaded? (> constants/default-number-of-messages (count messages))
      ;; We paginate using clock-value + message-id to break ties, we need
      ;; to exclude previously loaded messages with identical clock value
      ;; otherwise we might fetch exactly the same page if all the messages
      ;; in a page have the same clock-value. The initial idea was to use a
      ;; cursor clock-value-message-id but realm does not support </> operators
      ;; on strings
      :pagination-info {:last-clock-value clock-value
                        :message-ids (if (= clock-value last-clock-value)
                                       (clojure.set/union message-ids new-message-ids)
                                       new-message-ids)}
      :messages    (keep transform-message messages)})))

(defn get-message-id-by-old [old-message-id]
  (when-let
   [js-message (core/single
                (core/get-by-field
                 @core/account-realm
                 :message :old-message-id old-message-id))]
    (aget js-message "message-id")))

(defn- get-references-by-ids
  [message-ids]
  (when (seq message-ids)
    (keep (fn [{:keys [response-to response-to-v2]}]
            (when-let [js-message
                       (if response-to-v2
                         (.objectForPrimaryKey @core/account-realm "message" response-to-v2)
                         (core/single (core/get-by-field
                                       @core/account-realm
                                       :message :old-message-id response-to)))]
              (when-let [deserialized-message (-> js-message
                                                  (core/realm-obj->clj :message)
                                                  transform-message)]
                [(or response-to-v2 response-to) deserialized-message])))
          message-ids)))

(def default-values
  {:to nil})

(re-frame/reg-cofx
 :data-store/get-messages
 (fn [cofx _]
   (assoc cofx :get-stored-messages get-by-chat-id)))

(defn- sha3 [s]
  (.sha3 (.-prototype (dependencies/Web3)) s))

(re-frame/reg-cofx
 :data-store/get-referenced-messages
 (fn [cofx _]
   (assoc cofx :get-referenced-messages get-references-by-ids)))

(defn get-user-messages
  [public-key]
  (.reduce (core/get-by-field @core/account-realm
                              :message :from public-key)
           (fn [acc message-object _ _]
             (conj acc
                   {:message-id (aget message-object "message-id")
                    :chat-id (aget message-object "chat-id")}))
           []))

(re-frame/reg-cofx
 :data-store/get-user-messages
 (fn [cofx _]
   (assoc cofx :get-user-messages get-user-messages)))

(defn prepare-content [content]
  (if (string? content)
    content
    (pr-str content)))

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

(defn delete-chat-messages-tx
  "Returns tx function for deleting messages with user statuses for given chat-id"
  [chat-id]
  (fn [realm]
    (core/delete realm (core/get-by-field realm :message :chat-id chat-id))
    (core/delete realm (core/get-by-field realm :user-status :chat-id chat-id))))

(defn message-exists? [message-id]
  (if @core/account-realm
    (not (nil? (.objectForPrimaryKey @core/account-realm "message" message-id)))
    false))
