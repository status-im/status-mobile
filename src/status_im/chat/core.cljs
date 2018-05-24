(ns status-im.chat.core
  (:require [status-im.data-store.messages :as messages-store]))

;; Seen messages
(defn receive-seen
  [chat-id sender {:keys [message-ids]} {:keys [db js-obj]}]
  (merge
   {:confirm-messages-processed [{:web3   (:web3 db)
                                  :js-obj js-obj}]}
   (when-let [seen-messages-ids (-> (get-in db [:chats chat-id :messages])
                                    (select-keys message-ids)
                                    keys)]
     (let [new-db (reduce
                   (fn [new-db message-id]
                     (assoc-in new-db
                               [:chats chat-id
                                :messages message-id
                                :user-statuses sender]
                               :seen))
                   db
                   seen-messages-ids)]
       {:db            new-db
        :data-store/tx [(messages-store/update-messages-tx
                         (map #(select-keys (get-in db [:chats chat-id :messages %])
                                            [:message-id :user-statuses])
                              seen-messages-ids))]}))))
