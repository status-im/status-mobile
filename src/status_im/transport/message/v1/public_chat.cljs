(ns ^{:doc "Public chat API"}
    status-im.transport.message.v1.public-chat
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.message.core :as message]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]))

(defn- has-already-joined? [chat-id {:keys [db]}]
  (get-in db [:transport/chats chat-id]))

(defn join-public-chat
  "Function producing all protocol level effects necessary for joining public chat identified by chat-id"
  [chat-id {:keys [db] :as cofx}]
  (when-not (has-already-joined? chat-id cofx)
    (let [on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [::add-new-sym-key {:chat-id    chat-id
                                                              :sym-key    sym-key
                                                              :sym-key-id sym-key-id}]))]
      (handlers-macro/merge-fx cofx
                         {:shh/generate-sym-key-from-password {:web3       (:web3 db)
                                                               :password   chat-id
                                                               :on-success on-success}}
                         (protocol/init-chat chat-id)))))

(handlers/register-handler-fx
  ::add-new-sym-key
  (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id]}]]
    (let [{:keys [web3]} db
          topic          (transport.utils/get-topic chat-id)]
      {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
       :shh/add-filter {:web3       web3
                        :sym-key-id sym-key-id
                        :topic      topic
                        :chat-id    chat-id}
       :data-store.transport/save {:chat-id chat-id
                                   :chat    (-> (get-in db [:transport/chats chat-id])
                                                (assoc :sym-key-id sym-key-id)
                                                ;;TODO (yenda) remove once go implements persistence
                                                (assoc :sym-key sym-key))}
       :dispatch [:inbox/request-messages {:topics [topic]
                                           :from   0}]})))
