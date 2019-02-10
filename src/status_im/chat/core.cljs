(ns status-im.chat.core
  (:require [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.fx :as fx]))

;; Seen messages
(fx/defn receive-seen
  [{:keys [db js-obj]} chat-id sender {:keys [message-ids]}]
  (when-let [seen-messages-ids (-> (get-in db [:chats chat-id :messages])
                                   (select-keys message-ids)
                                   keys)]
    (let [statuses (map (fn [message-id]
                          {:chat-id          chat-id
                           :message-id       message-id
                           :public-key       sender
                           :status           :seen})
                        seen-messages-ids)]
      {:db            (reduce (fn [acc {:keys [message-id] :as status}]
                                (assoc-in acc [:chats chat-id :message-statuses
                                               message-id sender]
                                          status))
                              db
                              statuses)
       :data-store/tx [{:transaction (user-statuses-store/save-statuses-tx statuses)
                        :success-event [:message/messages-persisted [js-obj]]}]})))
