(ns status-im.db
  (:require [schema.core :as s :include-macros true]
            [status-im.components.react :refer [animated]]
            [status-im.components.animation :as anim]
            [status-im.constants :refer [console-chat-id]]))

;; schema of app-db
(def schema {:greeting s/Str})

(def default-view :chat-list)

;; initial state of app-db
(def app-db {:identity-password      "replace-me-with-user-entered-password"
             :identity               "me"
             :current-public-key     "me"

             :accounts               {}
             :current-account-id     nil

             :profile-edit           {:edit?      false
                                      :name       nil
                                      :email      nil
                                      :status     nil
                                      :photo-path nil}

             :new-contact-identity   ""
             :contacts               {}
             :discoveries            []
             :discovery-search-tags  []
             :tags                   {}

             :chats                  {}
             :chat                   {:command      nil
                                      :last-message nil}
             :current-chat-id        console-chat-id

             :contacts-ids           #{}
             :selected-contacts      #{}
             :chats-updated-signal   0
             :chat-ui-props          {:show-actions?     false
                                      :show-bottom-info? false}
             :selected-participants  #{}
             :view-id                default-view
             :navigation-stack       (list default-view)
             :current-tag            nil
             :qr-codes               {}
             :keyboard-height        0
             :animations             {;; todo clear this
                                      :tabs-bar-value (anim/create-value 0)}
             :loading-allowed        true})

(defn chat-staged-commands-path [chat-id]
  [:chats chat-id :staged-commands])
(defn chat-command-path [chat-id]
  [:chats chat-id :command-input :command])
(defn chat-command-to-message-id-path [chat-id]
  [:chats chat-id :command-input :to-message-id])
(defn chat-command-content-path [chat-id]
  [:chats chat-id :command-input :content])
(defn chat-command-requests-path [chat-id]
  [:chats chat-id :command-requests])
(defn chat-command-request-path [chat-id message-id]
  [:chats chat-id :command-requests message-id])
