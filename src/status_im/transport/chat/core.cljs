(ns status-im.transport.chat.core
  (:require [status-im.data-store.transport :as transport-store]
            [status-im.transport.inbox :as inbox]
            [status-im.utils.fx :as fx]))

(fx/defn remove-transport-chat
  [{:keys [db]} chat-id]
  {:db                (update db :transport/chats dissoc chat-id)
   :data-store/tx     [(transport-store/delete-transport-tx chat-id)]
   :shh/remove-filter (get-in db [:transport/filters chat-id])})

(fx/defn unsubscribe-from-chat
  "Unsubscribe from chat on transport layer"
  [cofx chat-id]
  (fx/merge cofx
            (inbox/remove-chat-from-inbox-topic chat-id)
            (remove-transport-chat chat-id)))
