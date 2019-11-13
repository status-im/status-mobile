(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message-list :as message-list]
            [status-im.chat.models.message-content :as message-content]
            [status-im.constants :as constants]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.messages :as messages-store]
            [status-im.ethereum.core :as ethereum]
            [status-im.mailserver.core :as mailserver]
            [status-im.native-module.core :as status]
            [status-im.transport.message.group-chat :as message.group-chat]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.transit :as transit]
            [status-im.transport.utils :as transport.utils]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.ui.components.react :as react]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.datetime :as time]
            [status-im.utils.fx :as fx]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn- wrap-group-message
  "Wrap a group message in a membership update"
  [cofx chat-id message]
  (when-let [chat (get-in cofx [:db :chats chat-id])]
    (message.group-chat/map->GroupMembershipUpdate.
     {:chat-id              chat-id
      :membership-updates   (:membership-updates chat)
      :message              message})))

(defn- prepare-message
  [{:keys [content content-type] :as message} chat-id current-chat?]
  (cond-> message
    current-chat?
    (assoc :seen true) (and (= constants/content-type-text content-type)
                            (message-content/should-collapse?
                             (:text content)
                             (:line-count content)))

    (assoc :should-collapse? true)))

(defn system-message? [{:keys [message-type]}]
  (= :system-message message-type))

(defn add-outgoing-status
  [{:keys [from outgoing-status] :as message} current-public-key]
  (if (and (= from current-public-key)
           (not (system-message? message)))
    (assoc message
           :outgoing true
           ;; We don't override outgoing-status if there, which means
           ;; that our device has sent the message, while if empty is coming
           ;; from a different device
           :outgoing-status (or outgoing-status :sent))
    message))

