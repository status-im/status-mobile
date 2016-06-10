(ns status-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [status-im.db :as db]
    ;todo handlers in subs?...
            [status-im.chat.suggestions :refer
             [get-suggestions typing-command? get-content-suggestions]]
            [status-im.models.commands :as commands]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]))

(register-sub :chat-properties
  (fn [db [_ properties]]
    (->> properties
         (map (fn [k]
                [k (-> @db
                       (get-in [:chats (:current-chat-id @db) k])
                       (reaction))]))
         (into {}))))

(register-sub
  :show-actions
  (fn [db _]
    (reaction (:show-actions @db))))

(register-sub :chat
  (fn [db [_ k]]
    (-> @db
        (get-in [:chats (:current-chat-id @db) k])
        (reaction))))


(register-sub :get-chat-messages
  (fn [db _]
    (let [chat-id (:current-chat-id @db)]
      (reaction (get-in @db [:chats chat-id :messages])))))

(register-sub :get-current-chat-id
  (fn [db _]
    (reaction (:current-chat-id @db))))

(register-sub :get-suggestions
  (fn [db _]
    (let [input-text (->> (:current-chat-id @db)
                          db/chat-input-text-path
                          (get-in @db)
                          (reaction))]
      (reaction (get-suggestions @db @input-text)))))

(register-sub :get-commands
  (fn [db _]
    (reaction (commands/get-commands @db))))

(register-sub :get-responses
  (fn [db _]
    (let [current-chat (@db :current-chat-id)]
      (reaction (or (get-in @db [:chats current-chat :responses]) {})))))

(register-sub :get-commands-and-responses
  (fn [db _]
    (let [current-chat (@db :current-chat-id)]
      (reaction _ (or (->> (get-in @db [:chats current-chat])
                           ((juxt :commands :responses))
                           (apply merge)) {})))))

(register-sub :get-chat-input-text
  (fn [db _]
    (->> [:chats (:current-chat-id @db) :input-text]
         (get-in @db)
         (reaction))))

(register-sub :get-chat-staged-commands
  (fn [db _]
    (->> [:chats (:current-chat-id @db) :staged-commands]
         (get-in @db)
         (reaction))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (commands/get-chat-command @db))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (commands/get-chat-command-content @db))))

(register-sub :chat-command-request
  (fn [db _]
    (reaction (commands/get-chat-command-request @db))))

(register-sub :get-current-chat
  (fn [db _]
    (let [current-chat-id (:current-chat-id @db)]
      (reaction (get-in @db [:chats current-chat-id])))))

(register-sub :get-chat
  (fn [db [_ chat-id]]
    (reaction (get-in @db [:chats chat-id]))))

(register-sub :typing-command?
  (fn [db _]
    (reaction (typing-command? @db))))

(register-sub :get-content-suggestions
  (fn [db _]
    (let [command (reaction (commands/get-chat-command @db))
          text    (reaction (commands/get-chat-command-content @db))]
      (reaction (get-content-suggestions @db @command @text)))))
