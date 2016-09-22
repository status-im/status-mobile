(ns status-im.chat.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.db :as db]
            [status-im.models.commands :refer [get-commands
                                               get-chat-command-request
                                               get-chat-command-to-message-id]]
            [status-im.utils.utils :refer [log http-get]]
            [clojure.string :as s]))

(defn suggestion? [text]
  (= (get text 0) "!"))

(defn can-be-suggested? [text]
  (fn [{:keys [name]}]
    (.startsWith (str "!" name) text)))

(defn get-suggestions
  [{:keys [current-chat-id] :as db} text]
  (let [commands (get-in db [:chats current-chat-id :commands])]
    (if (suggestion? text)
      (filter (fn [[_ v]] ((can-be-suggested? text) v)) commands)
      [])))

(defn get-command [db text]
  (when (suggestion? text)
    ;; TODO change 'commands' to 'suggestions'
    (first (filter #(= (:text %) text) (get-commands db)))))

(defn handle-command [db command-key content]
  (when-let [command-handler (get-chat-command-request db)]
    (let [to-message-id (get-chat-command-to-message-id db)]
      (command-handler to-message-id command-key content)))
  db)

(defn get-command-handler [db command-key content]
  (when-let [command-handler (get-chat-command-request db)]
    (let [to-message-id (get-chat-command-to-message-id db)]
      (fn []
        (command-handler to-message-id command-key content)))))

(defn check-suggestion [db message]
  (when-let [suggestion-text (when (string? message)
                               (re-matches #"^![^\s]+\s" message))]
    (let [suggestion-text' (s/trim suggestion-text)]
      (->> (get-commands db)
           (filter #(= suggestion-text' (->> % second :name (str "!"))))
           first))))

(defn typing-command? [db]
  (-> db
      (get-in [:chats (:current-chat-id db) :input-text])
      suggestion?))
