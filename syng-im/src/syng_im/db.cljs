(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

;; initial state of app-db
(def app-db {:greeting             "Hello Clojure in iOS and Android!"
             :identity-password    "replace-me-with-user-entered-password"
             :contacts             []
             :chat                 {:current-chat-id "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
                                    :suggestions []
                                    :command nil}
             :chats                {}
             :chats-updated-signal 0
             :new-group            #{}})


(def protocol-initialized-path [:protocol-initialized])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])
(def input-suggestions-path [:chat :suggestions])
(def input-command-path [:chat :command])
(def updated-chats-signal-path [:chats-updated-signal])
(defn updated-chat-signal-path [chat-id]
  [:chats chat-id :chat-updated-signal])
(def new-group-path [:new-group])

