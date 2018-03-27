(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.core :as utils]
            [status-im.chat.events.console :as console-events]
            [status-im.chat.events.requests :as requests-events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.utils.clocks :as clocks-utils]
            [status-im.utils.handlers :as handlers]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as transport]
            [status-im.transport.message.v1.protocol :as protocol]))

(def receive-interceptors
  [(re-frame/inject-cofx :data-store/get-message) (re-frame/inject-cofx :data-store/get-chat)
   (re-frame/inject-cofx :random-id) re-frame/trim-v])

(defn- lookup-response-ref
  [access-scope->commands-responses account chat contacts response-name]
  (let [available-commands-responses (commands-model/commands-responses :response
                                                                        access-scope->commands-responses
                                                                        account
                                                                        chat
                                                                        contacts)]
    (:ref (get available-commands-responses response-name))))

(defn- add-message
  [chat-id {:keys [message-id from-clock-value to-clock-value] :as message} current-chat? {:keys [db]}]
  (let [prepared-message (cond-> (assoc message :appearing? true)
                           (not current-chat?)
                           (assoc :appearing? false))]
    {:db                    (cond-> (-> db
                                        (update-in [:chats chat-id :messages] dissoc from-clock-value)
                                        (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                                        (update-in [:chats chat-id :last-from-clock-value] max from-clock-value)
                                        (update-in [:chats chat-id :last-to-clock-value] max to-clock-value))
                              (not current-chat?)
                              (update-in [:chats chat-id :unviewed-messages] (fnil conj #{}) message-id))
     :data-store/save-message prepared-message}))

;; We start with [0 0] ([from-clock-value to-clock-value]) for each participant of 1-1 chat (local perspective on each device).
;; Now for sending, we always increment the to-clock-value and include it in message payload being sent (so only to-clock-value is present in network message).
;; Locally, the sent message always replicates the latest-from-clock-value of the chat.
;; Upon receiving message, receiver reads the to-clock-value of the received message and sets that to be the from-clock-value locally
;; (this will be also the new latest-from-clock-value of the chat), to-clock-value for the message is the latest-to-clock-value of the 1-1 chat`.

;; All this ensures, that there will be no [from-clock-value to-clock-value] duplicate in chat message on each device + the local order will appear consistent,
;; even if it’s possible it won’t be the same on both devices (for example user A sends 5 messages, during the sending,
;; he receives the message from user B, so his local order will be A1, A2, B, A3, A4, A5, but his messages will take a long time to reach user B,
;; for some reason, so user B will see it as B, A1, A2, A3, A4, A5).
;; I don’t think that’s very problematic and I don’t think we can do much about it without single source of truth where order received messages are serialised
;; and definite order is established (server), it is the case even in the current implementation.
(defn- prepare-chat [chat-id {:keys [db] :as cofx}]
  (if (get-in db [:chats chat-id])
    (chat-model/upsert-chat {:chat-id chat-id} cofx)
    (chat-model/add-chat chat-id cofx)))

(defn- get-current-account [{:accounts/keys [accounts current-account-id]}]
  (get accounts current-account-id))

(defn- send-message-seen [chat-id message-id send-seen? cofx]
  (when send-seen?
    (transport/send (protocol/map->MessagesSeen {:message-ids #{message-id}}) chat-id cofx)))

(defn- placeholder-message [chat-id from timestamp temp-id to-clock]
  {:message-id       temp-id
   :outgoing         false
   :chat-id          chat-id
   :from             from
   :to               "me"
   :content          "Waiting for message to arrive..."
   :content-type     constants/content-type-placeholder
   :show?            true
   :from-clock-value temp-id
   :to-clock-value   to-clock
   :timestamp        timestamp})

(defn- add-placeholder-messages [chat-id from timestamp old-from-clock to-clock new-from-clock {:keys [db]}]
  (when (> (- new-from-clock old-from-clock) 1)
    {:db (reduce (fn [db temp-id]
                   (assoc-in db [:chats chat-id :messages temp-id] (placeholder-message chat-id from timestamp temp-id to-clock)))
                 db
                 (range (inc old-from-clock) new-from-clock))}))

(defn- add-received-message
  [{:keys [from message-id chat-id content content-type timestamp to-clock-value] :as message}
   {:keys [db now] :as cofx}]
  (let [{:keys [current-chat-id
                view-id
                access-scope->commands-responses]
         :contacts/keys [contacts]}               db
        {:keys [public-key] :as current-account}  (get-current-account db)
        current-chat?                             (and (= :chat view-id) (= current-chat-id chat-id))
        {:keys [last-from-clock-value
                last-to-clock-value] :as chat}    (get-in db [:chats chat-id])
        request-command                           (:request-command content)
        command-request?                          (and (= content-type constants/content-type-command-request)
                                                       request-command)
        new-from-clock-value                      (or to-clock-value (inc last-from-clock-value))
        new-timestamp                             (or timestamp now)]
    (handlers/merge-fx cofx
                       (add-message chat-id
                                    (cond-> (assoc message
                                                   :timestamp        new-timestamp
                                                   :show?            true
                                                   :from-clock-value new-from-clock-value
                                                   :to-clock-value   last-to-clock-value)
                                      public-key
                                      (assoc :user-statuses {public-key (if current-chat? :seen :received)})
                                      command-request?
                                      (assoc-in [:content :request-command-ref]
                                                (lookup-response-ref access-scope->commands-responses
                                                                     current-account chat contacts request-command)))
                                    current-chat?)
                       (send-message-seen chat-id message-id (and public-key
                                                                  current-chat?
                                                                  (not (chat-model/bot-only-chat? db chat-id))
                                                                  (not (= constants/system from))))
                       (add-placeholder-messages chat-id from new-timestamp last-from-clock-value last-to-clock-value new-from-clock-value))))

(defn receive
  [{:keys [chat-id message-id] :as message} cofx]
  (handlers/merge-fx cofx
                     (prepare-chat chat-id)
                     (add-received-message message)
                     (requests-events/add-request chat-id message-id)))

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
  [{:keys [db get-stored-message]} {:keys [chat-id from message-id] :as message}]
  (let [{:keys [chats deleted-chats current-public-key]} db
        {:keys [messages not-loaded-message-ids]}        (get chats chat-id)]
    (when (not= from current-public-key)
      (if (group-message? message)
        (not (or (get deleted-chats chat-id)
                 (get messages message-id)
                 (get not-loaded-message-ids message-id)))
        (not (or (get messages message-id)
                 (get not-loaded-message-ids message-id)
                 (and (get deleted-chats chat-id)
                      (get-stored-message message-id))))))))

(defn message-seen-by? [message user-pk]
  (= :seen (get-in message [:user-statuses user-pk])))

;;;; Send message

(def send-interceptors
  [(re-frame/inject-cofx :random-id) (re-frame/inject-cofx :random-id-seq)
   (re-frame/inject-cofx :data-store/get-chat) re-frame/trim-v])

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
  [{{:accounts/keys [current-account-id] :as db} :db :as cofx} chat-id {:keys [content-type] :as message}]
  (if (= content-type constants/content-type-command)
    (when-let [text-message (get-in message [:content :handler-data :text-message])]
      (handle-message-from-bot cofx {:message text-message
                                     :chat-id chat-id}))
    (let [data (get-in db [:local-storage chat-id])]
      {:call-jail-function {:chat-id    chat-id
                            :function   :on-message-send
                            :parameters {:message (:content message)}
                            :context    {:data data
                                         :from current-account-id}}})))

(defn- send
  [chat-id send-record {{:contacts/keys [contacts]} :db :as cofx}]
  (let [{:keys [dapp? fcm-token]} (get contacts chat-id)]
    (if dapp?
      (send-dapp-message! cofx chat-id send-record)
      (if fcm-token
        (handlers/merge-fx cofx
                           {:send-notification {:message "message"
                                                :payload {:title "Status" :body "You have a new message"}
                                                :tokens [fcm-token]}}
                           (transport/send send-record chat-id))
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
                              {:keys [last-to-clock-value last-from-clock-value] :as chat} now]
  (add-message-type {:chat-id          chat-id
                     :content          message-text
                     :from             identity
                     :content-type     constants/text-content-type
                     :outgoing         true
                     :timestamp        now
                     :to-clock-value   (inc last-to-clock-value)
                     :from-clock-value last-from-clock-value
                     :show?            true}
                    chat))

(def ^:private transport-keys [:content :content-type :message-type :to-clock-value :timestamp])

(defn- upsert-and-send [{:keys [chat-id] :as message} cofx]
  (let [send-record     (protocol/map->Message (select-keys message transport-keys))
        message-with-id (assoc message :message-id (transport.utils/message-id send-record))]
    (handlers/merge-fx cofx
                       (chat-model/upsert-chat {:chat-id chat-id})
                       (add-message chat-id message-with-id true)
                       (send chat-id send-record))))

(defn send-message [{:keys [db now random-id] :as cofx} {:keys [chat-id] :as params}]
  (upsert-and-send (prepare-plain-message chat-id params (get-in db [:chats chat-id]) now) cofx))

(defn- prepare-command-message
  [identity
   {:keys [last-to-clock-value last-from-clock-value chat-id] :as chat}
   now
   {request-params  :params
    request-command :command
    :keys           [prefill prefillBotDb]
    :as             request}
   {:keys [params command handler-data content-type]}]
  (let [content (if request
                  {:request-command     request-command
                   ;; TODO janherich this is technically not correct, but works for now
                   :request-command-ref (:ref command)
                   :params              (assoc request-params :bot-db (:bot-db params))
                   :prefill             prefill
                   :prefill-bot-db      prefillBotDb}
                  {:params  params})
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
                       :to-clock-value   (inc last-to-clock-value)
                       :from-clock-value last-from-clock-value
                       :show?            true}
                      chat)))

(defn- add-console-responses
  [command handler-data {:keys [random-id-seq]}]
  {:dispatch-n (->> (console-events/console-respond-command-messages command handler-data random-id-seq)
                    (mapv (partial vector :chat-received-message/add)))})

(defn send-command
  [{{:keys [current-public-key chats] :as db} :db :keys [now] :as cofx} params]
  (let [{{:keys [handler-data to-message command] :as content} :command chat-id :chat-id} params
        request (:request handler-data)]
    (handlers/merge-fx cofx
                       (upsert-and-send (prepare-command-message current-public-key (get chats chat-id) now request content))
                       (add-console-responses command handler-data)
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
  [{{:accounts/keys [accounts current-account-id]
     :contacts/keys [contacts]} :db}
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
                                           :current-account (get accounts current-account-id)
                                           :message-id      id}
                                    (:async-handler command)
                                    (assoc :orig-params orig-params))}]
    {:call-jail {:jail-id                 identity
                 :path                    [handler-type [name scope-bitmask] :handler]
                 :params                  jail-params
                 :callback-event-creator (fn [jail-response]
                                           (when-not (:async-handler command)
                                             [:command-handler! chat-id orig-params jail-response]))}}))

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
