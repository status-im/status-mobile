(ns status-im2.contexts.chat.messages.delete-message-for-me.events
  (:require
    [utils.i18n :as i18n]
    [quo2.foundations.colors :as colors]
    [status-im2.contexts.chat.messages.list.events :as message-list]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]
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
    (let [existing-undo-toast (get-in db [:toasts :toasts :delete-message-for-me])
          toast-count         (inc (get existing-undo-toast :message-deleted-for-me-count 0))
          existing-undos      (-> existing-undo-toast
                                  (get :message-deleted-for-me-undos [])
                                  (conj {:message-id message-id :chat-id chat-id}))]
      (assoc
       (message-list/rebuild-message-list
        {:db (reduce
              ;; sync all pending deletes' undo timelimit, extend to the latest one
              (fn [db-acc {:keys [message-id chat-id]}]
                (update-db-delete-locally db-acc chat-id message-id undo-time-limit-ms))
              db
              existing-undos)}
        chat-id)
       :dispatch-n           [[:toasts/close :delete-message-for-me]
                              [:toasts/upsert
                               {:id :delete-message-for-me
                                :icon :info
                                :icon-color colors/danger-50-opa-40
                                :message-deleted-for-me-count toast-count
                                :message-deleted-for-me-undos existing-undos
                                :text (i18n/label-pluralize toast-count
                                                            :t/message-deleted-for-you-count)
                                :duration undo-time-limit-ms
                                :undo-duration (/ undo-time-limit-ms 1000)
                                :undo-on-press #(do (rf/dispatch
                                                     [:chat.ui/undo-all-delete-message-for-me])
                                                    (rf/dispatch [:toasts/close
                                                                  :delete-message-for-me]))}]]
       :utils/dispatch-later [{:dispatch [:chat.ui/delete-message-for-me-and-sync
                                          {:chat-id chat-id :message-id message-id}]
                               :ms       undo-time-limit-ms}]))))

(rf/defn undo
  {:events [:chat.ui/undo-delete-message-for-me]}
  [{:keys [db]} {:keys [chat-id message-id]}]
  (when (get-in db [:messages chat-id message-id])
    (message-list/rebuild-message-list
     {:db (update-db-undo-locally db chat-id message-id)}
     chat-id)))

(rf/defn undo-all
  {:events [:chat.ui/undo-all-delete-message-for-me]}
  [{:keys [db]}]
  (when-let [pending-undos (get-in db
                                   [:toasts :toasts :delete-message-for-me
                                    :message-deleted-for-me-undos])]
    {:dispatch-n (mapv #(vector :chat.ui/undo-delete-message-for-me %) pending-undos)}))

(defn- check-before-delete-and-sync
  "Make sure message alredy deleted-for-me? locally and undo timelimit has passed"
  [db chat-id message-id]
  (let [message                                                (get-in db [:messages chat-id message-id])
        {:keys [deleted-for-me? deleted-for-me-undoable-till]} message]
    (and deleted-for-me?
         deleted-for-me-undoable-till
         (>= (datetime/timestamp) deleted-for-me-undoable-till))))

(rf/defn delete-and-sync
  {:events [:chat.ui/delete-message-for-me-and-sync]}
  [{:keys [db]} {:keys [message-id chat-id]} force?]
  (when-let [message (get-in db [:messages chat-id message-id])]
    (when (or force? (check-before-delete-and-sync db chat-id message-id))
      {:db            (update-db-clear-undo-timer db chat-id message-id)
       :json-rpc/call [{:method      "wakuext_deleteMessageForMeAndSync"
                        :params      [chat-id message-id]
                        :js-response true
                        :on-error    #(log/error
                                       "failed to delete message for me, message id: "
                                       {:message-id message-id :error %})
                        :on-success  #(rf/dispatch [:sanitize-messages-and-process-response
                                                    %])}]})))

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
    (apply rf/merge cofx (map #(delete-and-sync % true) pending-sync-messages))))
