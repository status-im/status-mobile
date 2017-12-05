(ns status-im.chat.models.unviewed-messages)

(defn index-unviewed-messages [unviewed-messages] 
  (into {}
        (map (fn [[chat-id messages]]
               [chat-id (into #{} (map :message-id) messages)]))
        (group-by :chat-id unviewed-messages)))

(defn add-unviewed-message [db chat-id message-id] 
  (update-in db [:chats chat-id :unviewed-messages] (fnil conj #{}) message-id))

(defn remove-unviewed-message [db chat-id message-id]
  (update-in db [:chats chat-id :unviewed-messages] disj message-id))
