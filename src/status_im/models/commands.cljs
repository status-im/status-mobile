(ns status-im.models.commands
  (:require [status-im.db :as db]
            [tailrecursion.priority-map :refer [priority-map-by]]))

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
  (assoc-in db (db/chat-command-content-path current-chat-id) content))

(defn set-command-parameter
  [{:keys [current-chat-id] :as db} name value]
  (assoc-in db [:chats current-chat-id :command-input :params name] value))

(defn get-chat-command
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-path current-chat-id)))

(defn get-command-input [{:keys [current-chat-id] :as db}]
  (get-in db [:chats current-chat-id :command-input]))

(defn add-params [command params]
  (let [command-params (:params command)
        command-params (vec (map (fn [param]
                                   (let [param-key (keyword (:name param))
                                         value (get params param-key)]
                                     (assoc param :value value))) command-params))]
    (assoc command :params command-params)))

(defn set-command-input
  ([db type command-key]
   (set-command-input db type nil command-key))
  ([db type message-id command-key]
   (set-command-input db type message-id command-key nil))
  ([{:keys [current-chat-id] :as db} type message-id command-key params]
   (let [command         (if (map? command-key)
                           command-key
                           (get-response-or-command type db command-key))
         command'        (add-params command params)
         first-parameter (get (:params command') 0)
         value           (:value first-parameter)]
     (update-in db [:chats current-chat-id :command-input] merge
                {:content       value
                 :command       command'
                 :parameter-idx 0
                 :params        params
                 :to-message-id message-id}))))

(defn get-command-parameter-index
  ([{:keys [current-chat-id] :as db}]
   (get-command-parameter-index db current-chat-id))
  ([db chat-id]
   (get-in db [:chats chat-id :command-input :parameter-idx])))

(defn next-command-parameter
  [{:keys [current-chat-id] :as db}]
  (let [parameter-index (get-command-parameter-index db)
        command (get-chat-command db)
        next-parameter (get (:params command) (inc parameter-index))
        value (:value next-parameter)]
    (-> db
        (update-in [:chats current-chat-id :command-input :parameter-idx] inc)
        (set-chat-command-content value))))


(defn get-chat-command-to-message-id
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-to-message-id-path current-chat-id)))

(defn get-chat-command-request
  [{:keys [current-chat-id] :as db}]
  (get-in db (db/chat-command-request-path current-chat-id
                                           (get-chat-command-to-message-id db))))

(defn parse-command-message-content [commands content]
  (if (map? content)
    (update content :command #((keyword %) commands))
    content))

(defn parse-command-request [commands content]
  (update content :command #((keyword %) commands)))
