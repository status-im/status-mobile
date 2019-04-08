(ns status-im.chat.models.message
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.native-module.core :as status]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.core :as utils]
            [status-im.utils.config :as config]
            [status-im.contact.db :as contact.db]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.datetime :as time]
            [status-im.transport.message.group-chat :as message.group-chat]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.models.message-content :as message-content]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.chat.db :as chat.db]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.money :as money]
            [status-im.utils.types :as types]
            [status-im.notifications.core :as notifications]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.protocol :as protocol]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.data-store.messages :as messages-store]
            [status-im.transport.message.transit :as transit]))

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
    (cond-> (assoc message :appearing? true)
      (not current-chat?)
      (assoc :appearing? false)

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

(fx/defn add-own-status
  [{:keys [db] :as cofx} chat-id message-id status]
  (let [me     (accounts.db/current-public-key cofx)
        status {:chat-id          chat-id
                :message-id       message-id
                :public-key       me
                :status           status}]
    {:db            (assoc-in db
                              [:chats chat-id :message-statuses message-id me]
                              status)
     :data-store/tx [(user-statuses-store/save-status-tx status)]}))

(defn add-outgoing-status [{:keys [from] :as message} current-public-key]
  (assoc message :outgoing (and (= from current-public-key)
                                (not (system-message? message)))))

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
    :keys [current-chat? batch? dedup-id raw-message]}]
  (let [current-public-key (accounts.db/current-public-key cofx)
        prepared-message (-> message
                             (prepare-message chat-id current-chat?)
                             (add-outgoing-status current-public-key))]
    (when (and platform/desktop?
               (not= from current-public-key)
               (get-in db [:account/account :desktop-notifications?])
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
                                           (fnil conj #{}) message-id))
               :data-store/tx [(merge
                                {:transaction (messages-store/save-message-tx prepared-message)}
                                (when (or dedup-id raw-message)
                                  {:success-event
                                   [:message/messages-persisted [(or dedup-id raw-message)]]}))]}
              (when (and platform/desktop?
                         (not batch?)
                         (not (system-message? prepared-message)))
                (chat-model/update-dock-badge-label))
              (when-not batch?
                (re-index-message-groups chat-id))
              (when-not batch?
                (chat-loading/group-chat-messages chat-id [message])))))

(fx/defn send-message-seen
  [cofx chat-id message-id send-seen?]
  (when send-seen?
    (protocol/send (protocol/map->MessagesSeen {:message-ids #{message-id}}) chat-id cofx)))

(defn ensure-clock-value [{:keys [clock-value] :as message} {:keys [last-clock-value]}]
  (if clock-value
    message
    (assoc message :clock-value (utils.clocks/send last-clock-value))))

(defn check-response-to
  [{{:keys [response-to response-to-v2]} :content :as message}
   old-id->message]
  (if (and response-to (not response-to-v2))
    (let [response-to-v2
          (or (get-in old-id->message [response-to :message-id])
              (messages-store/get-message-id-by-old response-to))]
      (assoc-in message [:content :response-to-v2] response-to-v2))
    message))

(fx/defn add-received-message
  [{:keys [db] :as cofx}
   old-id->message
   {:keys [from message-id chat-id js-obj content dedup-id] :as raw-message}]
  (let [{:keys [web3 current-chat-id view-id]} db
        current-public-key            (accounts.db/current-public-key cofx)
        current-chat?                 (and (or (= :chat view-id)
                                               (= :chat-modal view-id))
                                           (= current-chat-id chat-id))
        {:keys [group-chat] :as chat} (get-in db [:chats chat-id])
        message                       (-> raw-message
                                          (commands-receiving/enhance-receive-parameters cofx)
                                          (ensure-clock-value chat)
                                          (check-response-to old-id->message)
                                          ;; TODO (cammellos): Refactor so it's not computed twice
                                          (add-outgoing-status current-public-key))]
    (fx/merge cofx
              (add-message {:batch?       true
                            :message      message
                            :dedup-id     dedup-id
                            :current-chat current-chat?
                            :raw-message  js-obj})
              ;; Checking :outgoing here only works for now as we don't have a :seen
              ;; status for public chats, if we add processing of our own messages
              ;; for 1-to-1 care needs to be taken not to override the :seen status
              (add-own-status chat-id message-id (cond (:outgoing message) :sent
                                                       current-chat? :seen
                                                       :else :received))
              (commands-receiving/receive message)
              ;;TODO(rasom): uncomment when seen messages will be revisited
              #_(send-message-seen chat-id message-id (and (not group-chat)
                                                           current-chat?
                                                           (not (= constants/system from))
                                                           (not (:outgoing message)))))))

(fx/defn update-group-messages [cofx chat->message chat-id]
  (fx/merge cofx
            (re-index-message-groups chat-id)
            (chat-loading/group-chat-messages chat-id (get chat->message chat-id))))

(defn- add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id from]}]
  (let [{:keys [deleted-at-clock-value messages]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (>= deleted-at-clock-value clock-value)
             (messages-store/message-exists? message-id)))))

