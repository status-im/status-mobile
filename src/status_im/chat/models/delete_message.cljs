(ns status-im.chat.models.delete-message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.message-list :as message-list]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn- update-db-clear-undo-timer
  [db chat-id message-id]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               dissoc
               :deleted-undoable-till)))

(defn- update-db-delete-locally
  "Delete message in re-frame db and set the undo timelimit"
  [db chat-id message-id undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               assoc
               :deleted? true
               :deleted-undoable-till (+ (datetime/timestamp)
                                         undo-time-limit-ms))))

(defn- update-db-undo-locally
  "Restore deleted message if called within timelimit"
  [db chat-id message-id]
  (let [{:keys [deleted? deleted-undoable-till]}
        (get-in db [:messages chat-id message-id])]
    (if (and deleted?
             (> deleted-undoable-till (datetime/timestamp)))
      (update-in db
                 [:messages chat-id message-id]
                 dissoc
                 :deleted?
                 :deleted-undoable-till)
      (update-db-clear-undo-timer db chat-id message-id))))

(fx/defn delete
  "Delete message now locally and broadcast after undo time limit timeout"
  {:events [:chat.ui/delete-message]}
  [{:keys [db]} {:keys [chat-id message-id]} undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (assoc
     (message-list/rebuild-message-list
      {:db (update-db-delete-locally db chat-id message-id undo-time-limit-ms)}
      chat-id)
     :utils/dispatch-later [{:dispatch [:chat.ui/delete-message-and-send
                                        {:chat-id    chat-id
                                         :message-id message-id}]
                             :ms       undo-time-limit-ms}])))

(fx/defn undo
  {:events [:chat.ui/undo-delete-message]}
  [{:keys [db]} {:keys [chat-id message-id]}]
  (when (get-in db [:messages chat-id message-id])
    (message-list/rebuild-message-list
     {:db (update-db-undo-locally db chat-id message-id)}
     chat-id)))

(fx/defn delete-and-send
  {:events [:chat.ui/delete-message-and-send]}
  [{:keys [db]} {:keys [message-id chat-id]}]
  (when (get-in db [:messages chat-id message-id])
    {:db             (update-db-clear-undo-timer db chat-id message-id)
     ::json-rpc/call [{:method      "wakuext_deleteMessageAndSend"
                       :params      [message-id]
                       :js-response true
                       :on-error    #(log/error "failed to delete message " message-id " " %)
                       :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])}]}))
(defn- chats-reducer
  "traverse all messages find not yet synced deleted? messages, generate dispatch vector"
  [acc chat-id messages]
  (->> messages
       (filter (fn [[_ {:keys [deleted? deleted-undoable-till]}]] (and deleted? deleted-undoable-till)))
       (map #(vector :chat.ui/delete-message-and-send {:chat-id    chat-id
                                                       :message-id (first %)}))
       (concat acc)))

(fx/defn send-all
  "Get all deleted messages that not yet synced with status-go and send them"
  {:events [:chat.ui/send-all-deleted-messages]}
  [{:keys [db]}]
  {:dispatch-n (reduce-kv chats-reducer [] (:messages db))})
