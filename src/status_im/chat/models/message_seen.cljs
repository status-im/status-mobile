(ns status-im.chat.models.message-seen
  (:require [status-im.utils.fx :as fx]
            [status-im.data-store.messages :as messages-store]))

(defn subtract-seen-messages
  [old-count new-seen-messages-ids]
  (max 0 (- old-count (count new-seen-messages-ids))))

(fx/defn update-chats-unviewed-messages-count
  [{:keys [db]} chat-id loaded-unviewed-messages-ids]
  (let [{:keys [unviewed-messages-count]}
        (get-in db [:chats chat-id])]
    {:db (update-in db [:chats chat-id] assoc
                    :unviewed-messages-count (subtract-seen-messages
                                              unviewed-messages-count
                                              loaded-unviewed-messages-ids))}))

(fx/defn mark-messages-seen
  "Marks all unviewed loaded messages as seen in particular chat"
  {:events [:chat/mark-messages-seen]}
  [{:keys [db] :as cofx} chat-id ids]
  (fx/merge cofx
            {:db (reduce (fn [acc message-id]
                           (assoc-in acc [:messages chat-id message-id :seen]
                                     true))
                         db
                         ids)}
            (messages-store/mark-messages-seen chat-id ids nil)
            (update-chats-unviewed-messages-count chat-id ids)))
