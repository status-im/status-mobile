(ns ^{:doc "Public chat API"}
 status-im.transport.message.public-chat
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.transport :as transport-store]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]))

(defn- has-already-joined? [{:keys [db]} chat-id]
  (get-in db [:transport/chats chat-id]))

(fx/defn join-group-chat
  "Function producing all protocol level effects necessary for a group chat identified by chat-id"
  [{:keys [db] :as cofx} chat-id]
  (when-not (has-already-joined? cofx chat-id)
    (let [public-key (get-in db [:account/account :public-key])
          topic (transport.utils/get-topic chat-id)]
      (fx/merge cofx
                {:shh/add-discovery-filters {:web3 (:web3 db)
                                             :private-key-id public-key
                                             :topics [{:topic topic
                                                       :chat-id chat-id}]}}
                (protocol/init-chat {:chat-id chat-id
                                     :topic   topic})
                #(hash-map :data-store/tx  [(transport-store/save-transport-tx
                                             {:chat-id chat-id
                                              :chat    (get-in % [:db :transport/chats chat-id])})])))))

(fx/defn join-public-chat
  "Function producing all protocol level effects necessary for joining public chat identified by chat-id"
  [{:keys [db] :as cofx} chat-id]
  (when-not (has-already-joined? cofx chat-id)
    (let [on-success (fn [sym-key sym-key-id]
                       (re-frame/dispatch [::add-new-sym-key {:chat-id    chat-id
                                                              :sym-key    sym-key
                                                              :sym-key-id sym-key-id}]))]
      (fx/merge cofx
                {:shh/generate-sym-key-from-password [{:web3       (:web3 db)
                                                       :password   chat-id
                                                       :on-success on-success}]}
                (protocol/init-chat {:chat-id chat-id
                                     :topic   (transport.utils/get-topic chat-id)})))))

(handlers/register-handler-fx
 ::add-new-sym-key
 (fn [{:keys [db] :as cofx} [_ {:keys [sym-key-id sym-key chat-id]}]]
   (let [{:keys [web3]} db
         topic          (transport.utils/get-topic chat-id)]
     {:db             (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
      :shh/add-filter {:web3       web3
                       :sym-key-id sym-key-id
                       :topic      topic
                       :chat-id    chat-id}
      :data-store/tx  [(transport-store/save-transport-tx
                        {:chat-id chat-id
                         :chat    (-> (get-in db [:transport/chats chat-id])
                                      (assoc :sym-key-id sym-key-id)
                                      ;;TODO (yenda) remove once go implements persistence
                                      (assoc :sym-key sym-key))})]})))
