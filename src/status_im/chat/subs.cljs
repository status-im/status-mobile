(ns status-im.chat.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub dispatch subscribe path]]
            [status-im.models.commands :as commands]
            [status-im.constants :refer [response-suggesstion-resize-duration]]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]))

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
    (let [chat-id (subscribe [:get-current-chat-id])]
      (reaction (get-in @db [:command-suggestions @chat-id])))))

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

(register-sub :valid-plain-message?
  (fn [_ _]
    (let [input-message   (subscribe [:get-chat-input-text])
          staged-commands (subscribe [:get-chat-staged-commands])]
      (reaction
        (plain-message/message-valid? @staged-commands @input-message)))))

(register-sub :valid-command?
  (fn [_ [_ validator]]
    (let [input (subscribe [:get-chat-command-content])]
      (reaction (command/valid? @input validator)))))

(register-sub :get-chat-command
  (fn [db _]
    (reaction (commands/get-chat-command @db))))

(register-sub :get-chat-command-content
  (fn [db _]
    (reaction (commands/get-chat-command-content @db))))

(register-sub :get-chat-command-to-msg-id
  (fn [db _]
    (reaction (commands/get-chat-command-to-msg-id @db))))

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

(register-sub :get-content-suggestions
  (fn [db _]
    (reaction (get-in @db [:suggestions (:current-chat-id @db)]))))

(register-sub :command?
  (fn [db]
    (->> (get-in @db [:edit-mode (:current-chat-id @db)])
         (= :command)
         (reaction))))
