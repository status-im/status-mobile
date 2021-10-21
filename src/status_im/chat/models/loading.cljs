(ns status-im.chat.models.loading
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.utils.fx :as fx]
            [status-im.chat.models.message-list :as message-list]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.chat.models.pin-message :as models.pin-message]))

(defn cursor->clock-value
  [^js cursor]
  (js/parseInt (.substring cursor 51 64)))

(defn clock-value->cursor [clock-value]
  (str "000000000000000000000000000000000000000000000000000"
       clock-value
       "0x0000000000000000000000000000000000000000000000000000000000000000"))

(fx/defn update-chats-in-app-db
  {:events [:chats-list/load-success]}
  [{:keys [db]} ^js new-chats-js]
  (let [{:keys [all-chats chats-home-list]}
        (reduce (fn [acc ^js chat-js]
                  (let [{:keys [chat-id profile-public-key timeline? community-id] :as chat}
                        (data-store.chats/<-rpc-js chat-js)]
                    (cond-> acc
                      (and (not profile-public-key) (not timeline?) (not community-id))
                      (update :chats-home-list conj chat-id)
                      :always
                      (assoc-in [:all-chats chat-id] chat))))
                {:all-chats {}
                 :chats-home-list #{}}
                new-chats-js)]
    {:db (assoc db :chats all-chats
                :chats-home-list chats-home-list
                :chats/loading? false)}))

(fx/defn load-chat-success
  {:events [:chats-list/load-chat-success]}
  [{:keys [db]} ^js chat]
  (let [{:keys [chat-id] :as chat} (data-store.chats/<-rpc chat)]
    {:db (update-in db [:chats chat-id] merge chat)}))

(fx/defn load-chat
  [_ chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "chat")
                     :params [chat-id]
                     :on-success #(re-frame/dispatch [:chats-list/load-chat-success %])
                     :on-failure #(log/error "failed to fetch chats" 0 -1 %)}]})

(fx/defn handle-failed-loading-messages
  {:events [::failed-loading-messages]}
  [{:keys [db]} current-chat-id _ err]
  (log/error "failed loading messages" current-chat-id err)
  (when current-chat-id
    {:db (assoc-in db [:pagination-info current-chat-id :loading-messages?] false)}))

(defn mark-chat-all-read
  [db chat-id]
  (update-in db [:chats chat-id] assoc
             :unviewed-messages-count 0
             :unviewed-mentions-count 0
             :highlight false))

(fx/defn handle-mark-all-read-successful
  {:events [::mark-all-read-successful]}
  [{:keys [db]} chat-id]
  {:db (mark-chat-all-read db chat-id)})

(fx/defn handle-mark-all-read-in-community-successful
  {:events [::mark-all-read-in-community-successful]}
  [{:keys [db]} chat-ids]
  {:db (reduce mark-chat-all-read db chat-ids)})

(fx/defn handle-mark-all-read
  {:events [:chat.ui/mark-all-read-pressed :chat/mark-all-as-read]}
  [_ chat-id]
  {:clear-message-notifications chat-id
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "markAllRead")
                     :params     [chat-id]
                     :on-success #(re-frame/dispatch [::mark-all-read-successful chat-id])}]})

(fx/defn handle-mark-mark-all-read-in-community
  {:events [:chat.ui/mark-all-read-in-community-pressed]}
  [_ community-id]
  {:clear-message-notifications community-id
   ::json-rpc/call [{:method     (json-rpc/call-ext-method "markAllReadInCommunity")
                     :params     [community-id]
                     :on-success #(re-frame/dispatch [::mark-all-read-in-community-successful %])}]})

(fx/defn messages-loaded
  "Loads more messages for current chat"
  {:events [::messages-loaded]}
  [{db :db} chat-id session-id {:keys [cursor messages]}]
  (when-not (and (get-in db [:pagination-info chat-id :messages-initialized?])
                 (not= session-id
                       (get-in db [:pagination-info chat-id :messages-initialized?])))
    (let [already-loaded-messages (get-in db [:messages chat-id])
          ;; We remove those messages that are already loaded, as we might get some duplicates
          {:keys [all-messages new-messages senders contacts]}
          (reduce (fn [{:keys [all-messages] :as acc}
                       {:keys [message-id from]
                        :as   message}]
                    (cond-> acc
                      (not (get-in db [:chats chat-id :users from]))
                      (update :senders assoc from message)

                      (nil? (get all-messages message-id))
                      (update :new-messages conj message)

                      :always
                      (update :all-messages assoc message-id message)))
                  {:all-messages already-loaded-messages
                   :senders      {}
                   :contacts     {}
                   :new-messages []}
                  messages)
          current-clock-value (get-in db [:pagination-info chat-id :cursor-clock-value])
          clock-value (when cursor (cursor->clock-value cursor))]
      {:dispatch [:chat/add-senders-to-chat-users (vals senders)]
       :db       (-> db
                     (update-in [:pagination-info chat-id :cursor-clock-value]
                                #(if (and (seq cursor) (or (not %) (< clock-value %)))
                                   clock-value
                                   %))

                     (update-in [:pagination-info chat-id :cursor]
                                #(if (or (empty? cursor) (not current-clock-value) (< clock-value current-clock-value))
                                   cursor
                                   %))
                     (assoc-in [:pagination-info chat-id :loading-messages?] false)
                     (assoc-in [:messages chat-id] all-messages)
                     (update-in [:message-lists chat-id] message-list/add-many new-messages)
                     (assoc-in [:pagination-info chat-id :all-loaded?]
                               (empty? cursor))
                     (update :contacts/contacts merge contacts))})))

(fx/defn load-more-messages
  {:events [:chat.ui/load-more-messages]}
  [{:keys [db]} chat-id first-request]
  (when-let [session-id (get-in db [:pagination-info chat-id :messages-initialized?])]
    (when (and
           (not (get-in db [:pagination-info chat-id :all-loaded?]))
           (not (get-in db [:pagination-info chat-id :loading-messages?])))
      (let [cursor (get-in db [:pagination-info chat-id :cursor])]
        (when (or first-request cursor)
          (merge
           {:db (assoc-in db [:pagination-info chat-id :loading-messages?] true)}
           {:utils/dispatch-later [{:ms 100 :dispatch [:load-more-reactions cursor chat-id]}
                                   {:ms 100 :dispatch [::models.pin-message/load-pin-messages chat-id]}]}
           (data-store.messages/messages-by-chat-id-rpc
            chat-id
            cursor
            constants/default-number-of-messages
            #(re-frame/dispatch [::messages-loaded chat-id session-id %])
            #(re-frame/dispatch [::failed-loading-messages chat-id session-id %]))))))))

(fx/defn load-more-messages-for-current-chat
  {:events [:chat.ui/load-more-messages-for-current-chat]}
  [{:keys [db] :as cofx}]
  (load-more-messages cofx (:current-chat-id db) false))

(fx/defn load-messages
  [{:keys [db now] :as cofx} chat-id]
  (when-not (get-in db [:pagination-info chat-id :messages-initialized?])
    (fx/merge cofx
              {:db (assoc-in db [:pagination-info chat-id :messages-initialized?] now)
               :utils/dispatch-later [{:ms 500 :dispatch [:chat.ui/mark-all-read-pressed chat-id]}]}
              (load-more-messages chat-id true))))