(defn- filter-messages [cofx messages]
  (:accumulated
   (reduce (fn [{:keys [seen-ids] :as acc}
                {:keys [message-id old-message-id] :as message}]
             (if (and (add-to-chat? cofx message)
                      (not (seen-ids message-id)))
               (-> acc
                   (update :seen-ids conj message-id)
                   (update :accumulated
                           (fn [acc]
                             (-> acc
                                 (update :messages conj message)
                                 (assoc-in [:by-old-message-id old-message-id]
                                           message)))))
               acc))
           {:seen-ids    #{}
            :accumulated {:messages     []
                          :by-old-message-id {}}}
           messages)))

(defn extract-chat-id [cofx {:keys [chat-id from message-type]}]
  "Validate and return a valid chat-id"
  (cond
    (and (= :group-user-message message-type)
         (and (get-in cofx [:db :chats chat-id :contacts from])
              ;; Version 0 does not have a concept of joining, so any message is ok
              ;; otherwise check we joined
              (or (= 0 (get-in cofx [:db :chats chat-id :group-chat-local-version]))
                  (get-in cofx [:db :chats chat-id :members-joined (accounts.db/current-public-key cofx)])))) chat-id
    (and (= :public-group-user-message message-type)
         (get-in cofx [:db :chats chat-id :public?])) chat-id
    (and (= :user-message message-type)
         (= (accounts.db/current-public-key cofx) from)) chat-id
    (= :user-message message-type) from))

