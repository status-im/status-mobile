(ns status-im.db
  (:require [schema.core :as s :include-macros true]
            [status-im.components.react :refer [animated]]
            [status-im.components.animation :as anim]))

;; schema of app-db
(def schema {:greeting s/Str})

(def default-view :chat-list)

;; initial state of app-db
(def app-db {:identity-password      "replace-me-with-user-entered-password"
             :identity               "me"
             :contacts               []
             :contacts-ids           #{}
             :selected-contacts      #{}
             :current-chat-id        "console"
             :chat                   {:command      nil
                                      :last-message nil}
             :chats                  {}
             :chats-updated-signal   0
             :show-actions           false
             :selected-participants  #{}
             :signed-up              true
             :view-id                default-view
             :navigation-stack       (list default-view)
             ;; TODO fix hardcoded values
             :photo-path             nil
             :username               "My Name"
             :phone-number           "3147984309"
             :email                  "myemail@gmail.com"
             :status                 "Hi, this is my status"
             :current-tag            nil
             :disable-group-creation false
             :animations             {;; mutable data
                                      :response-pan                 nil
                                      :response-pan-responder       nil
                                      :message-input-offset         (anim/create-value 0)
                                      :message-input-buttons-scale  (anim/create-value 1)
                                      :commands-input-is-switching? false
                                      :response-suggestions-height  (anim/create-value 0)
                                      :response-input-is-hiding?    false}})

(def protocol-initialized-path [:protocol-initialized])
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
