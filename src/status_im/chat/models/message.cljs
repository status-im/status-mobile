(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.core :as utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.datetime :as time]
            [status-im.chat.events.console :as console-events]
            [status-im.chat.events.requests :as requests-events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.protocol :as protocol]
            [status-im.data-store.messages :as messages-store]))

(def receive-interceptors
  [(re-frame/inject-cofx :random-id)
   re-frame/trim-v])

(defn- lookup-response-ref
  [access-scope->commands-responses account chat contacts response-name]
  (let [available-commands-responses (commands-model/commands-responses :response
                                                                        access-scope->commands-responses
                                                                        account
                                                                        chat
                                                                        contacts)]
    (:ref (get available-commands-responses response-name))))

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

(defn- add-message
  [batch? {:keys [chat-id message-id clock-value content] :as message} current-chat? {:keys [db] :as cofx}]
  (let [prepared-message (prepare-message message chat-id current-chat?)]
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

(defn- add-received-message
  [batch?
   {:keys [from message-id chat-id content content-type timestamp clock-value to-clock-value js-obj] :as message}
   {:keys [db now] :as cofx}]
  (let [{:keys [web3
                current-chat-id
                view-id
                access-scope->commands-responses]
         :contacts/keys [contacts]}               db
        {:keys [public-key] :as current-account}  (:account/account db)
        current-chat?                             (and (= :chat view-id) (= current-chat-id chat-id))
        {:keys [last-clock-value
                public?] :as chat}                (get-in db [:chats chat-id])
        request-command                           (:request-command content)
        command-request?                          (and (= content-type constants/content-type-command-request)
                                                       request-command)
        add-message-fn                            (if batch? add-batch-message add-single-message)]
    (handlers-macro/merge-fx cofx
                             {:confirm-messages-processed [{:web3   web3
                                                            :js-obj js-obj}]}
                             (add-message-fn (cond-> message
                                               public-key
                                               (assoc :user-statuses {public-key (if current-chat? :seen :received)})
                                               (not clock-value)
                                               (assoc :clock-value (utils.clocks/send last-clock-value)) ; TODO (cammeelos): for backward compatibility, we use received time to be removed when not an issue anymore
                                               command-request?
                                               (assoc-in [:content :request-command-ref]
                                                         (lookup-response-ref access-scope->commands-responses
                                                                              current-account chat contacts request-command)))
                                             current-chat?)
                             (requests-events/add-request chat-id message-id)
                             (send-message-seen chat-id message-id (and public-key
                                                                        (not public?)
                                                                        current-chat?
                                                                        (not (chat-model/bot-only-chat? db chat-id))
                                                                        (not (= constants/system from)))))))

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
  (let [chat-ids        (into #{} (map :chat-id) messages)
        chat-effects    (handlers-macro/merge-effects cofx
                                                      (fn [chat-id cofx]
                                                        (chat-model/upsert-chat {:chat-id   chat-id
                                                                                 :is-active true
                                                                                 :timestamp now}
                                                                                cofx))
                                                      chat-ids)
        message-effects (handlers-macro/merge-effects chat-effects cofx add-batch-received-message messages)]
    (handlers-macro/merge-effects message-effects
                                  cofx
                                  (fn [chat-id cofx]
                                    (handlers-macro/merge-fx cofx
                                                             (re-index-message-groups chat-id)
                                                             (group-messages chat-id messages)))
                                  chat-ids)))

(defn system-message [chat-id message-id timestamp content]
  {:message-id   message-id
   :outgoing     false
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

(defn message-seen-by? [message user-pk]
  (= :seen (get-in message [:user-statuses user-pk])))

;;;; Send message

(def send-interceptors
  [(re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :random-id-seq)
   re-frame/trim-v])

(defn- handle-message-from-bot [{:keys [random-id] :as cofx} {:keys [message chat-id]}]
  (when-let [message (cond
                       (string? message)
                       {:message-id   random-id
                        :content      (str message)
                        :content-type constants/text-content-type
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"}

                       (= "request" (:type message))
                       {:message-id   random-id
                        :content      (assoc (:content message) :bot chat-id)
                        :content-type constants/content-type-command-request
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"})]
    (receive message cofx)))

(defn- send-dapp-message!
  [{:keys [db] :as cofx} chat-id {:keys [content-type] :as message}]
  (if (= content-type constants/content-type-command)
    (when-let [text-message (get-in message [:content :handler-data :text-message])]
      (handle-message-from-bot cofx {:message text-message
                                     :chat-id chat-id}))
    (let [data (get-in db [:local-storage chat-id])]
      {:call-jail-function {:chat-id    chat-id
                            :function   :on-message-send
                            :parameters {:message (:content message)}
                            :context    {:data data
                                         :from (get-in db [:account/account :address])}}})))

(defn- send
  [chat-id message-id send-record {{:contacts/keys [contacts] :keys [network-status current-public-key]} :db :as cofx}]
  (let [{:keys [dapp?]} (get contacts chat-id)]
    (if dapp?
      (send-dapp-message! cofx chat-id send-record)
      (if (= network-status :offline)
        {:dispatch-later [{:ms       10000
                           :dispatch [:update-message-status chat-id message-id current-public-key :not-sent]}]}
        (transport/send send-record chat-id cofx)))))

(defn add-message-type [message {:keys [chat-id group-chat public?]}]
  (cond-> message
    (not group-chat)
    (assoc :message-type :user-message)
    (and group-chat public?)
    (assoc :message-type :public-group-user-message)
    (and group-chat (not public?))
    (assoc :message-type :group-user-message)))

(defn- prepare-plain-message [chat-id {:keys [identity message-text]}
                              {:keys [last-clock-value] :as chat} now]
  (add-message-type {:chat-id          chat-id
                     :content          message-text
                     :from             identity
                     :content-type     constants/text-content-type
                     :outgoing         true
                     :timestamp        now
                     :clock-value     (utils.clocks/send last-clock-value)
                     :show?            true
                     :user-statuses   {identity :sending}}
                    chat))

(def ^:private transport-keys [:content :content-type :message-type :clock-value :timestamp])

(defn- upsert-and-send [{:keys [chat-id] :as message} {:keys [now] :as cofx}]
  (let [send-record     (protocol/map->Message (select-keys message transport-keys))
        message-id      (transport.utils/message-id send-record)
        message-with-id (assoc message :message-id message-id)]
    (handlers-macro/merge-fx cofx
                             (chat-model/upsert-chat {:chat-id chat-id
                                                      :timestamp now})
                             (add-single-message message-with-id true)
                             (send chat-id message-id send-record))))

(defn send-push-notification [fcm-token status cofx]
  (when (and fcm-token (= status :sent))
    {:send-notification {:message "message"
                         :payload {:title "Status" :body "You have a new message"}
                         :tokens  [fcm-token]}}))

(defn update-message-status [{:keys [chat-id message-id from] :as message} status {:keys [db]}]
  (let [updated-message (assoc-in message [:user-statuses from] status)]
    {:db            (assoc-in db [:chats chat-id :messages message-id] updated-message)
     :data-store/tx [(messages-store/update-message-tx updated-message)]}))

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

(defn send-message [{:keys [db now random-id] :as cofx} {:keys [chat-id] :as params}]
  (upsert-and-send (prepare-plain-message chat-id params (get-in db [:chats chat-id]) now) cofx))

(defn- prepare-command-message
  [identity
   {:keys [chat-id last-clock-value] :as chat}
   now
   {request-params  :params
    request-command :command
    :keys           [prefill prefillBotDb]
    :as             request}
   {:keys [params command handler-data content-type]}
   network]
  (let [content (if request
                  {:request-command     request-command
                   ;; TODO janherich this is technically not correct, but works for now
                   :request-command-ref (:ref command)
                   :params              (assoc request-params :bot-db (:bot-db params))
                   :prefill             prefill
                   :prefill-bot-db      prefillBotDb}
                  {:params (cond-> params
                             (= (:name command) "send")
                             (assoc :network (ethereum/network-names network)))})
        content' (assoc content
                        :command               (:name command)
                        :handler-data          handler-data
                        :type                  (name (:type command))
                        :command-scope-bitmask (:scope-bitmask command)
                        :command-ref           (:ref command)
                        :preview               (:preview command)
                        :short-preview         (:short-preview command)
                        :bot (:owner-id command))]
    (add-message-type {:chat-id          chat-id
                       :from             identity
                       :timestamp        now
                       :content          content'
                       :content-type     (or content-type
                                             (if request
                                               constants/content-type-command-request
                                               constants/content-type-command))
                       :outgoing         true
                       :clock-value      (utils.clocks/send last-clock-value)
                       :show?            true}
                      chat)))

(defn send-command
  [{{:keys [current-public-key chats network] :as db} :db :keys [now] :as cofx} params]
  (let [{{:keys [handler-data to-message command] :as content} :command chat-id :chat-id} params
        ;; We send commands to deleted chats as well, i.e. signed later transactions
        chat    (or (get chats chat-id) {:chat-id chat-id})
        request (:request handler-data)]
    (handlers-macro/merge-fx cofx
                             (upsert-and-send (prepare-command-message current-public-key chat now request content network))
                             (console-events/console-respond-command-messages command handler-data)
                             (requests-events/request-answered chat-id to-message))))

(defn invoke-console-command-handler
  [{:keys [db] :as cofx} {:keys [command] :as command-params}]
  (let [fx-fn (get console-events/console-commands->fx (-> command :command :name))
        fx    (fx-fn cofx command)]
    (let [command (send-command (assoc cofx :db (or (:db fx) db)) command-params)
          dn      (concat (:dispatch-n fx) (:dispatch-n command))]
      ;; Make sure `dispatch-n` do not collide
      ;; TODO (jeluard) Refactor to rely on merge-fx and reduce creation of `dispatch-n`
      (merge {:dispatch-n dn} (dissoc fx :dispatch-n) (dissoc command :dispatch-n)))))

(defn invoke-command-handlers
  [{{:contacts/keys [contacts] :keys [network] :as db} :db}
   {{:keys [command params id]} :command
    :keys [chat-id address]
    :as orig-params}]
  (let [{:keys [type name scope-bitmask bot owner-id]} command
        handler-type (if (= :command type) :commands :responses)
        to           (get-in contacts [chat-id :address])
        identity     (or owner-id bot chat-id)
        ;; TODO what's actually semantic difference between `:parameters` and `:context`
        ;; and do we have some clear API for both ? seems very messy and unorganized now
        jail-params  {:parameters params
                      :context    (cond-> {:from            address
                                           :to              to
                                           :current-account (get db :account/account)
                                           :network         (ethereum/network-names network)
                                           :message-id      id}
                                    (:async-handler command)
                                    (assoc :orig-params orig-params))}]
    {:call-jail [{:jail-id                identity
                  :path                   [handler-type [name scope-bitmask] :handler]
                  :params                 jail-params
                  :callback-event-creator (fn [jail-response]
                                            (when-not (:async-handler command)
                                              [:command-handler! chat-id orig-params jail-response]))}]}))

(defn process-command
  [cofx {:keys [command chat-id] :as params}]
  (let [{:keys [command]} command]
    (cond
      (and (= constants/console-chat-id chat-id)
           (console-events/commands-names (:name command)))
      (invoke-console-command-handler cofx params)

      (:has-handler command)
      (invoke-command-handlers cofx params)

      :else
      (send-command cofx params))))
