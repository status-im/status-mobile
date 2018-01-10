(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.chat.events.console :as console-events]
            [status-im.chat.events.requests :as requests-events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.utils :as chat-utils]
            [status-im.data-store.messages :as messages-store]
            [status-im.utils.datetime :as datetime-utils]
            [status-im.utils.clocks :as clocks-utils]
            [status-im.utils.random :as random]))

(defn- get-current-account
  [{:accounts/keys [accounts current-account-id]}]
  (get accounts current-account-id))

(def receive-interceptors
  [(re-frame/inject-cofx :message-exists?)
   (re-frame/inject-cofx :pop-up-chat?)
   (re-frame/inject-cofx :get-last-clock-value)
   (re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :get-stored-chat)
   re-frame/trim-v])

(defn- lookup-response-ref
  [access-scope->commands-responses account chat contacts response-name]
  (let [available-commands-responses (commands-model/commands-responses :response
                                                                        access-scope->commands-responses
                                                                        account
                                                                        chat
                                                                        contacts)]
    (:ref (get available-commands-responses response-name))))

(defn- add-message-to-db
  [db {:keys [message-id] :as message} chat-id current-chat?]
  (cond-> (chat-utils/add-message-to-db db chat-id chat-id message (:new? message))
    (not current-chat?)
    (update-in [:chats chat-id :unviewed-messages] (fnil conj #{}) message-id)))

(defn receive
  [{:keys [db message-exists? pop-up-chat? get-last-clock-value now] :as cofx}
   {:keys [from group-id chat-id content-type content message-id timestamp clock-value]
    :as   message
    :or   {clock-value 0}}]
  (let [{:keys [current-chat-id view-id
                access-scope->commands-responses] :contacts/keys [contacts]} db
        {:keys [public-key] :as current-account} (get-current-account db)
        chat-identifier (or group-id chat-id from)
        direct-message? (nil? group-id)]
    ;; proceed with adding message if message is not already stored in realm,
    ;; it's not from current user (outgoing message) and it's for relevant chat
    ;; (either current active chat or new chat not existing yet or it's a direct message)
    (when (and (not (message-exists? message-id))
               (not= from public-key)
               (or (pop-up-chat? chat-identifier)
                   direct-message?))
      (let [current-chat?    (and (= :chat view-id)
                                  (= current-chat-id chat-identifier))
            fx               (if (get-in db [:chats chat-identifier])
                               (chat-model/upsert-chat cofx {:chat-id chat-identifier
                                                             :group-chat (boolean group-id)})
                               (chat-model/add-chat cofx chat-identifier))
            command-request? (= content-type constants/content-type-command-request)
            command          (:command content)
            enriched-message (cond-> (assoc message
                                            :chat-id     chat-identifier
                                            :timestamp   (or timestamp now)
                                            :show?       true
                                            :clock-value (clocks-utils/receive
                                                          clock-value
                                                          (get-last-clock-value chat-identifier)))
                               public-key
                               (assoc :user-statuses {public-key (if current-chat? :seen :received)})
                               (and command command-request?)
                               (assoc-in [:content :content-command-ref]
                                         (lookup-response-ref access-scope->commands-responses
                                                              current-account
                                                              (get-in fx [:db :chats chat-identifier])
                                                              contacts
                                                              command)))]
        (cond-> (-> fx
                    (update :db add-message-to-db enriched-message chat-identifier current-chat?)
                    (assoc :save-message (dissoc enriched-message :new?)))
                command-request?
                (requests-events/add-request chat-identifier enriched-message))))))

(defn- handle-message-from-bot [cofx {:keys [message chat-id]}]
  (when-let [message (cond
                       (string? message)
                       {:message-id   (random/id)
                        :content      (str message)
                        :content-type constants/text-content-type
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"}

                       (= "request" (:type message))
                       {:message-id   (random/id)
                        :content      (assoc (:content message) :bot chat-id)
                        :content-type constants/content-type-command-request
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"})]
    (receive cofx message)))

(defn- send-dapp-message!
  [{{:accounts/keys [current-account-id] :as db} :db :as cofx}
   {{:keys [message-type]
     :as   message} :message
    :keys [chat-id command] :as args}]
  (if command
    (when-let [text-message (get-in command [:content :handler-data :text-message])]
      (handle-message-from-bot cofx {:message text-message
                                     :chat-id chat-id}))
    (let [data (get-in db [:local-storage chat-id])]
      {:call-jail-function {:chat-id    chat-id
                            :function   :on-message-send
                            :parameters {:message (:content message)}
                            :context    {:data data
                                         :from current-account-id}}})))

(defn- generate-message
  [{:keys [web3 current-public-key chats network-status]}
   {:keys [chat-id command message] :as args}]
  (if command
    (let [payload (-> command
                      (select-keys [:content :content-type
                                    :clock-value :show?])
                      (assoc :timestamp (datetime-utils/now-ms)))
          payload (if (= network-status :offline)
                    (assoc payload :show? false)
                    payload)]
      {:from       current-public-key
       :message-id (:message-id command)
       :payload    payload})
    (let [message' (select-keys message [:from :message-id])
          payload  (select-keys message [:timestamp :content :content-type
                                         :clock-value :show?])
          payload  (if (= network-status :offline)
                     (assoc payload :show? false)
                     payload)]
      (assoc message' :payload payload))))

(defn send
  [{{:keys          [web3 chats]
     :accounts/keys [accounts current-account-id]
     :contacts/keys [contacts] :as db} :db :as cofx}
   {:keys [chat-id command] :as args}]
  (let [{:keys [dapp? fcm-token]} (get contacts chat-id)]
    (if dapp?
      (send-dapp-message! cofx args)
      (let [{:keys [group-chat public?]} (get-in db [:chats chat-id])
            options {:web3    web3
                     :message (generate-message db args)}]
        (cond
          (and group-chat (not public?))
          (let [{:keys [public-key private-key]} (get chats chat-id)]
            {:send-group-message (assoc options
                                   :group-id chat-id
                                   :keypair {:public  public-key
                                             :private private-key})})

          (and group-chat public?)
          {:send-public-group-message (assoc options :group-id chat-id
                                                     :username (get-in accounts [current-account-id :name]))}

          :else
          (merge {:send-message (assoc-in options [:message :to] chat-id)}
                 (when-not command) {:send-notification fcm-token}))))))

;;;; Send message

(def send-interceptors
  [(re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :random-id-seq)
   (re-frame/inject-cofx :get-stored-chat)
   (re-frame/inject-cofx :now)
   (re-frame/inject-cofx :get-last-clock-value)
   re-frame/trim-v])

(defn- prepare-message [clock-value params chat]
  (let [{:keys [chat-id identity message-text]} params
        {:keys [group-chat public?]} chat
        message {:message-id   (random/id)
                 :chat-id      chat-id
                 :content      message-text
                 :from         identity
                 :content-type constants/text-content-type
                 :outgoing     true
                 :timestamp    (datetime-utils/now-ms)
                 :clock-value  (clocks-utils/send clock-value)
                 :show?        true}]
    (cond-> message
            (not group-chat)
            (assoc :message-type :user-message
                   :to           chat-id)
            group-chat
            (assoc :group-id chat-id)
            (and group-chat public?)
            (assoc :message-type :public-group-user-message)
            (and group-chat (not public?))
            (assoc :message-type :group-user-message)
            (not group-chat)
            (assoc :to chat-id :message-type :user-message))))


(defn send-message [{{:keys [network-status] :as db} :db
                     :keys                           [now get-stored-chat get-last-clock-value]}
                    {:keys [chat-id] :as params}]
  (let [chat    (get-in db [:chats chat-id])
        message (prepare-message (get-last-clock-value chat-id) params chat)
        params' (assoc params :message message)

        fx      {:db                       (chat-utils/add-message-to-db db chat-id chat-id message)
                 :update-message-overhead! [chat-id network-status]
                 :save-message             message}]
    (-> (merge fx (chat-model/upsert-chat (assoc fx :get-stored-chat get-stored-chat :now now)
                                          {:chat-id chat-id}))
        (as-> fx'
          (merge fx' (send fx' params'))))))

(defn- prepare-command
  [identity chat-id clock-value
   {request-params  :params
    request-command :command
    :keys           [prefill prefillBotDb]
    :as             request}
   {:keys [id params command to-message handler-data content-type]}]
  (let [content (if request
                  {:command        request-command
                   :params         (assoc request-params :bot-db (:bot-db params))
                   :prefill        prefill
                   :prefill-bot-db prefillBotDb}
                  {:command (:name command)
                   :scope   (:scope command)
                   :params  params})
        content' (assoc content :handler-data handler-data
                                :type (name (:type command))
                                :content-command (:name command)
                                :content-command-scope-bitmask (:scope-bitmask command)
                                :content-command-ref (:ref command)
                                :preview (:preview command)
                                :short-preview (:short-preview command)
                                :bot (or (:bot command)
                                         (:owner-id command)))]
    {:message-id   id
     :from         identity
     :to           chat-id
     :timestamp    (datetime-utils/now-ms)
     :content      content'
     :content-type (or content-type
                       (if request
                         constants/content-type-command-request
                         constants/content-type-command))
     :outgoing     true
     :to-message   to-message
     :type         (:type command)
     :has-handler  (:has-handler command)
     :clock-value  (clocks-utils/send clock-value)
     :show?        true}))

(defn send-command
  [{{:keys [current-public-key network-status] :as db} :db
    :keys [now get-stored-chat random-id-seq get-last-clock-value]} result add-to-chat-id params]
  (let [{{:keys [handler-data
                 command]
          :as   content} :command
         chat-id         :chat-id} params
        request       (:request handler-data)
        hidden-params (->> (:params command)
                           (filter :hidden)
                           (map :name))
        command'      (prepare-command current-public-key chat-id (get-last-clock-value chat-id) request content)
        params'       (assoc params :command command')

        fx            {:db                       (-> (merge db (:db result))
                                                     (chat-utils/add-message-to-db add-to-chat-id chat-id command'))
                       :update-message-overhead! [chat-id network-status]
                       :save-message             (-> command'
                                                     (assoc :chat-id chat-id)
                                                     (update-in [:content :params]
                                                                #(apply dissoc % hidden-params))
                                                     (dissoc :to-message :has-handler :raw-input))}]

    (cond-> (merge fx
                   (chat-model/upsert-chat (assoc fx :get-stored-chat get-stored-chat :now now)
                                           {:chat-id chat-id})
                   (dissoc result :db))

      true
      (as-> fx'
        (merge fx' (send fx' params')))

      (:to-message command')
      (assoc :chat-requests/mark-as-answered {:chat-id    chat-id
                                              :message-id (:to-message command')})

      (= constants/console-chat-id chat-id)
      (as-> fx'
        (let [messages (console-events/console-respond-command-messages params' random-id-seq)
              events   (mapv #(vector :chat-received-message/add %) messages)]
          (update fx' :dispatch-n into events))))))

(defn invoke-console-command-handler
  [{:keys [db] :as cofx} {:keys [chat-id command] :as command-params}]
  (let [fx-fn (get console-events/console-commands->fx (-> command :command :name))
        result (fx-fn cofx command)]
    (send-command cofx result chat-id command-params)))

(defn invoke-command-handlers
  [{{:keys          [bot-db]
     :accounts/keys [accounts current-account-id]
     :contacts/keys [contacts] :as db} :db}
   {{:keys [command params id]} :command
    :keys [chat-id address]
    :as orig-params}]
  (let [{:keys [type name scope-bitmask bot owner-id]} command
        handler-type (if (= :command type) :commands :responses)
        to           (get-in contacts [chat-id :address])
        identity     (or owner-id bot chat-id)
        bot-db       (get bot-db (or bot chat-id))
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
                 :callback-events-creator (fn [jail-response]
                                            (when-not (:async-handler command)
                                              [[:command-handler! chat-id orig-params jail-response]]))}}))

(defn process-command
  [{:keys [db] :as cofx} {:keys [command message chat-id] :as params}]
  (let [{:keys [command] :as content} command]
    (-> {:db (chat-model/set-chat-ui-props db {:sending-in-progress? false})}

        (as-> fx'
          (cond
            (and (= constants/console-chat-id chat-id)
                 (console-events/commands-names (:name command)))
            (invoke-console-command-handler (merge cofx fx') params)

            (:has-handler command)
            (merge fx' (invoke-command-handlers fx' params))

            :else
            (merge fx' (send-command cofx fx' chat-id params)))))))
