(ns status-im.chat.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.chat.constants :as chat-consts]
            [status-im.models.commands :refer [get-commands
                                               get-chat-command-request
                                               get-chat-command-to-message-id]]
            [status-im.utils.utils :refer [log http-get]]
            [clojure.string :as s]))

(defn suggestion? [text]
  (= (get text 0) chat-consts/command-char))

(defn bot-suggestion? [text]
  (= (get text 0) chat-consts/bot-char))

(defn can-be-suggested? [first-char text]
  (fn [{:keys [name bot]}]
    (s/starts-with? (s/lower-case (str first-char (or bot name))) text)))

(defn get-suggestions
  [{:keys [current-chat-id global-commands] :as db} text]
  (let [commands (get-in db [:chats current-chat-id :commands])]
    (cond
      (suggestion? text)
      (filter (fn [[_ v]]
                ((can-be-suggested? chat-consts/command-char text) v))
              (merge commands global-commands))

      (bot-suggestion? text)
      (filter (fn [[_ v]]
                ((can-be-suggested? chat-consts/bot-char text) v))
              global-commands)

      :else [])))

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
                               (re-matches (re-pattern (str "^" chat-consts/command-char "[^\\s]+\\s")) message))]
    (let [suggestion-text' (s/trim suggestion-text)]
      (->> (get-commands db)
           (filter #(= suggestion-text' (->> % second :name (str chat-consts/command-char))))
           first))))

(defn typing-command? [db]
  (-> db
      (get-in [:chats (:current-chat-id db) :input-text])
      suggestion?))
