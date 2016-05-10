(ns syng-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
    ;todo handlers in subs?...
            [syng-im.handlers.suggestions :refer [get-suggestions]]
            [syng-im.models.commands :as commands]))

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
    (reaction (get-in @db db/show-actions-path))))

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

(register-sub :get-chat-input-text
  (fn [db _]
    (->> (db/chat-input-text-path (:current-chat-id @db))
         (get-in @db)
         (reaction))))

(register-sub :get-chat-staged-commands
  (fn [db _]
    (->> (db/chat-staged-commands-path (:current-chat-id @db))
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