(defn build-desktop-notification
  [{:keys [db] :as cofx} {:keys [chat-id timestamp content from] :as message}]
  (let [{:keys [group-chat] :as chat} (get-in db [:chats chat-id])
        contact-name (get-in db [:contacts/contacts from :name]
                             (:name (contact.db/public-key->new-contact from)))
        chat-name        (if group-chat
                           (chat.db/group-chat-name chat)
                           contact-name)
        ;; contact name and chat-name are the same in 1-1 chats
        shown-chat-name   (when group-chat chat-name)
        timestamp'        (when-not (< (time/seconds-ago (time/to-date timestamp)) 15)
                            (str " @ " (time/to-short-str timestamp)))
        body-first-line   (when (or shown-chat-name timestamp')
                            (str shown-chat-name timestamp' ":\n"))]
    {:title       contact-name
     :body        (str body-first-line (:text content))
     :prioritary? (not (chat-model/multi-user-chat? cofx chat-id))}))

(fx/defn add-message
  [{:keys [db] :as cofx}
   {{:keys [chat-id message-id clock-value timestamp from] :as message} :message
    :keys [current-chat? batch?]}]
  (let [current-public-key (multiaccounts.model/current-public-key cofx)
        prepared-message (-> message
                             (prepare-message chat-id current-chat?)
                             (add-outgoing-status current-public-key))
        chat-initialized?
        (or
         current-chat?
         (get-in db [:chats chat-id :messages-initialized?]))]
    (when (and platform/desktop?
               (not= from current-public-key)
               (get-in db [:multiaccount :desktop-notifications?])
               (< (time/seconds-ago (time/to-date timestamp)) constants/one-earth-day))
      (let [{:keys [title body prioritary?]} (build-desktop-notification cofx message)]
        (.displayNotification react/desktop-notification title body prioritary?)))
    (fx/merge cofx
              {:db            (cond->
                               (-> db
                                   (update-in [:chats chat-id :last-clock-value] (partial utils.clocks/receive clock-value))
                                   ;; We should not be always adding to the list, as it does not make sense
                                   ;; if the chat has not been initialized, but run into
                                   ;; some troubles disabling it, so next time
                                   (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                                   (update-in [:chats chat-id :message-list] message-list/add prepared-message))
                                (and (not current-chat?)
                                     (not= from current-public-key))
                                (update-in [:chats chat-id :loaded-unviewed-messages-ids]
                                           (fnil conj #{}) message-id))}
              #(messages-store/save-message % prepared-message)
              (when (and platform/desktop?
                         (not batch?)
                         (not (system-message? prepared-message)))

                (chat-model/update-dock-badge-label)))))

(fx/defn add-received-message
  [{:keys [db] :as cofx}
   {:keys [from message-id chat-id content metadata] :as message}]
  (let [{:keys [current-chat-id view-id]} db
        current-public-key             (multiaccounts.model/current-public-key cofx)
        current-chat?                  (and (or (= :chat view-id)
                                                (= :chat-modal view-id))
                                            (= current-chat-id chat-id))]
    (add-message cofx {:batch?       true
                       :message      message
                       :metadata     metadata
                       :current-chat? current-chat?})))

(defn- add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id from]}]
  (let [{:keys [deleted-at-clock-value messages]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (>= deleted-at-clock-value clock-value)))))

(defn extract-chat-id [cofx {:keys [chat-id from message-type]}]
  "Validate and return a valid chat-id"
  (cond
    (and (= :group-user-message message-type)
         (and (get-in cofx [:db :chats chat-id :contacts from])
              (get-in cofx [:db :chats chat-id :members-joined (multiaccounts.model/current-public-key cofx)]))) chat-id
    (and (= :public-group-user-message message-type)
         (get-in cofx [:db :chats chat-id :public?])) chat-id
    (and (= :user-message message-type)
         (= (multiaccounts.model/current-public-key cofx) from)) chat-id
    (= :user-message message-type) from))

(defn calculate-unviewed-message-count
  [{:keys [db] :as cofx} {:keys [chat-id from]}]
  (let [{:keys [current-chat-id view-id]} db
        chat-view?         (or (= :chat view-id)
                               (= :chat-modal view-id))
        current-count (get-in db [:chats chat-id :unviewed-messages-count])]
    (if (or (and chat-view? (= current-chat-id chat-id))
            (= from (multiaccounts.model/current-public-key cofx)))
      current-count
      (inc current-count))))

(fx/defn update-unviewed-count [{:keys [now db] :as cofx} {:keys [chat-id] :as message}]
  {:db (update-in db [:chats chat-id]
                  assoc
                  :is-active               true
                  :timestamp               now
                  :unviewed-messages-count  (calculate-unviewed-message-count cofx message))})

(fx/defn update-last-message [{:keys [db]} {:keys [clock-value chat-id content timestamp content-type]}]
  (let [last-chat-clock-value (get-in db [:chats chat-id :last-message-clock-value])]
    ;; We should also compare message-id in case of clashes, but not sure it's worth
    (when (> clock-value last-chat-clock-value)
      {:db (update-in db [:chats chat-id]
                      assoc
                      :last-message-clock-value  clock-value
                      :last-message-content      content
                      :last-message-timestamp    timestamp
                      :last-message-content-type content-type)})))

(fx/defn receive-one
  [cofx message]
  (when-let [chat-id (extract-chat-id cofx message)]
    (let [message-with-chat-id (assoc message :chat-id chat-id)]
      (when (add-to-chat? cofx message-with-chat-id)
        (fx/merge cofx
                  (chat-model/ensure-chat {:chat-id chat-id})
                  (add-received-message message-with-chat-id)
                  (update-unviewed-count message-with-chat-id)
                  (chat-model/join-time-messages-checked chat-id)
                  (update-last-message message-with-chat-id)
                  (when platform/desktop?
                    (chat-model/update-dock-badge-label))
                  ;; And save chat
                  (chat-model/save-chat-delayed chat-id))))))

(defn system-message [{:keys [now] :as cofx} {:keys [clock-value chat-id content from]}]
  (let [{:keys [last-clock-value]} (get-in cofx [:db :chats chat-id])
        message {:chat-id           chat-id
                 :from              from
                 :timestamp         now
                 :whisper-timestamp now
                 :clock-value       (or clock-value
                                        (utils.clocks/send last-clock-value))
                 :content           content
                 :message-type      :system-message
                 :content-type      constants/content-type-status}]
    (assoc message
           :message-id (transport.utils/system-message-id message))))

(defn group-message? [{:keys [message-type]}]
  (#{:group-user-message :public-group-user-message} message-type))

;;;; Send message

(fx/defn send
  [{{:keys [peers-count]} :db :as cofx} chat-id message send-record]
  (protocol/send send-record chat-id (assoc cofx :message message)))

(defn add-message-type [message {:keys [chat-id group-chat public?]}]
  (cond-> message
    (not group-chat)
    (assoc :message-type :user-message)
    (and group-chat public?)
    (assoc :message-type :public-group-user-message)
    (and group-chat (not public?))
    (assoc :message-type :group-user-message)))

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp :name])

(defn remove-icon
  "Coin's icon's resource is represented as a function,
  can't be properly de/serialised and has to be removed."
  [message]
  (cond-> message
    (get-in message [:content :params :coin :icon :source])
    (update-in [:content :params :coin] dissoc :icon)))

(fx/defn add-message-with-id [cofx message chat-id]
  (when (and message
             (not (get-in cofx [:db :chats chat-id :messages (:message-id message)])))
    (add-message cofx {:batch?           false
                       :message          message
                       :current-chat?    (= (get-in cofx [:db :current-chat-id]) chat-id)})))

(fx/defn prepare-message-content [cofx chat-id message]
  {::json-rpc/call
   [{:method "shhext_prepareContent"
     :params [(:content message)]
     :on-success #(re-frame/dispatch [::prepared-message chat-id message %])
     :on-failure #(log/error "failed to prepare content" %)}]})

(fx/defn prepared-message
  {:events [::prepared-message]}
  [{:keys [now] :as cofx} chat-id message content]
  (let [message-with-content
        (update message :content
                assoc
                :parsed-text  (:parsedText content)
                :line-count (:lineCount content)
                :should-collapse? (message-content/should-collapse?
                                   (:text content)
                                   (:lineCount content))
                :rtl? (:rtl content))
        send-record     (protocol/map->Message
                         (select-keys message-with-content transport-keys))
        wrapped-record  (if (= (:message-type send-record) :group-user-message)
                          (wrap-group-message cofx chat-id send-record)
                          send-record)]
    (fx/merge cofx
              (chat-model/upsert-chat
               {:chat-id                   chat-id
                :timestamp                 now
                :last-message-timestamp    (:timestamp message-with-content)
                :last-message-content      (:content message-with-content)
                :last-message-content-type (:content-type message-with-content)
                :last-clock-value          (:clock-value message-with-content)})
              (send chat-id message-with-content wrapped-record))))

(fx/defn upsert-and-send
  [{:keys [now] :as cofx} {:keys [chat-id from] :as message}]
  (let [message         (remove-icon message)
        message (assoc message :outgoing-status :sending)]

    (prepare-message-content cofx chat-id message)))

(fx/defn update-message-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (fx/merge cofx
            {:db (assoc-in db
                           [:chats chat-id :messages message-id :outgoing-status]
                           status)}
            (messages-store/update-outgoing-status message-id status)))

(fx/defn resend-message
  [cofx chat-id message-id]
  (let [message (get-in cofx [:db :chats chat-id :messages message-id])
        send-record (-> message
                        (select-keys transport-keys)
                        (update :message-type keyword)
                        protocol/map->Message)

        wrapped-record (if (= (:message-type send-record) :group-user-message)
                         (wrap-group-message cofx chat-id send-record)
                         send-record)]
    (fx/merge cofx
              (send chat-id message wrapped-record)
              (update-message-status chat-id message-id :sending))))

(fx/defn rebuild-message-list
  [{:keys [db]} chat-id]
  {:db (assoc-in db [:chats chat-id :message-list]
                 (message-list/add-many nil (vals (get-in db [:chats chat-id :messages]))))})

(fx/defn delete-message
  "Deletes chat message, rebuild message-list"
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {:db            (update-in db [:chats chat-id :messages] dissoc message-id)}
            (messages-store/delete-message message-id)
            (rebuild-message-list chat-id)))

(fx/defn add-system-messages [cofx messages]
  (let [messages-fx (map #(add-message
                           {:batch         false
                            :message       (system-message cofx %)
                            :current-chat? true})
                         messages)]
    (apply fx/merge cofx messages-fx)))

(fx/defn send-message
  [{:keys [db now] :as cofx} {:keys [chat-id] :as message}]
  (let [{:keys [chats]}  db
        {:keys [last-clock-value] :as chat} (get chats chat-id)
        message-data                        (-> message
                                                (assoc :from (multiaccounts.model/current-public-key cofx)
                                                       :timestamp now
                                                       :whisper-timestamp now
                                                       :clock-value (utils.clocks/send
                                                                     last-clock-value))
                                                (tribute-to-talk/add-transaction-hash db)
                                                (add-message-type chat))]
    (upsert-and-send cofx message-data)))

(fx/defn toggle-expand-message
  [{:keys [db]} chat-id message-id]
  {:db (update-in db [:chats chat-id :messages message-id :expanded?] not)})

(fx/defn confirm-message-processed
  [_ raw-message]
  {:transport/confirm-messages-processed [raw-message]})
