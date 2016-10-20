(ns status-im.models.commands
  (:require [status-im.db :as db]
            [tailrecursion.priority-map :refer [priority-map-by]]
            [taoensso.timbre :as log]))

(defn get-commands [{:keys [current-chat-id] :as db}]
  (or (get-in db [:chats current-chat-id :commands]) {}))

(defn get-response-or-command
  [type {:keys [current-chat-id] :as db} command-key]
  ((or (get-in db [:chats current-chat-id type]) {}) command-key))

(defn get-chat-command-content
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-content-path current-chat-id)))

(defn set-chat-command-content
  [{:keys [current-chat-id] :as db} content]
  (assoc-in db [:chats current-chat-id :command-input :content] content))

(defn set-command-parameter
  [{:keys [current-chat-id] :as db} name value]
  (assoc-in db [:chats current-chat-id :command-input :params name] value))

(defn get-chat-command
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-path current-chat-id)))

(defn get-command-input [{:keys [current-chat-id] :as db}]
  (get-in db [:chats current-chat-id :command-input]))

(defn set-command-input
  ([db type command-key]
   (set-command-input db type nil command-key))
  ([{:keys [current-chat-id] :as db} type message-id command-key]
   (update-in db [:chats current-chat-id :command-input] merge
              {:content       nil
               :command       (get-response-or-command type db command-key)
               :parameter-idx 0
               :params        nil
               :to-message-id message-id})))

(defn get-chat-command-to-message-id
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-to-message-id-path current-chat-id)))

(defn compare-commands
  [{created-at-1 :created-at} {created-at-2 :created-at}]
  (compare created-at-1 created-at-2))

(defn stage-command
  [{:keys [current-chat-id] :as db} {:keys [id] :as command-info}]
  (let [path             (db/chat-staged-commands-path current-chat-id)
        staged-commands  (get-in db path)
        staged-coomands' (if (seq staged-commands)
                           staged-commands
                           (priority-map-by compare-commands))]
    (assoc-in db path (assoc staged-coomands' id command-info))))

(defn unstage-command [db {:keys [id]}]
  (update-in db (db/chat-staged-commands-path (:current-chat-id db))
             dissoc id))

(defn get-chat-command-request
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-request-path current-chat-id
                                           (get-chat-command-to-message-id db))))

(defn parse-command-message-content [commands content]
  (update content :command #((keyword %) commands)))

(defn parse-command-request [commands content]
  (log/debug "parse-command-request: " commands content)
  (update content :command #((keyword %) commands)))