(defn calculate-unviewed-messages-count
  [{:keys [db] :as cofx} chat-id messages]
  (let [{:keys [current-chat-id view-id]} db
        chat-view?         (or (= :chat view-id)
                               (= :chat-modal view-id))
        current-public-key (accounts.db/current-public-key cofx)]
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
        {:keys [content content-type clock-value]}
        (->> (chat.db/sort-message-groups message-groups messages)
             last
             second
             last
             :message-id
             (get messages))]
    (chat-model/upsert-chat
     {:chat-id                   chat-id
      :last-message-content      content
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
        old-id->message              (:by-old-message-id filtered-messages)
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
        messages-fx-fns              (map #(add-received-message old-id->message %) deduped-messages)
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
           :old-message-id "system"
           :raw-payload-hash "system")))

(defn group-message? [{:keys [message-type]}]
  (#{:group-user-message :public-group-user-message} message-type))

;;;; Send message

(fx/defn send
  [{{:keys [peers-count]} :db :as cofx} chat-id message-id send-record]
  (if (zero? peers-count)
    {:dispatch-later [{:ms       10000
                       :dispatch [:message/update-message-status chat-id message-id :not-sent]}]}
    (protocol/send send-record chat-id (assoc cofx :message-id message-id))))

(defn add-message-type [message {:keys [chat-id group-chat public?]}]
  (cond-> message
    (not group-chat)
    (assoc :message-type :user-message)
    (and group-chat public?)
    (assoc :message-type :public-group-user-message)
    (and group-chat (not public?))
    (assoc :message-type :group-user-message)))

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp])

(fx/defn upsert-and-send [{:keys [now] :as cofx} {:keys [chat-id from] :as message}]
  (let [send-record     (protocol/map->Message (select-keys message transport-keys))
        old-message-id  (transport.utils/old-message-id send-record)
        wrapped-record  (if (= (:message-type send-record) :group-user-message)
                          (wrap-group-message cofx chat-id send-record)
                          send-record)
        raw-payload     (transport.utils/from-utf8 (transit/serialize wrapped-record))
        message-id      (transport.utils/message-id from raw-payload)
        message-with-id (assoc message
                               :message-id message-id
                               :old-message-id old-message-id
                               :raw-payload-hash (transport.utils/sha3 raw-payload))]

    (fx/merge cofx
              (chat-model/upsert-chat
               {:chat-id                   chat-id
                :timestamp                 now
                :last-message-content      (:content message)
                :last-message-content-type (:content-type message)
                :last-clock-value          (:clock-value message)})
              (add-message {:batch?           false
                            :message          message-with-id
                            :current-chat?    true})
              (add-own-status chat-id message-id :sending)
              (send chat-id message-id wrapped-record))))

(fx/defn send-push-notification [cofx chat-id message-id fcm-tokens status]
  (log/debug "#6772 - send-push-notification" message-id fcm-tokens)
  (when (and (seq fcm-tokens) (= status :sent))
    (let [payload {:from (accounts.db/current-public-key cofx)
                   :to chat-id
                   :id message-id}]
      {:send-notification {:data-payload (notifications/encode-notification-payload payload)
                           :tokens       fcm-tokens}})))

(fx/defn update-message-status [{:keys [db]} chat-id message-id status]
  (let [from           (get-in db [:chats chat-id :messages message-id :from])
        updated-status (-> db
                           (get-in [:chats chat-id :message-statuses message-id from])
                           (assoc :status status))]
    {:db            (assoc-in db
                              [:chats chat-id :message-statuses message-id from]
                              updated-status)
     :data-store/tx [(user-statuses-store/save-status-tx updated-status)]}))

(fx/defn resend-message [cofx chat-id message-id]
  (let [message (get-in cofx [:db :chats chat-id :messages message-id])
        send-record (-> message
                        (select-keys transport-keys)
                        (update :message-type keyword)
                        protocol/map->Message)

        wrapped-record (if (= (:message-type send-record) :group-user-message)
                         (wrap-group-message cofx chat-id send-record)
                         send-record)]
    (fx/merge cofx
              (send chat-id message-id wrapped-record)
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
            {:db            (update-in db [:chats chat-id :messages] dissoc message-id)
             :data-store/tx [(messages-store/delete-message-tx message-id)]}
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
                                                (assoc :from        (accounts.db/current-public-key cofx)
                                                       :timestamp   now
                                                       :clock-value (utils.clocks/send
                                                                     last-clock-value))
                                                (add-message-type chat))]
    (upsert-and-send cofx message-data)))

(fx/defn toggle-expand-message
  [{:keys [db]} chat-id message-id]
  {:db (update-in db [:chats chat-id :messages message-id :expanded?] not)})

(fx/defn confirm-message-processed
  [{:keys [db]} raw-message]
  {:transport/confirm-messages-processed [{:web3 (:web3 db)
                                           :js-obj raw-message}]})

;; effects

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:message/add messages])))

(re-frame/reg-fx
 :send-notification
 (fn [{:keys [data-payload tokens]}]
   "Sends a notification to another device. data-payload is a Clojure map of strings to strings"
   (let [data-payload-json (types/clj->json data-payload)
         tokens-json       (types/clj->json tokens)]
     (log/debug "send-notification data-payload-json:" data-payload-json "tokens-json:" tokens-json)
     ;; NOTE: react-native-firebase doesn't have a good implementation of sendMessage
     ;;       (supporting e.g. priority or content_available properties),
     ;;       therefore we must use an implementation in status-go.
     (status/send-data-notification {:data-payload data-payload-json :tokens tokens-json}
                                    #(log/debug "send-data-notification cb result: " %)))))
