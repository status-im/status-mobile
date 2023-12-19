(ns legacy.status-im.data-store.invitations
  (:require
    clojure.set))

(defn <-rpc
  [message]
  (-> message
      (clojure.set/rename-keys {:chatId              :chat-id
                                :introductionMessage :introduction-message
                                :messageType         :message-type})))
