(ns syng-im.handlers.suggestions
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.db :as db]
            [syng-im.models.chat :refer [current-chat-id]]
            [syng-im.models.commands :refer [commands
                                             suggestions
                                             get-commands
                                             get-chat-command-request
                                             get-chat-command-to-msg-id
                                             clear-staged-commands]]
            [syng-im.utils.utils :refer [log on-error http-get]]
            [syng-im.utils.logging :as log]
            [clojure.string :as s]))

(defn suggestion? [text]
  (= (get text 0) "!"))

(defn get-suggestions [db text]
  (if (suggestion? text)
    ;; TODO change 'commands' to 'suggestions'
    (filterv #(.startsWith (:text %) text) (get-commands db))
    []))

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
  (let [staged-commands (get-in db (db/chat-staged-commands-path (current-chat-id db)))]
    (dorun (map (fn [staged-command]
                  (when-let [handler (:handler staged-command)]
                    (handler)))
                staged-commands))
    (clear-staged-commands db)))

(defn execute-commands-js [body]
  (.eval js/window body)
  (let [commands (.-commands js/window)]
    (dispatch [:set-commands (map #(update % :command keyword)
                                  (js->clj commands :keywordize-keys true))])))

(defn load-commands []
  (http-get "chat-commands.js" execute-commands-js nil))

(defn check-suggestion [db message]
  (when-let [suggestion-text (when (string? message)
                               (re-matches #"^![^\s]+\s" message))]
    (let [suggestion-text' (s/trim suggestion-text)
          [suggestion] (filter #(= suggestion-text' (:text %))
                               (get-commands db))]
      suggestion)))
