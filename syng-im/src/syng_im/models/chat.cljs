(ns syng-im.models.chat
  (:require [syng-im.db :as db]))

(defn set-current-chat-id [db chat-id]
  (assoc-in db db/current-chat-id-path chat-id))

(defn current-chat-id [db]
  (get-in db db/current-chat-id-path))
