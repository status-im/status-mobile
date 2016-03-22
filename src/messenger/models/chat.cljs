(ns messenger.models.chat
  (:require [messenger.state :as state]))

(defn set-current-chat-id [chat-id]
  (swap! state/app-state assoc-in state/current-chat-id-path chat-id))

(defn current-chat-id []
  (get-in @state/app-state state/current-chat-id-path))
