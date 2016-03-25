(ns syng-im.models.chat
  (:require [syng-im.db :as db]))

(defn set-current-chat-id [db chat-id]
  (assoc-in db db/current-chat-id-path chat-id))

(defn current-chat-id [db]
  (get-in db db/current-chat-id-path))

(defn set-latest-msg-id [db chat-id msg-id]
  (assoc-in db (db/latest-msg-id-path chat-id) msg-id))

(defn latest-msg-id [db chat-id]
  (->> (db/latest-msg-id-path chat-id)
       (get-in db)))