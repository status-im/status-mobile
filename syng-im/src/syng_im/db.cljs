(ns syng-im.db
  (:require [schema.core :as s :include-macros true]))

;; schema of app-db
(def schema {:greeting s/Str})

;; initial state of app-db
(def app-db {:greeting "Hello Clojure in iOS and Android!"
             :identity-password "replace-me-with-user-entered-password"
             :contacts []})


(def protocol-initialized-path [:protocol-initialized])
(def simple-store-path [:simple-store])
(def identity-password-path [:identity-password])
(def current-chat-id-path [:chat :current-chat-id])
(defn arrived-message-path [chat-id]
  [:chat chat-id :arrived-message-id])
