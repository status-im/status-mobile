(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.core :as utils]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.datetime :as time]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.input :as input]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.money :as money]
            [status-im.utils.notifications :as notifications]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.ui.screens.currency-settings.subs :as currency-settings]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            [clojure.string :as string]))

(def receive-interceptors
  [(re-frame/inject-cofx :random-id)
   re-frame/trim-v])

(defn- emoji-only-content?
  [content]
  (and (string? content) (re-matches constants/regx-emoji content)))

(defn- prepare-message
  [{:keys [content] :as message} chat-id current-chat?]
  ;; TODO janherich: enable the animations again once we can do them more efficiently
  (cond-> (assoc message :appearing? true)
    (not current-chat?) (assoc :appearing? false)
    (emoji-only-content? content) (assoc :content-type constants/content-type-emoji)))

(defn- re-index-message-groups
  "Relative datemarks of message groups can get obsolete with passing time,
  this function re-indexes them for given chat"
  [chat-id {:keys [db]}]
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

(defn- sort-references
  "Sorts message-references sequence primary by clock value,
  breaking ties by `:message-id`"
  [messages message-references]
  (sort-by (juxt (comp :clock-value (partial get messages) :message-id)
                 :message-id)
           message-references))

(defn- group-messages
  "Takes chat-id, new messages + cofx and properly groups them
  into the `:message-groups`index in db"
  [chat-id messages {:keys [db]}]
  {:db (reduce (fn [db [datemark grouped-messages]]
                 (update-in db [:chats chat-id :message-groups datemark]
                            (fn [message-references]
                              (->> grouped-messages
                                   (map (fn [{:keys [message-id timestamp]}]
                                          {:message-id    message-id
                                           :timestamp-str (time/timestamp->time timestamp)}))
                                   (into (or message-references '()))
                                   (sort-references (get-in db [:chats chat-id :messages]))))))
               db
               (group-by (comp time/day-relative :timestamp)
                         (filter :show? messages)))})

(defn- add-own-status
  [chat-id message-id status {:keys [db]}]
  (let [me     (:current-public-key db)
        status {:chat-id          chat-id
                :message-id       message-id
                :whisper-identity me
                :status           status}]
    {:db            (assoc-in db
                              [:chats chat-id :message-statuses message-id me]
                              status)
     :data-store/tx [(user-statuses-store/save-status-tx status)]}))

(defn add-outgoing-status [{:keys [from] :as message} {:keys [db]}]
  (assoc message :outgoing (= from (:current-public-key db))))

(defn- add-message
  [batch? {:keys [chat-id message-id clock-value content from] :as message} current-chat? {:keys [db] :as cofx}]
  (let [prepared-message (-> message
                             (prepare-message chat-id current-chat?)
                             (add-outgoing-status cofx))]
(when (and platform/desktop? (not= from (:current-public-key db))) (#(.sendNotification react/desktop-notification content)))
    (let [fx {:db            (cond->
                              (-> db
                                  (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                                  ;; this will increase last-clock-value twice when sending our own messages
                                  (update-in [:chats chat-id :last-clock-value] (partial utils.clocks/receive clock-value)))
                               (not current-chat?)
                               (update-in [:chats chat-id :unviewed-messages] (fnil conj #{}) message-id))
              :data-store/tx [(messages-store/save-message-tx prepared-message)]}]
      (if batch?
        fx
        (handlers-macro/merge-fx cofx
                                 fx
                                 (re-index-message-groups chat-id)
                                 (group-messages chat-id [message]))))))

(def ^:private- add-single-message (partial add-message false))
(def ^:private- add-batch-message (partial add-message true))

(defn- send-message-seen [chat-id message-id send-seen? cofx]
  (when send-seen?
    (transport/send (protocol/map->MessagesSeen {:message-ids #{message-id}}) chat-id cofx)))

(defn ensure-clock-value [{:keys [clock-value] :as message} {:keys [last-clock-value]}]
  (if clock-value
    message
    (assoc message :clock-value (utils.clocks/send last-clock-value))))

(defn- update-legacy-type [{:keys [content-type] :as message}]
  (cond-> message
    (= constants/content-type-command-request content-type)
    (assoc :content-type constants/content-type-command)))

(defn- display-notification [chat-id cofx]
  (when config/in-app-notifications-enabled?
    (let [view-id (get-in cofx [:db :view-id])
          from (get-in cofx [:db :current-public-key])
          current-chat-id (get-in cofx [:db :current-chat-id])]
      (when-not (and (= :chat view-id)
                     (= current-chat-id chat-id))
        {:display-notification-fx {:title (i18n/label :notifications-new-message-title)
                                   :body  (i18n/label :notifications-new-message-body)
                                   :to    chat-id
                                   :from  from}}))))

(defn- add-received-message
  [batch?
   {:keys [from message-id chat-id content content-type clock-value js-obj] :as raw-message}
   {:keys [db now] :as cofx}]
  (let [{:keys [web3 current-chat-id view-id]} db
        current-chat?              (and (= :chat view-id) (= current-chat-id chat-id))
        {:keys [public?] :as chat} (get-in db [:chats chat-id])
        add-message-fn             (if batch? add-batch-message add-single-message)
        message                    (-> raw-message
                                       (ensure-clock-value chat)
                                       ;; TODO (cammellos): Refactor so it's not computed twice
                                       (add-outgoing-status cofx)
                                       ;; TODO (janherich): Remove after couple of releases
                                       update-legacy-type)]
    (handlers-macro/merge-fx cofx
                             {:confirm-messages-processed [{:web3   web3
                                                            :js-obj js-obj}]}
                             (add-message-fn message current-chat?)
                             ;; Checking :outgoing here only works for now as we don't have a :seen
                             ;; status for public chats, if we add processing of our own messages
                             ;; for 1-to-1 care needs to be taken not to override the :seen status
                             (add-own-status chat-id message-id (cond (:outgoing message) :sent
                                                                      current-chat? :seen
                                                                      :else :received))
                             (commands-receiving/receive message)
                             (display-notification chat-id)
                             (send-message-seen chat-id message-id (and (not public?)
                                                                        current-chat?
                                                                        (not (chat-model/bot-only-chat? db chat-id))
                                                                        (not (= constants/system from))
                                                                        (not (:outgoing message)))))))

(def ^:private add-single-received-message (partial add-received-message false))
(def ^:private add-batch-received-message (partial add-received-message true))

(defn receive
  [{:keys [chat-id message-id] :as message} {:keys [now] :as cofx}]
  (handlers-macro/merge-fx cofx
                           (chat-model/upsert-chat {:chat-id   chat-id
                                                    ;; We activate a chat again on new messages
                                                    :is-active true
                                                    :timestamp now})
                           (add-single-received-message message)))

(defn receive-many
  [messages {:keys [now] :as cofx}]
  (let [chat->message   (group-by :chat-id messages)
        chat-ids        (keys chat->message)
        chat-effects    (handlers-macro/merge-effects
                         cofx
                         (fn [chat-id cofx]
                           (chat-model/upsert-chat {:chat-id   chat-id
                                                    :is-active true
                                                    :timestamp now}
                                                   cofx))
                         chat-ids)
        message-effects (handlers-macro/merge-effects
                         chat-effects cofx add-batch-received-message messages)]
    (handlers-macro/merge-effects
     message-effects
     cofx
     (fn [chat-id cofx]
       (handlers-macro/merge-fx cofx
                                (re-index-message-groups chat-id)
                                (group-messages chat-id (get chat->message chat-id))))
     chat-ids)))

(defn system-message [chat-id message-id timestamp content]
  {:message-id   message-id
   :chat-id      chat-id
   :from         constants/system
   :username     constants/system
   :timestamp    timestamp
   :show?        true
   :content      content
   :content-type constants/text-content-type})

(defn group-message? [{:keys [message-type]}]
  (#{:group-user-message :public-group-user-message} message-type))

(defn add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id] :as message}]
  (let [{:keys [deleted-at-clock-value messages not-loaded-message-ids]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (get not-loaded-message-ids message-id)
             (>= deleted-at-clock-value clock-value)))))

;;;; Send message

(def send-interceptors
  [(re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :random-id-seq)
   re-frame/trim-v])

(defn- send
  [chat-id message-id send-record {{:keys [network-status current-public-key]} :db :as cofx}]
  (if (= network-status :offline)
    {:dispatch-later [{:ms       10000
                       :dispatch [:update-message-status chat-id message-id current-public-key :not-sent]}]}
    (transport/send send-record chat-id cofx)))

(defn add-message-type [message {:keys [chat-id group-chat public?]}]
  (cond-> message
    (not group-chat)
    (assoc :message-type :user-message)
    (and group-chat public?)
    (assoc :message-type :public-group-user-message)
    (and group-chat (not public?))
    (assoc :message-type :group-user-message)))

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp])

(defn- upsert-and-send [{:keys [chat-id] :as message} {:keys [now] :as cofx}]
  (let [send-record     (protocol/map->Message (select-keys message transport-keys))
        message-id      (transport.utils/message-id send-record)
        message-with-id (assoc message :message-id message-id)]
    (handlers-macro/merge-fx cofx
                             (input/process-cooldown)
                             (chat-model/upsert-chat {:chat-id chat-id
                                                      :timestamp now})
                             (add-single-message message-with-id true)
                             (add-own-status chat-id message-id :sending)
                             (send chat-id message-id send-record))))

(defn send-push-notification [fcm-token status cofx]
  (when (and fcm-token (= status :sent))
    {:send-notification {:message (js/JSON.stringify #js {:from (get-in cofx [:db :current-public-key])
                                                          :to   (get-in cofx [:db :current-chat-id])})
                         :payload {:title (i18n/label :notifications-new-message-title)
                                   :body  (i18n/label :notifications-new-message-body)
                                   :sound notifications/sound-name}
                         :tokens  [fcm-token]}}))

(defn update-message-status [{:keys [chat-id message-id from]} status {:keys [db]}]
  (let [updated-status (-> db
                           (get-in [:chats chat-id :message-statuses message-id from])
                           (assoc :status status))]
    {:db            (assoc-in db
                              [:chats chat-id :message-statuses message-id from]
                              updated-status)
     :data-store/tx [(user-statuses-store/save-status-tx updated-status)]}))

(defn resend-message [chat-id message-id cofx]
  (let [message (get-in cofx [:db :chats chat-id :messages message-id])
        send-record (-> message
                        (select-keys transport-keys)
                        (update :message-type keyword)
                        protocol/map->Message)]
    (handlers-macro/merge-fx cofx
                             (send chat-id message-id send-record)
                             (update-message-status message :sending))))

(defn- remove-message-from-group [chat-id {:keys [timestamp message-id]} {:keys [db]}]
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

(defn delete-message
  "Deletes chat message, along its occurence in all references, like `:message-groups`"
  [chat-id message-id {:keys [db] :as cofx}]
  (handlers-macro/merge-fx
   cofx
   {:db            (update-in db [:chats chat-id :messages] dissoc message-id)
    :data-store/tx [(messages-store/delete-message-tx message-id)]}
   (remove-message-from-group chat-id (get-in db [:chats chat-id :messages message-id]))))

(defn send-message [{:keys [chat-id] :as message} {:keys [db now] :as cofx}]
  (let [{:keys [current-public-key chats]}  db
        {:keys [last-clock-value] :as chat} (get chats chat-id)
        message-data                        (-> message
                                                (assoc :from        current-public-key
                                                       :timestamp   now
                                                       :clock-value (utils.clocks/send
                                                                     last-clock-value)
                                                       :show?       true)
                                                (add-message-type chat))]
    (upsert-and-send message-data cofx)))
