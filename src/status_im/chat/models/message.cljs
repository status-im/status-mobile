(ns status-im.chat.models.message
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.utils.core :as utils]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.datetime :as time]
            [status-im.group-chats.core :as group-chats]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.money :as money]
            [status-im.utils.types :as types]
            [status-im.notifications.core :as notifications]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.data-store.messages :as messages-store]
            [status-im.data-store.user-statuses :as user-statuses-store]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn- emoji-only-content?
  [content]
  (and (string? content) (re-matches constants/regx-emoji content)))

(defn- prepare-message
  [{:keys [content] :as message} chat-id current-chat?]
  ;; TODO janherich: enable the animations again once we can do them more efficiently
  (cond-> (assoc message :appearing? true)
    (not current-chat?) (assoc :appearing? false)
    (emoji-only-content? content) (assoc :content-type constants/content-type-emoji)))

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
  [{:keys [db]} chat-id message-id status]
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

(fx/defn add-message
  [{:keys [db] :as cofx} batch? {:keys [chat-id message-id clock-value] :as message} current-chat?]
  (let [prepared-message (-> message
                             (prepare-message chat-id current-chat?)
                             (add-outgoing-status cofx))]
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
        (fx/merge cofx
                  fx
                  (re-index-message-groups chat-id)
                  (chat-loading/group-chat-messages chat-id [message]))))))

