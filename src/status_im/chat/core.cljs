(ns status-im.chat.core
  (:require [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.fx :as fx]
            [status-im.data-store.chats :as chats-store]))

;; Seen messages
(fx/defn receive-seen
  [{:keys [db js-obj]} chat-id sender {:keys [message-ids]}]
  (merge {:transport/confirm-messages-processed [{:web3   (:web3 db)
                                                  :js-obj js-obj}]}
         (when-let [seen-messages-ids (-> (get-in db [:chats chat-id :messages])
                                          (select-keys message-ids)
                                          keys)]
           (let [statuses (map (fn [message-id]
                                 {:chat-id          chat-id
                                  :message-id       message-id
                                  :whisper-identity sender
                                  :status           :seen})
                               seen-messages-ids)]
             {:db            (reduce (fn [acc {:keys [message-id] :as status}]
                                       (assoc-in acc [:chats chat-id :message-statuses
                                                      message-id sender]
                                                 status))
                                     db
                                     statuses)
              :data-store/tx [(user-statuses-store/save-statuses-tx statuses)]}))))

(fx/defn persist-offset
  [{{:keys [chats]} :db}]
  {:data-store/tx (->> chats
                       vals
                       (filter #(pos? (:offset %)))
                       (map chats-store/save-chat-tx)
                       vec)})