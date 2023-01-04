(ns status-im2.contexts.chat.messages.delete-message-for-me.events
  (:require [status-im.chat.models.message-list :as message-list]
            [utils.datetime :as datetime]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn- update-db-clear-undo-timer
  [db chat-id message-id]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               dissoc
               :deleted-for-me-undoable-till)))

(defn- update-db-delete-locally
  "Delete message for me in re-frame db and set the undo timelimit"
  [db chat-id message-id undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               assoc
               :deleted-for-me?              true
               :deleted-for-me-undoable-till (+ (datetime/timestamp)
                                                undo-time-limit-ms))))

(defn- update-db-undo-locally
  "Restore deleted-for-me message if called within timelimit"
  [db chat-id message-id]
  (let [{:keys [deleted-for-me? deleted-for-me-undoable-till]}
        (get-in db [:messages chat-id message-id])]
    (if (and deleted-for-me?
             (> deleted-for-me-undoable-till (datetime/timestamp)))
      (update-in db
                 [:messages chat-id message-id]
                 dissoc
                 :deleted-for-me?
                 :deleted-for-me-undoable-till)
      (update-db-clear-undo-timer db chat-id message-id))))

(rf/defn delete
  "Delete message for me now locally and broadcast after undo time limit timeout"
  {:events [:chat.ui/delete-message-for-me]}
  [{:keys [db]} {:keys [chat-id message-id]} undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (assoc
     (message-list/rebuild-message-list
      {:db (update-db-delete-locally db chat-id message-id undo-time-limit-ms)}
      chat-id)
     :utils/dispatch-later
     [{:dispatch [:chat.ui/delete-message-for-me-and-sync
                  {:chat-id    chat-id
                   :message-id message-id}]
       :ms       undo-time-limit-ms}])))

(rf/defn undo
  {:events [:chat.ui/undo-delete-message-for-me]}
  [{:keys [db]} {:keys [chat-id message-id]}]
  (when (get-in db [:messages chat-id message-id])
    (message-list/rebuild-message-list
     {:db (update-db-undo-locally db chat-id message-id)}
     chat-id)))

(rf/defn delete-and-sync
  {:events [:chat.ui/delete-message-for-me-and-sync]}
  [{:keys [db]} {:keys [message-id chat-id]}]
  (when (get-in db [:messages chat-id message-id])
    {:db            (update-db-clear-undo-timer db chat-id message-id)
     :json-rpc/call [{:method      "wakuext_deleteMessageForMeAndSync"
                      :params      [chat-id message-id]
                      :js-response true
                      :on-error    #(log/error "failed to delete message for me, message id: "
                                               {:message-id message-id :error %})
                      :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])}]}))

(defn- filter-pending-sync-messages
  "traverse all messages find not yet synced deleted-for-me? messages"
  [acc chat-id messages]
  (->> messages
       (filter (fn [[_ {:keys [deleted-for-me? deleted-for-me-undoable-till]}]]
                 (and deleted-for-me? deleted-for-me-undoable-till)))
       (map (fn [message] {:chat-id chat-id :message-id (first message)}))
       (concat acc)))

(rf/defn sync-all
  "Get all deleted-for-me messages that not yet synced with status-go and sync them"
  {:events [:chat.ui/sync-all-deleted-for-me-messages]}
  [{:keys [db] :as cofx}]
  (let [pending-sync-messages (reduce-kv filter-pending-sync-messages [] (:messages db))]
    (apply rf/merge cofx (map delete-and-sync pending-sync-messages))))
