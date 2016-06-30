(ns status-im.models.commands
  (:require [status-im.db :as db]
            [tailrecursion.priority-map :refer [priority-map-by]]))

(defn get-commands [{:keys [current-chat-id] :as db}]
  (or (get-in db [:chats current-chat-id :commands]) {}))

(defn get-response-or-command
  [type {:keys [current-chat-id] :as db} command-key]
  ((or (get-in db [:chats current-chat-id type]) {}) command-key))

(defn find-command [commands command-key]
  (first (filter #(= command-key (:command %)) commands)))

(defn get-chat-command-content
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-content-path current-chat-id)))

(defn set-chat-command-content
  [{:keys [current-chat-id] :as db} content]
  (assoc-in db [:chats current-chat-id :command-input :content] content))

(defn get-chat-command
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-path current-chat-id)))

(defn set-command-input
  ([db type command-key]
    (set-command-input db type nil command-key))
  ([{:keys [current-chat-id] :as db} type msg-id command-key]
   (update-in db [:chats current-chat-id :command-input] merge
              {:content       nil
               :command       (get-response-or-command type db command-key)
               :parameter-idx 0
               :to-msg-id     msg-id})))

(defn get-chat-command-to-msg-id
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-to-msg-id-path current-chat-id)))

(defn compare-commands
  [{created-at-1 :created-at} {created-at-2 :created-at}]
  (compare created-at-1 created-at-2))

(defn stage-command
  [{:keys [current-chat-id] :as db} {:keys [id] :as command-info}]
  (let [path (db/chat-staged-commands-path current-chat-id)
        staged-commands (get-in db path)
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
                                           (get-chat-command-to-msg-id db))))

(defn set-chat-command-request
  [{:keys [current-chat-id] :as db} msg-id handler]
  (update-in db (db/chat-command-requests-path current-chat-id)
             #(assoc % msg-id handler)))

(defn parse-command-msg-content [commands content]
  (update content :command #((keyword %) commands)))

(defn parse-command-request [commands content]
  (update content :command #((keyword %) commands)))
