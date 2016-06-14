(ns status-im.models.commands
  (:require [clojure.string :refer [join split]]
            [clojure.walk :refer [stringify-keys keywordize-keys]]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im.db :as db]
            [status-im.components.styles :refer [color-blue color-dark-mint]]
            [status-im.i18n :refer [label]]))

(defn get-commands [{:keys [current-chat-id] :as db}]
  (or (get-in db [:chats current-chat-id :commands]) {}))

(defn get-command [{:keys [current-chat-id] :as db} command-key]
  ((or (->> (get-in db [:chats current-chat-id])
            ((juxt :commands :responses))
            (apply merge))
       {}) command-key))

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

(defn set-response-chat-command
  [{:keys [current-chat-id] :as db} msg-id command-key]
  (update-in db [:chats current-chat-id :command-input] merge
             {:content   nil
              :command   (merge (get-command db command-key))
              :to-msg-id msg-id}))

(defn set-chat-command [db command-key]
  (set-response-chat-command db nil command-key))

(defn get-chat-command-to-msg-id
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-to-msg-id-path current-chat-id)))

(defn stage-command
  [{:keys [current-chat-id] :as db} command-info]
  (update-in db (db/chat-staged-commands-path current-chat-id)
             #(if %
               (conj % command-info)
               [command-info])))

(defn unstage-command [db staged-command]
  (update-in db (db/chat-staged-commands-path (:current-chat-id db))
             (fn [staged-commands]
               (filterv #(not= % staged-command) staged-commands))))

(defn clear-staged-commands
  [{:keys [current-chat-id] :as db}]
  (assoc-in db (db/chat-staged-commands-path current-chat-id) []))

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
