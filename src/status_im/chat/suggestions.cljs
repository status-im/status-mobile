(ns status-im.chat.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.db :as db]
            [status-im.models.commands :refer [commands
                                               get-commands
                                               get-chat-command-request
                                               get-chat-command-to-msg-id
                                               clear-staged-commands]]
            [status-im.utils.utils :refer [log on-error http-get]]
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
    (let [to-msg-id (get-chat-command-to-msg-id db)]
      (command-handler to-msg-id command-key content)))
  db)

(defn get-command-handler [db command-key content]
  (when-let [command-handler (get-chat-command-request db)]
    (let [to-msg-id (get-chat-command-to-msg-id db)]
      (fn []
        (command-handler to-msg-id command-key content)))))

(defn apply-staged-commands [db]
  (let [staged-commands (get-in db (db/chat-staged-commands-path
                                     (:current-chat-id db)))]
    (dorun (map (fn [staged-command]
                  (when-let [handler (:handler staged-command)]
                    (handler)))
                staged-commands))
    (clear-staged-commands db)))

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

(defn switch-command-suggestions [db]
  (let [text (if (typing-command? db) nil "!")]
    (assoc-in db [:chats (:current-chat-id db) :input-text] text)))
