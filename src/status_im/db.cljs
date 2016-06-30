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

             :accounts               {}
             :current-account-id     nil

             :profile-edit           {:edit?      false
                                      :name       nil
                                      :email      nil
                                      :status     nil
                                      :photo-path nil}

             :new-contact-identity   ""
             :contacts               {}

             :contacts-ids           #{}
             :selected-contacts      #{}
             :current-chat-id        "console"
             :chat                   {:command      nil
                                      :last-message nil}
             :chats                  {}
             :chats-updated-signal   0
             :show-actions           false
             :selected-participants  #{}
             :signed-up              false
             :view-id                default-view
             :navigation-stack       (list default-view)
             :current-tag            nil
             :qr-codes               {}
             :keyboard-height        0
             :disable-group-creation false
             :animations             {;; todo clear this
                                      :tabs-bar-value (anim/create-value 0)}
             :loading-allowed        true})

(def protocol-initialized-path [:protocol-initialized])
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
