(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

;; initial state of app-db
(def app-db {:greeting          "Hello Clojure in iOS and Android!"
             :identity-password "replace-me-with-user-entered-password"
             :chat              {:current-chat-id "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"}
             :chats             {}})


(def protocol-initialized-path [:protocol-initialized])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])
(defn latest-msg-id-path [chat-id]
  [:chats chat-id :arrived-message-id])