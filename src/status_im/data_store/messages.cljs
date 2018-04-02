(ns status-im.data-store.messages
  (:require [cljs.reader :as reader]
            [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.messages :as data-store]
            [status-im.utils.random :as random]
            [status-im.utils.core :as utils]
            [status-im.utils.datetime :as datetime]))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-log-messages
  [chat-id]
  (->> (data-store/get-by-chat-id chat-id 0 100)
       (filter #(= (:content-type %) constants/content-type-log-message))
       (map #(select-keys % [:content :timestamp]))))

(defn- command-type?
  [type]
  (contains?
   #{constants/content-type-command constants/content-type-command-request}
   type))

(def default-values
  {:outgoing       false
   :to             nil})

(re-frame/reg-cofx
  :data-store/get-message
  (fn [cofx _]
    (assoc cofx :get-stored-message data-store/get-by-id)))

(defn get-by-chat-id
  ([chat-id]
   (get-by-chat-id chat-id 0))
  ([chat-id from]
   (->> (data-store/get-by-chat-id chat-id from constants/default-number-of-messages)
        (keep (fn [{:keys [content-type preview] :as message}]
                (if (command-type? content-type)
                  (update message :content reader/read-string)
                  message))))))

(re-frame/reg-cofx
  :data-store/get-messages
  (fn [cofx _]
    (assoc cofx :get-stored-messages get-by-chat-id)))

(re-frame/reg-cofx
  :data-store/message-ids
  (fn [cofx _]
    (assoc cofx :stored-message-ids (data-store/get-stored-message-ids))))

(re-frame/reg-cofx
  :data-store/unviewed-messages
  (fn [{:keys [db] :as cofx} _]
    (assoc cofx
           :stored-unviewed-messages
           (into {}
                 (map (fn [[chat-id user-statuses]]
                        [chat-id (into #{} (map :message-id) user-statuses)]))
                 (group-by :chat-id (data-store/get-unviewed (:current-public-key db)))))))

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
                                          {:status-id        (str message-id "-" whisper-identity)
                                           :whisper-identity whisper-identity
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
                                              :timestamp (datetime/timestamp)})))))

(re-frame/reg-fx
  :data-store/save-message
  (fn [message]
    (async/go (async/>! core/realm-queue #(save message)))))

(defn update-message
  [{:keys [message-id] :as message}]
  (when-let [{:keys [chat-id]} (data-store/get-by-id message-id)]
    (data-store/save (prepare-message (assoc message :chat-id chat-id)))))

(re-frame/reg-fx
  :data-store/update-message
  (fn [message]
    (async/go (async/>! core/realm-queue #(update-message message)))))

(re-frame/reg-fx
  :data-store/update-messages
  (fn [messages]
    (doseq [message messages]
      (async/go (async/>! core/realm-queue #(update-message message))))))

(re-frame/reg-fx
  :data-store/delete-messages
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(data-store/delete-by-chat-id chat-id)))))

(re-frame/reg-fx
  :data-store/hide-messages
  (fn [chat-id]
    (async/go (async/>! core/realm-queue #(doseq [message-id (data-store/get-message-ids-by-chat-id chat-id)]
                                            (data-store/save {:message-id message-id
                                                              :show?      false}))))))
