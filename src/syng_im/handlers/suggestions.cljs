(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id]]
            [syng-im.models.commands :refer [commands
                                             suggestions
                                             get-chat-command-request
                                             get-chat-command-to-msg-id]]
            [syng-im.utils.utils :refer [log on-error http-post]]
            [syng-im.utils.logging :as log]))

(defn get-suggestions [text]
  (if (= (get text 0) "!")
    ;; TODO change 'commands' to 'suggestions'
    (filterv #(.startsWith (:text %) text) commands)
    []))

(defn get-command [text]
  (when (= (get text 0) "!")
    ;; TODO change 'commands' to 'suggestions'
    (first (filter #(= (:text %) text) commands))))

(defn handle-command [db command-key content]
  (when-let [command-handler (get-chat-command-request db)]
   (let [to-msg-id (get-chat-command-to-msg-id db)]
     (command-handler to-msg-id command-key content)))
  db)
