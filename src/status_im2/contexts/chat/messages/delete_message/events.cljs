(ns status-im2.contexts.chat.messages.delete-message.events
  (:require
   [i18n.i18n :as i18n]
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
               :deleted-undoable-till)))

(defn- update-db-delete-locally
  "Delete message in re-frame db and set the undo timelimit"
  [db chat-id message-id undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    (update-in db
               [:messages chat-id message-id]
               assoc
               :deleted?              true
               :deleted-undoable-till (+ (datetime/timestamp) undo-time-limit-ms))))

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

(defn- update-db-delete-locally-without-time-limit
  "Delete message in re-frame db, used to handle received removed-messages"
  [db chat-id message-id]
  (when (get-in db [:messages chat-id message-id])
    (update-in db [:messages chat-id message-id] assoc :deleted? true)))

(rf/defn delete
  "Delete message now locally and broadcast after undo time limit timeout"
  {:events [:chat.ui/delete-message]}
  [{:keys [db]} {:keys [chat-id message-id]} undo-time-limit-ms]
  (when (get-in db [:messages chat-id message-id])
    ;; all delete message toast are the same toast with id :delete-message-for-everyone
    ;; new delete operation will reset prev pending deletes' undo timelimit
    ;; undo will undo all pending deletes
    ;; all pending deletes are stored in toast
    (let [existing-undo-toast (get-in db [:toasts :toasts :delete-message-for-everyone])
          toast-count         (inc (get existing-undo-toast :message-deleted-for-everyone-count 0))
          existing-undos      (-> existing-undo-toast
                                  (get :message-deleted-for-everyone-undos [])
                                  (conj {:message-id message-id :chat-id chat-id}))]
      (assoc
       (message-list/rebuild-message-list
        {:db (reduce
              ;; sync all pending deletes' undo timelimit, extend to the latest one
              (fn [db-acc {:keys [chat-id message-id]}]
                (update-db-delete-locally db-acc chat-id message-id undo-time-limit-ms))
              db
              existing-undos)}
        chat-id)

       :dispatch-n
       [[:toasts/close :delete-message-for-everyone]
        [:toasts/upsert
         {:id                                 :delete-message-for-everyone
          :icon                               :info
          :icon-color                         colors/danger-50-opa-40
          :message-deleted-for-everyone-count toast-count
          :message-deleted-for-everyone-undos existing-undos
          :text                               (i18n/label-pluralize
                                               toast-count
                                               :t/message-deleted-for-everyone-count)
          :duration                           undo-time-limit-ms
          :undo-duration                      (/ undo-time-limit-ms 1000)
          :undo-on-press                      #(do (rf/dispatch [:chat.ui/undo-all-delete-message])
                                                   (rf/dispatch [:toasts/close
                                                                 :delete-message-for-everyone]))}]]
       :utils/dispatch-later [{:dispatch [:chat.ui/delete-message-and-send
                                          {:chat-id chat-id :message-id message-id}]
                               :ms       undo-time-limit-ms}]))))

(rf/defn undo
  {:events [:chat.ui/undo-delete-message]}
  [{:keys [db]} {:keys [chat-id message-id]}]
  (when (get-in db [:messages chat-id message-id])
    (message-list/rebuild-message-list
     {:db (update-db-undo-locally db chat-id message-id)}
     chat-id)))

(rf/defn undo-all
  {:events [:chat.ui/undo-all-delete-message]}
  [{:keys [db]}]
  (when-let [pending-undos (get-in db
                                   [:toasts :toasts :delete-message-for-everyone
                                    :message-deleted-for-everyone-undos])]
    {:dispatch-n (mapv #(vector :chat.ui/undo-delete-message %) pending-undos)}))

(defn- check-before-delete-and-send
  "make sure message alredy deleted? locally and undo timelimit has passed"
  [db chat-id message-id]
  (let [message                                  (get-in db [:messages chat-id message-id])
        {:keys [deleted? deleted-undoable-till]} message]
    (and deleted?
         deleted-undoable-till
         (>= (datetime/timestamp) deleted-undoable-till))))

(rf/defn delete-and-send
  {:events [:chat.ui/delete-message-and-send]}
  [{:keys [db]} {:keys [message-id chat-id]} force?]
  (when-let [message (get-in db [:messages chat-id message-id])]
    (when (or force? (check-before-delete-and-send db chat-id message-id))
      (cond-> {:db            (update-db-clear-undo-timer db chat-id message-id)
               :json-rpc/call [{:method      "wakuext_deleteMessageAndSend"
                                :params      [message-id]
                                :js-response true
                                :on-error    #(log/error "failed to delete message "
                                                         {:message-id message-id :error %})
                                :on-success  #(rf/dispatch
                                               [:sanitize-messages-and-process-response
                                                %])}]}
        (get-in db [:pin-messages chat-id message-id])
        (assoc :dispatch
               [:pin-message/send-pin-message
                {:chat-id chat-id :message-id message-id :pinned false}])))))

(defn- filter-pending-send-messages
  "traverse all messages find not yet synced deleted? messages"
  [acc chat-id messages]
  (->> messages
       (filter (fn [[_ {:keys [deleted? deleted-undoable-till]}]] (and deleted? deleted-undoable-till)))
       (map (fn [message] {:chat-id chat-id :message-id (first message)}))
       (concat acc)))

(rf/defn send-all
  "Get all deleted messages that not yet synced with status-go and send them"
  {:events [:chat.ui/send-all-deleted-messages]}
  [{:keys [db] :as cofx}]
  (let [pending-send-messages (reduce-kv filter-pending-send-messages [] (:messages db))]
    (apply rf/merge cofx (map #(delete-and-send % true) pending-send-messages))))

(rf/defn delete-messages-localy
  "Mark messages :deleted? localy in client"
  {:events [:chat.ui/delete-messages-localy]}
  [{:keys [db]} messages chat-id]
  (let [new-db (->> messages
                    (filter #(get-in db [:messages chat-id (:message-id %)]))
                    (reduce #(update-db-delete-locally-without-time-limit %1 chat-id (:message-id %2))
                            db))]
    (when new-db
      (message-list/rebuild-message-list {:db new-db} chat-id))))
