(ns status-im.chat.models.delete-message-for-me
  (:require [re-frame.core :as re-frame]
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
               :deleted-for-me-undoable-till)))

(defn- update-db-delete-locally
  "Delete message for me in re-frame db and set the undo timelimit"
  [db chat-id message-id undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               assoc
               :deleted-for-me? true
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

(fx/defn delete
  "Delete message for me now locally and broadcast after undo time limit timeout"
  {:events [:chat.ui/delete-message-for-me]}
  [{:keys [db]} {:keys [chat-id message-id]} undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    {:db (update-db-delete-locally db chat-id message-id undo-time-limit-ms)
     :utils/dispatch-later [{:dispatch [:chat.ui/delete-message-for-me-and-sync
                                        {:chat-id    chat-id
                                         :message-id message-id}]
                             :ms       undo-time-limit-ms}]}))

(fx/defn undo
  {:events [:chat.ui/undo-delete-message-for-me]}
  [{:keys [db]} {:keys [chat-id message-id]}]
  (when (get-in db [:messages chat-id message-id])
    {:db (update-db-undo-locally db chat-id message-id)}))

(fx/defn delete-and-sync
  {:events [:chat.ui/delete-message-for-me-and-sync]}
  [{:keys [db]} {:keys [message-id chat-id]}]
  (when (get-in db [:messages chat-id message-id])
    {:db             (update-db-clear-undo-timer db chat-id message-id)
     ::json-rpc/call [{:method      "wakuext_deleteMessageForMeAndSync"
                       :params      [chat-id message-id]
                       :js-response true
                       :on-error    #(log/error "failed to delete message for me " %)
                       :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response
                                                         %])}]}))
