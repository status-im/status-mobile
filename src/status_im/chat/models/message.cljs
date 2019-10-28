(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.chat.db :as chat.db]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
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
  (let [emoji? (message-content/emoji-only-content? content)]
    ;; TODO janherich: enable the animations again once we can do them more efficiently
    (cond-> message
      current-chat?
      (assoc :seen true)

      emoji?
      (assoc :content-type constants/content-type-emoji)

      (and (= constants/content-type-text content-type) (not emoji?))
      (update :content message-content/enrich-content))))

(defn system-message? [{:keys [message-type]}]
  (= :system-message message-type))

(fx/defn re-index-message-groups
  "Relative datemarks of message groups can get obsolete with passing time,
  this function re-indexes them for given chat"
  [{:keys [db]} chat-id]
  (let [chat-messages (get-in db [:chats chat-id :messages])]
    {:db (update-in db
                    [:chats chat-id :message-groups]
                    (partial reduce-kv (fn [groups datemark message-refs]
                                         (let [new-datemark (->> message-refs
                                                                 first
                                                                 :message-id
                                                                 (get chat-messages)
                                                                 :timestamp
                                                                 time/day-relative)]
                                           (if (= datemark new-datemark)
                                             ;; nothing to re-index
                                             (assoc groups datemark message-refs)
                                             ;; relative datemark shifted, reindex
                                             (assoc groups new-datemark message-refs))))
                             {}))}))

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
    :keys [current-chat? batch? metadata raw-message]}]
  (let [current-public-key (multiaccounts.model/current-public-key cofx)
        prepared-message (-> message
                             (prepare-message chat-id current-chat?)
                             (add-outgoing-status current-public-key))]
    (when (and platform/desktop?
               (not= from current-public-key)
               (get-in db [:multiaccount :desktop-notifications?])
               (< (time/seconds-ago (time/to-date timestamp)) constants/one-earth-day))
      (let [{:keys [title body prioritary?]} (build-desktop-notification cofx message)]
        (.displayNotification react/desktop-notification title body prioritary?)))
    (fx/merge cofx
              {:db            (cond->
                               (-> db
                                   (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                                   (update-in [:chats chat-id :last-clock-value] (partial utils.clocks/receive clock-value)))

                                (and (not current-chat?)
                                     (not= from current-public-key))
                                (update-in [:chats chat-id :loaded-unviewed-messages-ids]
                                           (fnil conj #{}) message-id))}
              #(messages-store/save-message % prepared-message)
              (when (and platform/desktop?
                         (not batch?)
                         (not (system-message? prepared-message)))
                (chat-model/update-dock-badge-label))
              (when-not batch?
                (re-index-message-groups chat-id))
              (when-not batch?
                (chat-loading/group-chat-messages chat-id [message])))))

(defn ensure-clock-value [{:keys [clock-value] :as message} {:keys [last-clock-value]}]
  (if clock-value
    message
    (assoc message :clock-value (utils.clocks/send last-clock-value))))

(fx/defn add-received-message
  [{:keys [db] :as cofx}
   {:keys [from message-id chat-id js-obj content metadata] :as raw-message}]
  (let [{:keys [current-chat-id view-id]} db
        current-public-key             (multiaccounts.model/current-public-key cofx)
        current-chat?                  (and (or (= :chat view-id)
                                                (= :chat-modal view-id))
                                            (= current-chat-id chat-id))
        {:keys [group-chat] :as chat}  (get-in db [:chats chat-id])
        {:keys [outgoing] :as message} (-> raw-message
                                           (commands-receiving/enhance-receive-parameters cofx)
                                           (ensure-clock-value chat)
                                           ;; TODO (cammellos): Refactor so it's not computed twice
                                           (add-outgoing-status current-public-key))]
    (fx/merge cofx
              (add-message {:batch?       true
                            :message      message
                            :metadata     metadata
                            :current-chat current-chat?
                            :raw-message  js-obj})
              (commands-receiving/receive message))))

(fx/defn update-group-messages [cofx chat->message chat-id]
  (fx/merge cofx
            (re-index-message-groups chat-id)
            (chat-loading/group-chat-messages chat-id (get chat->message chat-id))))

(defn- add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id from]}]
  (let [{:keys [deleted-at-clock-value messages]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (>= deleted-at-clock-value clock-value)))))

(defn- filter-messages [cofx messages]
  (:accumulated
   (reduce (fn [{:keys [seen-ids] :as acc}
                {:keys [message-id] :as message}]
             (if (and (add-to-chat? cofx message)
                      (not (seen-ids message-id)))
               (-> acc
                   (update :seen-ids conj message-id)
                   (update :accumulated
                           (fn [acc]
                             (update acc :messages conj message))))
               acc))
           {:seen-ids    #{}
            :accumulated {:messages     []}}
           messages)))

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

(defn calculate-unviewed-messages-count
  [{:keys [db] :as cofx} chat-id messages]
  (let [{:keys [current-chat-id view-id]} db
        chat-view?         (or (= :chat view-id)
                               (= :chat-modal view-id))
        current-public-key (multiaccounts.model/current-public-key cofx)]
    (+ (get-in db [:chats chat-id :unviewed-messages-count])
       (if (and chat-view? (= current-chat-id chat-id))
         0
         (count (remove
                 (fn [{:keys [from]}]
                   (= from current-public-key))
                 messages))))))

(defn- update-last-message [all-chats chat-id]
  (let [{:keys [messages message-groups]}
        (get all-chats chat-id)
        {:keys [content content-type clock-value timestamp]}
        (->> (chat.db/sort-message-groups message-groups messages)
             last
             second
             last
             :message-id
             (get messages))]
    (chat-model/upsert-chat
     {:chat-id                   chat-id
      :last-message-content      content
      :last-message-timestamp    timestamp
      :last-message-content-type content-type})))

(fx/defn update-last-messages
  [{:keys [db] :as cofx} chat-ids]
  (apply fx/merge cofx
         (map (partial update-last-message (:chats db)) chat-ids)))

(fx/defn declare-syncd-public-chats!
  [cofx chat-ids]
  (apply fx/merge cofx
         (map (partial chat-model/join-time-messages-checked cofx) chat-ids)))

(defn- chat-ids->never-synced-public-chat-ids [chats chat-ids]
  (let [never-synced-public-chat-ids (mailserver/chats->never-synced-public-chats chats)]
    (when (seq never-synced-public-chat-ids)
      (-> never-synced-public-chat-ids
          (select-keys (vec chat-ids))
          keys))))

(fx/defn receive-many
  [{:keys [now] :as cofx} messages]
  (let [valid-messages               (keep #(when-let [chat-id (extract-chat-id cofx %)]
                                              (assoc % :chat-id chat-id)) messages)
        filtered-messages            (filter-messages cofx valid-messages)
        deduped-messages             (:messages filtered-messages)
        chat->message                (group-by :chat-id deduped-messages)
        chat-ids                     (keys chat->message)
        never-synced-public-chat-ids (chat-ids->never-synced-public-chat-ids
                                      (get-in cofx [:db :chats]) chat-ids)
        chats-fx-fns                 (map (fn [chat-id]
                                            (let [unviewed-messages-count
                                                  (calculate-unviewed-messages-count
                                                   cofx
                                                   chat-id
                                                   (get chat->message chat-id))]
                                              (chat-model/upsert-chat
                                               {:chat-id                 chat-id
                                                :is-active               true
                                                :timestamp               now
                                                :unviewed-messages-count unviewed-messages-count})))
                                          chat-ids)
        messages-fx-fns              (map add-received-message deduped-messages)
        groups-fx-fns                (map #(update-group-messages chat->message %) chat-ids)]
    (apply fx/merge cofx (concat chats-fx-fns
                                 messages-fx-fns
                                 groups-fx-fns
                                 (when platform/desktop?
                                   [(chat-model/update-dock-badge-label)])
                                 [(update-last-messages chat-ids)]
                                 (when (seq never-synced-public-chat-ids)
                                   [(declare-syncd-public-chats! never-synced-public-chat-ids)])))))

(defn system-message [{:keys [now] :as cofx} {:keys [clock-value chat-id content from]}]
  (let [{:keys [last-clock-value]} (get-in cofx [:db :chats chat-id])
        message {:chat-id      chat-id
                 :from         from
                 :timestamp    now
                 :clock-value  (or clock-value
                                   (utils.clocks/send last-clock-value))
                 :content      content
                 :message-type :system-message
                 :content-type constants/content-type-status}]
    (assoc message
           :message-id (transport.utils/system-message-id message)
           :raw-payload-hash "system")))

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

(fx/defn upsert-and-send
  [{:keys [now] :as cofx} {:keys [chat-id from] :as message}]
  (let [message         (remove-icon message)
        send-record     (protocol/map->Message (select-keys message transport-keys))
        wrapped-record  (if (= (:message-type send-record) :group-user-message)
                          (wrap-group-message cofx chat-id send-record)
                          send-record)
        message (assoc message :outgoing-status :sending)]

    (fx/merge cofx
              (chat-model/upsert-chat
               {:chat-id                   chat-id
                :timestamp                 now
                :last-message-timestamp    (:timestamp message)
                :last-message-content      (:content message)
                :last-message-content-type (:content-type message)
                :last-clock-value          (:clock-value message)})
              (send chat-id message wrapped-record))))

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

(fx/defn remove-message-from-group
  [{:keys [db]} chat-id {:keys [timestamp message-id]}]
  (let [datemark (time/day-relative timestamp)]
    {:db (update-in db [:chats chat-id :message-groups]
                    (fn [groups]
                      (let [message-references (get groups datemark)]
                        (if (= 1 (count message-references))
                          ;; message removed is the only one in group, remove whole group
                          (dissoc groups datemark)
                          ;; remove message from `message-references` list
                          (assoc groups datemark
                                 (remove (comp (partial = message-id) :message-id)
                                         message-references))))))}))

(fx/defn delete-message
  "Deletes chat message, along its occurence in all references, like `:message-groups`"
  [{:keys [db] :as cofx} chat-id message-id]
  (fx/merge cofx
            {:db            (update-in db [:chats chat-id :messages] dissoc message-id)}
            (messages-store/delete-message message-id)
            (remove-message-from-group chat-id (get-in db [:chats chat-id :messages message-id]))))

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
                                                       :whisper-timestamp (quot now 1000)
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

;; effects

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:message/add messages])))
