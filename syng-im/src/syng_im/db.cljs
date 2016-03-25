(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

;; initial state of app-db
(def app-db {:greeting          "Hello Clojure in iOS and Android!"
             :identity-password "replace-me-with-user-entered-password"
             :chat              {:current-chat-id "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154"}
             :chats             {}})


(def protocol-initialized-path [:protocol-initialized])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])
(defn latest-msg-id-path [chat-id]
  [:chats chat-id :arrived-message-id])