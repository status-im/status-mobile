(ns syng-im.handlers.commands
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.commands :refer [get-command]]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defn set-chat-command-content [db content]
  (assoc-in db (db/chat-command-content-path (get-in db db/current-chat-id-path))
            content))

(defn set-chat-command [db command-key]
  (-> db
      (set-chat-command-content nil)
      (assoc-in (db/chat-command-path (get-in db db/current-chat-id-path))
             (get-command command-key))))
