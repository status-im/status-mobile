(ns status-im.data-store.core
  (:require status-im.data-store.chats
            status-im.data-store.messages
            status-im.data-store.pending-messages
            status-im.data-store.transport
            [status-im.data-store.realm.core :as data-source]
            [status-im.utils.handlers :as handlers]))


(defn init []
  (data-source/reset-account))

(defn change-account [address new-account? handler]
  (data-source/change-account address new-account? handler))
