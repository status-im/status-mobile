(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

;; initial state of app-db
(def app-db {:greeting             "Hello Clojure in iOS and Android!"
             :identity-password    "replace-me-with-user-entered-password"
             :contacts             []
             :chat                 {:current-chat-id "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
                                    :command         nil}
             :chats                {}
             :chats-updated-signal 0
             :name                 "My Name"
             :new-group            #{}
             :new-participants     #{}})


(def protocol-initialized-path [:protocol-initialized])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])
(def updated-chats-signal-path [:chats-updated-signal])
(defn updated-chat-signal-path [chat-id]
  [:chats chat-id :chat-updated-signal])
(defn chat-input-text-path [chat-id]
  [:chats chat-id :input-text])
(defn chat-command-path [chat-id]
  [:chats chat-id :command-input :command])
(defn chat-command-content-path [chat-id]
  [:chats chat-id :command-input :content])
(def new-group-path [:new-group])
(def new-participants-path [:new-participants])
(def updated-discoveries-signal-path [:discovery-updated-signal])
(defn updated-discovery-signal-path [whisper-id]
  [:discoveries whisper-id :discovery-updated-signal])
