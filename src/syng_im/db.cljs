(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

(def default-view :chat-list)

;; initial state of app-db
(def app-db {:greeting             "Hello Clojure in iOS and Android!"
             :identity-password    "replace-me-with-user-entered-password"
             :identity             "me"
             :contacts             []
             :current-chat-id      "console"
             :chat                 {:command nil
                                    :last-message    nil}
             :chats                {}
             :chats-updated-signal 0
             :show-actions         false
             :new-group            #{}
             :new-participants     #{}
             :signed-up            false
             :view-id              default-view
             :navigation-stack     (list default-view)})


(def protocol-initialized-path [:protocol-initialized])
(def identity-password-path [:identity-password])
(def contact-identity-path [:contact-identity])
(def current-chat-id-path [:current-chat-id])
(def updated-chats-signal-path [:chats-updated-signal])
(defn updated-chat-signal-path [chat-id]
  [:chats chat-id :chat-updated-signal])
(defn chat-input-text-path [chat-id]
  [:chats chat-id :input-text])
(defn chat-staged-commands-path [chat-id]
  [:chats chat-id :staged-commands])
(defn chat-command-path [chat-id]
  [:chats chat-id :command-input :command])
(defn chat-command-to-msg-id-path [chat-id]
  [:chats chat-id :command-input :to-msg-id])
(defn chat-command-content-path [chat-id]
  [:chats chat-id :command-input :content])
(defn chat-command-requests-path [chat-id]
  [:chats chat-id :command-requests])
(defn chat-command-request-path [chat-id msg-id]
  [:chats chat-id :command-requests msg-id])
(def show-actions-path [:show-actions])
(def new-group-path [:new-group])
(def new-participants-path [:new-participants])
