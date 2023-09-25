(ns status-im.chat.models.loading
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.messages.list.events :as message-list]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(defn cursor->clock-value
  [^js cursor]
  (js/parseInt (.substring cursor 51 64)))

(defn clock-value->cursor
  [clock-value]
  (str "000000000000000000000000000000000000000000000000000"
       clock-value
       "0x0000000000000000000000000000000000000000000000000000000000000000"))

(rf/defn update-chats-in-app-db
  {:events [:chats-list/load-success]}
  [{:keys [db]} ^js new-chats-js]
  (let [{:keys [all-chats chats-home-list]}
        (reduce (fn [acc ^js chat-js]
                  (let [{:keys [chat-id profile-public-key timeline? community-id active] :as chat}
                        (data-store.chats/<-rpc-js chat-js)]
                    (cond-> acc
                      (and (not profile-public-key) (not timeline?) (not community-id) active)
                      (update :chats-home-list conj chat-id)
                      :always
                      (assoc-in [:all-chats chat-id] chat))))
                {:all-chats       {}
                 :chats-home-list #{}}
                new-chats-js)]
    {:db (assoc db
                :chats           all-chats
                :chats-home-list chats-home-list
                :chats/loading?  false)}))

(rf/defn load-chat-success
  {:events [:chats-list/load-chat-success]}
  [{:keys [db]} ^js chat]
  (let [{:keys [chat-id] :as chat} (data-store.chats/<-rpc chat)]
    {:db (update-in db [:chats chat-id] merge chat)}))

(rf/defn load-chat
  [_ chat-id]
  {:json-rpc/call [{:method     "wakuext_chat"
                    :params     [chat-id]
                    :on-success #(re-frame/dispatch [:chats-list/load-chat-success %])
                    :on-error   #(log/error "failed to fetch chats" 0 -1 %)}]})

(rf/defn handle-failed-loading-messages
  {:events [::failed-loading-messages]}
  [{:keys [db]} current-chat-id _ err]
  (log/error "failed loading messages" current-chat-id err)
  (when current-chat-id
    {:db (assoc-in db [:pagination-info current-chat-id :loading-messages?] false)}))

(defn mark-chat-all-read
  [db chat-id]
  (when (get-in db [:chats chat-id])
    (update-in
     db
     [:chats chat-id]
     assoc
     :unviewed-messages-count 0
     :unviewed-mentions-count 0
     :highlight               false)))

(rf/defn handle-mark-all-read-successful
  {:events [::mark-all-read-successful]}
  [_]
  {:dispatch [:activity-center.notifications/fetch-unread-count]})

(rf/defn handle-mark-all-read-in-community-successful
  {:events [::mark-all-read-in-community-successful]}
  [{:keys [db] :as cofx} chat-ids]
  (rf/merge cofx
            {:db       (reduce mark-chat-all-read db chat-ids)
             :dispatch [:activity-center.notifications/fetch-unread-count]}))

(rf/defn handle-mark-all-read
  {:events [:chat.ui/mark-all-read-pressed :chat/mark-all-as-read]}
  [{db :db} chat-id]
  {:db                          (mark-chat-all-read db chat-id)
   :clear-message-notifications [[chat-id]
                                 (get-in db [:profile/profile :remote-push-notifications-enabled?])]
   :json-rpc/call               [{:method     "wakuext_markAllRead"
                                  :params     [chat-id]
                                  :on-success #(re-frame/dispatch [::mark-all-read-successful])}]})

(rf/defn handle-mark-mark-all-read-in-community
  {:events [:chat.ui/mark-all-read-in-community-pressed]}
  [{db :db} community-id]
  (let [community-chat-ids (map #(str community-id %)
                                (keys (get-in db [:communities community-id :chats])))]
    {:clear-message-notifications [community-chat-ids
                                   (get-in db [:profile/profile :remote-push-notifications-enabled?])]
     :json-rpc/call               [{:method     "wakuext_markAllReadInCommunity"
                                    :params     [community-id]
                                    :on-success #(re-frame/dispatch
                                                  [::mark-all-read-in-community-successful %])}]}))

(rf/defn messages-loaded
  "Loads more messages for current chat"
  {:events [::messages-loaded]}
  [{db :db} chat-id session-id {:keys [cursor messages]} on-loaded]
  (when-not (and (get-in db [:pagination-info chat-id :messages-initialized?])
                 (not= session-id
                       (get-in db [:pagination-info chat-id :messages-initialized?])))
    (let [already-loaded-messages (get-in db [:messages chat-id])
          ;; We remove those messages that are already loaded, as we might get some duplicates
          {:keys [all-messages new-messages contacts]}
          (reduce (fn [{:keys [all-messages] :as acc}
                       {:keys [message-id from]
                        :as   message}]
                    (let [message
                          ;; For example, when a user receives a list of 4 image messages while inside
                          ;; the chat screen we shouldn't group the images into albums. When the user
                          ;; exists the chat screen then enters the chat screen again, we now need to
                          ;; group the images into albums (like WhatsApp). The albumize? boolean is used
                          ;; to know whether we need to group these images into albums now or not. The
                          ;; album-id can't be used for this because it will always be there.
                          (if (and (:album-id message) (nil? (get all-messages message-id)))
                            (assoc message :albumize? true)
                            message)]
                      (cond-> acc
                        (not (get-in db [:chats chat-id :users from]))
                        (update :senders assoc from message)

                        (nil? (get all-messages message-id))
                        (update :new-messages conj message)

                        :always
                        (update :all-messages assoc message-id message))))
                  {:all-messages already-loaded-messages
                   :senders      {}
                   :contacts     {}
                   :new-messages []}
                  messages)
          current-clock-value (get-in db
                                      [:pagination-info chat-id
                                       :cursor-clock-value])
          clock-value (when cursor (cursor->clock-value cursor))]
      (when on-loaded
        (on-loaded (count new-messages)))
      {:db (-> db
               (update-in [:pagination-info chat-id :cursor-clock-value]
                          #(if (and (seq cursor) (or (not %) (< clock-value %)))
                             clock-value
                             %))
               (update-in [:pagination-info chat-id :cursor]
                          #(if (or (empty? cursor)
                                   (not current-clock-value)
                                   (< clock-value current-clock-value))
                             cursor
                             %))
               (assoc-in [:pagination-info chat-id :loading-messages?] false)
               (assoc-in [:messages chat-id] all-messages)
               (update-in [:message-lists chat-id] message-list/add-many new-messages)
               (assoc-in [:pagination-info chat-id :all-loaded?]
                         (empty? cursor))
               (update :contacts/contacts merge contacts))})))

(rf/defn load-more-messages
  {:events [:chat.ui/load-more-messages]}
  [{:keys [db]} chat-id first-request on-loaded]
  (when-let [session-id (get-in db [:pagination-info chat-id :messages-initialized?])]
    (when (and
           (not (get-in db [:pagination-info chat-id :all-loaded?]))
           (not (get-in db [:pagination-info chat-id :loading-messages?])))
      (let [cursor (get-in db [:pagination-info chat-id :cursor])]
        (when (or first-request cursor)
          (merge
           {:db (assoc-in db [:pagination-info chat-id :loading-messages?] true)}
           {:utils/dispatch-later [{:ms 100 :dispatch [:load-more-reactions cursor chat-id]}]}
           (data-store.messages/messages-by-chat-id-rpc
            chat-id
            cursor
            constants/default-number-of-messages
            #(re-frame/dispatch [::messages-loaded chat-id session-id % on-loaded])
            #(re-frame/dispatch [::failed-loading-messages chat-id session-id %]))))))))

(rf/defn load-more-messages-for-current-chat
  {:events [:chat.ui/load-more-messages-for-current-chat]}
  [{:keys [db] :as cofx} on-loaded]
  (load-more-messages cofx (:current-chat-id db) false on-loaded))

(rf/defn load-messages
  [{:keys [db now] :as cofx} chat-id]
  (when-not (get-in db [:pagination-info chat-id :messages-initialized?])
    (rf/merge cofx
              {:db                   (assoc-in db [:pagination-info chat-id :messages-initialized?] now)
               :utils/dispatch-later [{:ms 50 :dispatch [:chat.ui/mark-all-read-pressed chat-id]}
                                      (when-not (get-in cofx [:db :chats chat-id :public?])
                                        {:ms 100 :dispatch [:pin-message/load-pin-messages chat-id]})]}
              (load-more-messages chat-id true nil))))
