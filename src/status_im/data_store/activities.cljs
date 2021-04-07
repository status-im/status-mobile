(ns status-im.data-store.activities
  (:require [status-im.data-store.messages :as messages]
            [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            clojure.set))

(defn rpc->type [{:keys [type name] :as chat}]
  (cond
    (= 2 type) (assoc chat
                      :chat-type constants/private-group-chat-type
                      :chat-name name
                      :public? false
                      :group-chat true)
    (= 1 type) (assoc chat
                      :chat-type constants/one-to-one-chat-type
                      :chat-name name
                      :public? false
                      :group-chat false)))

(defn <-rpc [item]
  (-> item
      rpc->type
      (clojure.set/rename-keys {:lastMessage :last-message
                                :chatId      :chat-id})
      (assoc :color (rand-nth colors/chat-colors))
      (update :last-message #(when % (messages/<-rpc %)))
      (dissoc :chatId)))