(fx/defn send-message-seen
  [cofx chat-id message-id send-seen?]
  (when send-seen?
    (transport/send (protocol/map->MessagesSeen {:message-ids #{message-id}}) chat-id cofx)))

(defn ensure-clock-value [{:keys [clock-value] :as message} {:keys [last-clock-value]}]
  (if clock-value
    message
    (assoc message :clock-value (utils.clocks/send last-clock-value))))

(defn- update-legacy-data [{:keys [content-type content] :as message}]
  (cond-> message
    (= constants/content-type-command-request content-type)
    (assoc :content-type constants/content-type-command)))

(fx/defn display-notification
  [cofx chat-id]
  (when config/in-app-notifications-enabled?
    (let [view-id (get-in cofx [:db :view-id])
          from (get-in cofx [:db :current-public-key])
          current-chat-id (get-in cofx [:db :current-chat-id])]
      (when-not (and (= :chat view-id)
                     (= current-chat-id chat-id))
        {:notifications/display-notification {:title (i18n/label :notifications-new-message-title)
                                              :body  (i18n/label :notifications-new-message-body)
                                              :to    chat-id
                                              :from  from}}))))

(fx/defn add-received-message
  [{:keys [db now] :as cofx}
   batch?
   {:keys [from message-id chat-id content content-type clock-value js-obj] :as raw-message}]
  (let [{:keys [web3 current-chat-id view-id]} db
        current-chat?                 (and (or (= :chat view-id) (= :chat-modal view-id)) (= current-chat-id chat-id))
        {:keys [group-chat] :as chat} (get-in db [:chats chat-id])
        message                       (-> raw-message
                                          (commands-receiving/enhance-receive-parameters cofx)
                                          (ensure-clock-value chat)
                                          ;; TODO (cammellos): Refactor so it's not computed twice
                                          (add-outgoing-status cofx)
                                          ;; TODO (janherich): Remove after couple of releases
                                          update-legacy-data)]
    (fx/merge cofx
              {:confirm-messages-processed [{:web3   web3
                                             :js-obj js-obj}]}
              (add-message batch? message current-chat?)
              ;; Checking :outgoing here only works for now as we don't have a :seen
              ;; status for public chats, if we add processing of our own messages
              ;; for 1-to-1 care needs to be taken not to override the :seen status
              (add-own-status chat-id message-id (cond (:outgoing message) :sent
                                                       current-chat? :seen
                                                       :else :received))
              (commands-receiving/receive message)
              (display-notification chat-id)
              (send-message-seen chat-id message-id (and (not group-chat)
                                                         current-chat?
                                                         (not (= constants/system from))
                                                         (not (:outgoing message)))))))

(fx/defn update-group-messages [cofx chat->message chat-id]
  (fx/merge cofx
            (re-index-message-groups chat-id)
            (chat-loading/group-chat-messages chat-id (get chat->message chat-id))))

(defn- add-to-chat?
  [{:keys [db]} {:keys [chat-id clock-value message-id] :as message}]
  (let [{:keys [deleted-at-clock-value messages not-loaded-message-ids]}
        (get-in db [:chats chat-id])]
    (not (or (get messages message-id)
             (get not-loaded-message-ids message-id)
             (>= deleted-at-clock-value clock-value)))))

(defn- filter-messages [cofx messages]
  (:accumulated (reduce (fn [{:keys [seen-ids] :as acc}
                             {:keys [message-id] :as message}]
                          (if (and (add-to-chat? cofx message)
                                   (not (seen-ids message-id)))
                            (-> acc
                                (update :seen-ids conj message-id)
                                (update :accumulated conj message))
                            acc))
                        {:seen-ids    #{}
                         :accumulated []}
                        messages)))

(defn valid-chat-id? [cofx {:keys [chat-id from message-type]}]
  "Validate chat-id and message-type"
  (case message-type
    :group-user-message (get-in cofx [:db :chats chat-id :contacts from])
    :public-group-user-message (get-in cofx [:db :chats chat-id :public?])
    :user-message (or (= (get-in cofx [:db :current-public-key]) from)
                      (= chat-id from))
    false))

(fx/defn receive-many
  [{:keys [now] :as cofx} messages]
  (let [valid-messages   (filter (partial valid-chat-id? cofx) messages)
        deduped-messages (filter-messages cofx valid-messages)
        chat->message    (group-by :chat-id deduped-messages)
        chat-ids         (keys chat->message)
        chats-fx-fns     (map #(chat-model/upsert-chat {:chat-id   %
                                                        :is-active true
                                                        :timestamp now})
                              chat-ids)
        messages-fx-fns (map #(add-received-message true %) deduped-messages)
        groups-fx-fns   (map #(update-group-messages chat->message %) chat-ids)]
    (apply fx/merge cofx (concat chats-fx-fns messages-fx-fns groups-fx-fns))))

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

;;;; Send message

(fx/defn send
  [{{:keys [network-status]} :db :as cofx} chat-id message-id send-record]
  (if (= network-status :offline)
    {:dispatch-later [{:ms       10000
                       :dispatch [:message/update-message-status chat-id message-id :not-sent]}]}
    (let [wrapped-record (if (= (:message-type send-record) :group-user-message)
                           (group-chats/wrap-group-message cofx chat-id send-record)
                           send-record)]

      (transport/send wrapped-record chat-id cofx))))

(defn add-message-type [message {:keys [chat-id group-chat public?]}]
  (cond-> message
    (not group-chat)
    (assoc :message-type :user-message)
    (and group-chat public?)
    (assoc :message-type :public-group-user-message)
    (and group-chat (not public?))
    (assoc :message-type :group-user-message)))

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp])

(fx/defn upsert-and-send [{:keys [now] :as cofx} {:keys [chat-id] :as message}]
  (let [send-record     (protocol/map->Message (select-keys message transport-keys))
        message-id      (transport.utils/message-id send-record)
        message-with-id (assoc message :message-id message-id)]
    (fx/merge cofx
              (chat-model/upsert-chat {:chat-id chat-id
                                       :timestamp now})
              (add-message false message-with-id true)
              (add-own-status chat-id message-id :sending)
              (send chat-id message-id send-record))))

(fx/defn send-push-notification [cofx fcm-token status]
  (when (and fcm-token (= status :sent))
    {:send-notification {:message (js/JSON.stringify #js {:from (get-in cofx [:db :current-public-key])
                                                          :to   (get-in cofx [:db :current-chat-id])})
                         :payload {:title (i18n/label :notifications-new-message-title)
                                   :body  (i18n/label :notifications-new-message-body)
                                   :sound notifications/sound-name}
                         :tokens  [fcm-token]}}))

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
                        protocol/map->Message)]
    (fx/merge cofx
              (send chat-id message-id send-record)
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

(fx/defn send-message
  [{:keys [db now] :as cofx} {:keys [chat-id] :as message}]
  (let [{:keys [current-public-key chats]}  db
        {:keys [last-clock-value] :as chat} (get chats chat-id)
        message-data                        (-> message
                                                (assoc :from        current-public-key
                                                       :timestamp   now
                                                       :clock-value (utils.clocks/send
                                                                     last-clock-value)
                                                       :show?       true)
                                                (add-message-type chat))]
    (upsert-and-send cofx message-data)))

;; effects

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:message/add messages])))

(re-frame/reg-fx
 :send-notification
 (fn [{:keys [message payload tokens]}]
   (let [payload-json (types/clj->json payload)
         tokens-json  (types/clj->json tokens)]
     (log/debug "send-notification message: " message " payload-json: " payload-json " tokens-json: " tokens-json)
     (status/notify-users {:message message :payload payload-json :tokens tokens-json} #(log/debug "send-notification cb result: " %)))))
