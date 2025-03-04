(ns status-im.contexts.chat.messenger.messages.delete-message-for-me.events
  (:require
    [status-im.contexts.chat.messenger.messages.list.events :as message-list]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
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

(defn delete
  "Delete message for me now locally and broadcast after undo time limit timeout"
  [{:keys [db]} [{:keys [chat-id message-id]} undo-time-limit-ms]]
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
                                :type :negative
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

(rf/reg-event-fx :chat.ui/delete-message-for-me delete)

(defn undo
  [{:keys [db]} [{:keys [chat-id message-id]}]]
  (when (get-in db [:messages chat-id message-id])
    (message-list/rebuild-message-list
     {:db (update-db-undo-locally db chat-id message-id)}
     chat-id)))

(rf/reg-event-fx :chat.ui/undo-delete-message-for-me undo)

(defn undo-all
  [{:keys [db]}]
  (when-let [pending-undos (get-in db
                                   [:toasts :toasts :delete-message-for-me
                                    :message-deleted-for-me-undos])]
    {:dispatch-n (mapv #(vector :chat.ui/undo-delete-message-for-me %) pending-undos)}))

(rf/reg-event-fx :chat.ui/undo-all-delete-message-for-me undo-all)

(defn- check-before-delete-and-sync
  "Make sure message alredy deleted-for-me? locally and undo timelimit has passed"
  [db chat-id message-id]
  (let [message                                                (get-in db [:messages chat-id message-id])
        {:keys [deleted-for-me? deleted-for-me-undoable-till]} message]
    (and deleted-for-me?
         deleted-for-me-undoable-till
         (>= (datetime/timestamp) deleted-for-me-undoable-till))))

(defn delete-and-sync
  [{:keys [db]} [{:keys [message-id chat-id]} force?]]
  (when-let [_message (get-in db [:messages chat-id message-id])]
    (when (or force? (check-before-delete-and-sync db chat-id message-id))
      {:db (update-db-clear-undo-timer db chat-id message-id)
       :fx [[:json-rpc/call
             [{:method      "wakuext_deleteMessageForMeAndSync"
               :params      [chat-id message-id]
               :js-response true
               :on-error    [:chat/delete-message-for-me-and-sync-error message-id]
               :on-success  [:sanitize-messages-and-process-response]}]]]})))

(rf/reg-event-fx :chat.ui/delete-message-for-me-and-sync delete-and-sync)

(defn delete-and-sync-error
  [_ [message-id error]]
  {:fx [[:effects.log/error
         ["failed to delete message for me, message id:"
          {:message-id message-id
           :error      error}]]]})

(rf/reg-event-fx :chat/delete-message-for-me-and-sync-error delete-and-sync-error)

(defn- filter-pending-sync-messages
  "traverse all messages find not yet synced deleted-for-me? messages"
  [acc chat-id messages]
  (->> messages
       (filter (fn [[_ {:keys [deleted-for-me? deleted-for-me-undoable-till]}]]
                 (and deleted-for-me? deleted-for-me-undoable-till)))
       (map (fn [message] {:chat-id chat-id :message-id (first message)}))
       (concat acc)))

(defn sync-all
  "Get all deleted-for-me messages that not yet synced with status-go and sync them"
  [{:keys [db] :as cofx}]
  (let [pending-sync-messages (reduce-kv filter-pending-sync-messages [] (:messages db))
        pending-effects       (map (fn [message]
                                     (fn [cofx]
                                       (delete-and-sync cofx [message true])))
                                   pending-sync-messages)]
    (apply rf/merge cofx pending-effects)))

(rf/reg-event-fx :chat.ui/sync-all-deleted-for-me-messages sync-all)
