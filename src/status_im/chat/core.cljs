(ns status-im.chat.core)

;; Seen messages
(defn receive-seen
  [chat-id sender {:keys [message-ids js-obj]} {:keys [db]}]
  (merge
   {:confirm-message-processed [{:web3   (:web3 db)
                                 :js-obj js-obj}]}
   (when-let [seen-messages-ids (-> (get-in db [:chats chat-id :messages])
                                    (select-keys message-ids)
                                    keys)]
     {:db (reduce
           (fn [new-db message-id]
             (assoc-in new-db
                       [:chats chat-id
                        :messages message-id
                        :user-statuses sender]
                       :seen))
           db
           seen-messages-ids)})))
