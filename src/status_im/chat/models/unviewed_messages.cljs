(ns status-im.chat.models.unviewed-messages)

(defn load-unviewed-messages [db raw-unviewed-messages]
  (assoc db :unviewed-messages
         (->> raw-unviewed-messages
              (group-by :chat-id)
              (map (fn [[id messages]]
                     [id {:messages-ids (map :message-id messages)
                          :count        (count messages)}]))
              (into {}))))

(defn add-unviewed-message [db chat-id message-id]
  (-> db
      (update-in [:unviewed-messages chat-id :messages-ids] conj message-id)
      (update-in [:unviewed-messages chat-id :count] inc)))

(defn remove-unviewed-messages [db chat-id]
  (update db :unviewed-messages dissoc chat-id))
