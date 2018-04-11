(ns status-im.data-store.core
  (:require status-im.data-store.chats
            status-im.data-store.messages
            status-im.data-store.contacts
            status-im.data-store.transport
            status-im.data-store.browser
            status-im.data-store.accounts
            status-im.data-store.local-storage
            status-im.data-store.contact-groups
            status-im.data-store.requests
            [status-im.data-store.realm.core :as data-source]
            [status-im.utils.handlers :as handlers]))

(defn init [encryption-key]
  (when-not @data-source/base-realm
    (data-source/open-base-realm encryption-key))
  (data-source/reset-account-realm encryption-key))

(defn change-account [address new-account? encryption-key handler]
  (data-source/change-account address new-account? encryption-key handler))
