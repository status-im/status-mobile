(ns status-im.db
  (:require [status-im.constants :refer [console-chat-id]]
            [status-im.utils.platform :as p]))

;; initial state of app-db
(def app-db {:current-public-key         ""
             :status-module-initialized? (or p/ios? js/goog.DEBUG)
             :keyboard-height            0
             :accounts                   {}
             :navigation-stack           '()
             :contacts/contacts          {}
             :qr-codes                   {}
             :contact-groups             {}
             :selected-contacts          #{}
             :chats                      {}
             :current-chat-id            console-chat-id
             :loading-allowed            true
             :selected-participants      #{}
             :profile-edit               {:edit?      false
                                          :name       nil
                                          :email      nil
                                          :status     nil
                                          :photo-path nil}
             :discoveries                {}
             :discover-search-tags       '()
             :tags                       []
             :sync-state                 :done
             :network                    :testnet})
