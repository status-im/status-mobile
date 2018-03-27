(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.core :as utils]
            [status-im.chat.events.console :as console-events]
            [status-im.chat.events.requests :as requests-events]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.utils.clocks :as clocks-utils]))

(defn- get-current-account
  [{:accounts/keys [accounts current-account-id]}]
  (get accounts current-account-id))

(def receive-interceptors
  [(re-frame/inject-cofx :get-stored-message) (re-frame/inject-cofx :get-stored-chat)
   (re-frame/inject-cofx :random-id) re-frame/trim-v])

(defn- lookup-response-ref
  [access-scope->commands-responses account chat contacts response-name]
  (let [available-commands-responses (commands-model/commands-responses :response
                                                                        access-scope->commands-responses
                                                                        account
                                                                        chat
                                                                        contacts)]
    (:ref (get available-commands-responses response-name))))

(defn add-message-to-db
  [db chat-id {:keys [message-id clock-value] :as message} current-chat?]
  (let [prepared-message (cond-> (assoc message
                                        :chat-id    chat-id
                                        :appearing? true)
                           (not current-chat?)
                           (assoc :appearing? false))]
    (cond-> (-> db
                (update-in [:chats chat-id :messages] assoc message-id prepared-message)
                (update-in [:chats chat-id :last-clock-value] (fnil max 0) clock-value))
      (not current-chat?)
      (update-in [:chats chat-id :unviewed-messages] (fnil conj #{}) message-id))))

(defn receive
  [{:keys [db now] :as cofx}
   {:keys [from group-id chat-id content-type content message-id timestamp clock-value]
    :as   message}]
  (let [{:keys [current-chat-id view-id
                access-scope->commands-responses] :contacts/keys [contacts]} db
        {:keys [public-key] :as current-account} (get-current-account db)
        chat-identifier (or group-id chat-id from)
        current-chat?    (and (= :chat view-id)
                              (= current-chat-id chat-identifier))
        fx               (if (get-in db [:chats chat-identifier])
                           (chat-model/upsert-chat cofx {:chat-id chat-identifier
                                                         :group-chat (boolean group-id)})
                           (chat-model/add-chat cofx chat-identifier))
        chat             (get-in fx [:db :chats chat-identifier])
        command-request? (= content-type constants/content-type-command-request)
        command          (:command content)
        enriched-message (cond-> (assoc message
                                        :chat-id     chat-identifier
                                        :timestamp   (or timestamp now)
                                        :show?       true
                                        :clock-value (clocks-utils/receive
                                                      clock-value
                                                      (:last-clock-value chat)))
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
                (update :db add-message-to-db chat-identifier enriched-message current-chat?)
                (assoc :save-message (dissoc enriched-message :new?)))
      command-request?
      (requests-events/add-request chat-identifier enriched-message))))

(defn add-to-chat?
  [{:keys [db get-stored-message]} {:keys [group-id chat-id from message-id]}]
  (let [chat-identifier                                  (or group-id chat-id from)
        {:keys [chats deleted-chats current-public-key]} db
        {:keys [messages not-loaded-message-ids]}        (get chats chat-identifier)]
    (when (not= from current-public-key)
      (if group-id
        (not (or (get deleted-chats chat-identifier)
                 (get messages message-id)
                 (get not-loaded-message-ids message-id)))
        (not (or (get messages message-id)
                 (get not-loaded-message-ids message-id)
                 (and (get deleted-chats chat-identifier)
                      (get-stored-message message-id))))))))

(defn message-seen-by? [message user-pk]
  (= :seen (get-in message [:user-statuses user-pk])))

;;;; Send message

(def send-interceptors
  [(re-frame/inject-cofx :random-id) (re-frame/inject-cofx :random-id-seq)
   (re-frame/inject-cofx :get-stored-chat) re-frame/trim-v])

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
  [{:keys [network-status]} chat-id message]
  (assoc (select-keys message [:from :message-id])
         :payload (cond-> (select-keys message [:content :content-type :clock-value :timestamp :show?])
                    (= :offline network-status)
                    (assoc :show? false))))

(defn send
  [{{:keys          [web3 chats]
     :accounts/keys [accounts current-account-id]
     :contacts/keys [contacts] :as db} :db :as cofx}
   {:keys [chat-id command message] :as args}]
  (let [{:keys [dapp? fcm-token]} (get contacts chat-id)]
    (if dapp?
      (send-dapp-message! cofx args)
      (let [{:keys [group-chat public?]} (get-in db [:chats chat-id])
            options {:web3    web3
                     :message (generate-message db chat-id (or command message))}]
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
                 (when fcm-token {:send-notification {:message "message"
                                                      :payload {:title "Status" :body "You have a new message"}
                                                      :tokens [fcm-token]}})))))))

(defn- prepare-message [params chat now random-id]
  (let [{:keys [chat-id identity message-text]} params
        {:keys [group-chat public? last-clock-value]} chat
        message {:message-id   random-id
                 :chat-id      chat-id
                 :content      message-text
                 :from         identity
                 :content-type constants/text-content-type
                 :outgoing     true
                 :timestamp    now
                 :clock-value  (clocks-utils/send last-clock-value)
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

(defn send-message [{:keys [db now random-id] :as cofx} {:keys [chat-id] :as params}]
  (let [chat    (get-in db [:chats chat-id])
        message (prepare-message params chat now random-id)
        params' (assoc params :message message)
        fx      (-> (chat-model/upsert-chat cofx {:chat-id chat-id})
                    (update :db add-message-to-db chat-id message true)
                    (assoc :save-message message))]
    (merge fx (send cofx params'))))

(defn- prepare-command
  [identity chat-id now clock-value
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
     :timestamp    now
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
  [{{:keys [current-public-key chats]} :db :keys [now random-id-seq] :as cofx} params]
  (let [{{:keys [handler-data
                 command]
          :as   content} :command
         chat-id         :chat-id} params
        request          (:request handler-data)
        last-clock-value (get-in chats [chat-id :last-clock-value])
        hidden-params    (->> (:params command)
                              (filter :hidden)
                              (map :name))
        command'         (prepare-command current-public-key chat-id now last-clock-value request content)
        params'          (assoc params :command command')
        fx               (-> (chat-model/upsert-chat cofx {:chat-id chat-id})
                             (update :db add-message-to-db chat-id command' true)
                             (assoc :save-message (-> command'
                                                      (assoc :chat-id chat-id)
                                                      (update-in [:content :params]
                                                                 #(apply dissoc % hidden-params))
                                                      (dissoc :to-message :has-handler :raw-input))))]
    (cond-> (merge fx (send cofx params'))

      (:to-message command')
      (requests-events/request-answered chat-id (:to-message command'))

      (= constants/console-chat-id chat-id)
      (as-> fx'
          (let [messages (console-events/console-respond-command-messages params' random-id-seq)
                events   (mapv #(vector :chat-received-message/add %) messages)]
            (update fx' :dispatch-n into events))))))

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
                 :callback-events-creator (fn [jail-response]
                                            (when-not (:async-handler command)
                                              [[:command-handler! chat-id orig-params jail-response]]))}}))

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
