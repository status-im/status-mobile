(ns status-im.db
  (:require [status-im.components.react :refer [animated]]
            [status-im.components.animation :as anim]
            [status-im.constants :refer [console-chat-id]]
            [status-im.utils.platform :as p]))

;; initial state of app-db
(def app-db {:identity-password          "replace-me-with-user-entered-password"
             :identity                   "me"
             :current-public-key         "me"

             :accounts                   {}
             :current-account-id         nil

             :profile-edit               {:edit?      false
                                          :name       nil
                                          :email      nil
                                          :status     nil
                                          :photo-path nil}

             :new-contact-identity       ""
             :contacts                   {}
             :contact-groups             {}
             :discoveries                {}
             :discover-search-tags       []
             :tags                       {}

             :chats                      {}
             :chat                       {:command      nil
                                          :last-message nil}
             :current-chat-id            console-chat-id

             :contacts-ids               #{}
             :selected-contacts          #{}
             :chats-updated-signal       0
             :selected-participants      #{}
             :view-id                    nil
             :navigation-stack           '()
             :current-tag                nil
             :qr-codes                   {}
             :keyboard-height            0
             :loading-allowed            true

             :sync-state                 :done
             :sync-listening-started     nil
             :status-module-initialized? (or p/ios? js/goog.DEBUG)
             :edit-mode                  {}
             :network                    :testnet})

(defn chat-command-path [chat-id]
  [:chats chat-id :command-input :command])
(defn chat-command-to-message-id-path [chat-id]
  [:chats chat-id :command-input :to-message-id])
(defn chat-command-content-path [chat-id]
  [:chats chat-id :command-input :content])
(defn chat-command-request-path [chat-id message-id]
  [:chats chat-id :command-requests message-id])
