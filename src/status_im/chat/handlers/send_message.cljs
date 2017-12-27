(ns status-im.chat.handlers.send-message
  (:require [clojure.string :as s]
            [re-frame.core :refer [after dispatch path]]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.events.console :as console]
            [status-im.chat.utils :as cu]
            [status-im.constants :refer [console-chat-id
                                         text-content-type
                                         content-type-log-message
                                         content-type-command
                                         content-type-command-request] :as c]
            [status-im.data-store.messages :as messages]
            [status-im.native-module.core :as status]
            [status-im.protocol.core :as protocol]
            [status-im.utils.config :as config]
            [status-im.utils.clocks :as clocks]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.utils.random :as random]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn prepare-command
  [identity chat-id clock-value
   {request-params  :params
    request-command :command
    :keys           [prefill prefillBotDb]
    :as             request}
   {:keys [id params command to-message handler-data content-type]}]
  (let [content  (if request
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
     :timestamp    (datetime/now-ms)
     :content      content'
     :content-type (or content-type
                       (if request
                         content-type-command-request
                         content-type-command))
     :outgoing     true
     :to-message   to-message
     :type         (:type command)
     :has-handler  (:has-handler command)
     :clock-value  (clocks/send clock-value)
     :show?        true}))

(defn console-command? [chat-id command-name]
  (and (= console-chat-id chat-id)
       (console/commands-names command-name)))

(register-handler  :check-commands-handlers!
  (u/side-effect!
    (fn [_ [_ {:keys [command message chat-id] :as params}]]
      (let [{:keys [command] :as content} command]
        (cond
          (console-command? chat-id (:name command))
          (dispatch [:invoke-console-command-handler! params])

          (:has-handler command)
          (dispatch [::invoke-command-handlers! params])

          :else
          (dispatch [:prepare-command! chat-id params])))
      (dispatch [:set-chat-ui-props {:sending-in-progress? false}]))))

(register-handler :prepare-command!
  (u/side-effect!
    (fn [{:keys [current-public-key network-status] :as db}
         [_ add-to-chat-id {{:keys [handler-data
                                    command]
                             :as   content} :command
                            chat-id         :chat-id
                            :as             params}]]
      (let [clock-value   (messages/get-last-clock-value chat-id)
            request       (:request handler-data)
            hidden-params (->> (:params command)
                               (filter :hidden)
                               (map :name))
            command'      (prepare-command current-public-key chat-id clock-value request content)]
        (dispatch [:update-message-overhead! chat-id network-status])
        (dispatch [:set-chat-ui-props {:sending-in-progress? false}])
        (dispatch [::send-command! add-to-chat-id (assoc params :command command') hidden-params])
        (when (= console-chat-id chat-id)
          (dispatch [:console-respond-command params]))))))

(register-handler ::send-command!
  (u/side-effect!
    (fn [_ [_ add-to-chat-id params hidden-params]]
      (dispatch [::add-command add-to-chat-id params])
      (dispatch [::save-command! add-to-chat-id params hidden-params])
      (dispatch [::dispatch-responded-requests! params])
      (dispatch [::send-command-protocol! (update-in params [:command :content]
                                                     dissoc :preview :short-preview)]))))

(register-handler ::add-command
  (after (fn [_ [_ _ {:keys [handler]}]]
           (when handler (handler))))
  (fn [db [_ add-to-chat-id {:keys [chat-id command]}]]
    (cu/add-message-to-db db add-to-chat-id chat-id command)))

(register-handler
  ::save-command!
  (u/side-effect!
    (fn [db [_ chat-id {:keys [command]} hidden-params]]
      (let [
            command (cond-> (-> command
                                (assoc :chat-id chat-id)
                                (update-in [:content :params] #(apply dissoc % hidden-params))
                                (dissoc :to-message :has-handler :raw-input)))]
        (dispatch [:upsert-chat! {:chat-id chat-id}])
        (messages/save command)))))

(register-handler ::dispatch-responded-requests!
  (u/side-effect!
    (fn [_ [_ {{:keys [to-message]} :command :keys [chat-id]}]]
      (when to-message
        (dispatch [:request-answered chat-id to-message])))))

(register-handler ::invoke-command-handlers!
  (u/side-effect!
    (fn [{:keys [bot-db]
          :accounts/keys [accounts current-account-id]
          :contacts/keys [contacts] :as db}
         [_ {{:keys [command
                     params
                     id]} :command
             :keys        [chat-id address]
             :as          orig-params}]]
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
        (status/call-jail
         {:jail-id  identity
          :path     [handler-type [name scope-bitmask] :handler]
          :params   jail-params
          :callback (if (:async-handler command) ; async handler, we ignore return value
                      (fn [_]
                        (log/debug "Async command handler called"))
                      (fn [res]
                        (dispatch [:command-handler! chat-id orig-params res])))})))))

(register-handler
  :received-bot-response
  (u/side-effect!
    (fn [{:contacts/keys [contacts]} [_ {:keys [chat-id] :as params} {:keys [result bot-id] :as data}]]
      (let [{:keys [returned context]} result
            {:keys [markup text-message err]} returned
            {:keys [log-messages update-db default-db]} context
            content (or err text-message)]
        (when update-db
          (dispatch [:update-bot-db {:bot bot-id
                                     :db update-db}]))
        (dispatch [:suggestions-handler (assoc params
                                          :bot-id bot-id
                                          :result data
                                          :default-db default-db)])
        (doseq [message log-messages]
          (let [{:keys [message type]} message]
            (when (or (not= type "debug")
                      js/goog.DEBUG
                      (get-in contacts [chat-id :debug?]))
              (dispatch [:chat-received-message/add
                         {:message-id (random/id)
                          :content (str type ": " message)
                          :content-type content-type-log-message
                          :outgoing false
                          :chat-id chat-id
                          :from chat-id
                          :to "me"}]))))
        (when content
          (dispatch [:chat-received-message/add
                     {:message-id (random/id)
                      :content (str content)
                      :content-type text-content-type
                      :outgoing false
                      :chat-id chat-id
                      :from chat-id
                      :to "me"}]))))))

(defn handle-message-from-bot [{:keys [message chat-id]}]
  (cond
    (string? message)
    (dispatch [:chat-received-message/add {:message-id (random/id)
                                           :content (str message)
                                           :content-type text-content-type
                                           :outgoing false
                                           :chat-id chat-id
                                           :from chat-id
                                           :to "me"}])

    (= "request" (:type message))
    (dispatch [:add-request-message!
               {:content (:content message)
                :chat-id chat-id}])))

(register-handler :send-message-from-jail
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id message]}]]
      (let [parsed-message (types/json->clj message)]
        (handle-message-from-bot {:message parsed-message
                                  :chat-id chat-id})))))

(register-handler :show-suggestions-from-jail
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id markup]}]]
      (let [markup' (types/json->clj markup)
            result  (assoc-in {} [:result :returned :markup] markup')]
        (dispatch [:suggestions-handler
                   {:result  result
                    :chat-id chat-id}])))))

(register-handler ::send-command-protocol!
  (u/side-effect!
    (fn [{:keys [web3 current-public-key chats network-status]
          :accounts/keys [accounts current-account-id]
          :contacts/keys [contacts] :as db}
         [_ {:keys [chat-id command]}]]
      (if (get-in contacts [chat-id :dapp?])
        (when-let [text-message (get-in command [:content :handler-data :text-message])]
          (handle-message-from-bot {:message text-message
                                    :chat-id chat-id}))
        (let [{:keys [public-key private-key]} (chats chat-id)
              {:keys [group-chat public?]} (get-in db [:chats chat-id])

              payload (-> command
                          (select-keys [:content :content-type
                                        :clock-value :show?])
                          (assoc :timestamp (datetime/now-ms)))
              payload (if (= network-status :offline)
                        (assoc payload :show? false)
                        payload)
              options {:web3    web3
                       :message {:from       current-public-key
                                 :message-id (:message-id command)
                                 :payload    payload}}]
          (cond
            (and group-chat (not public?))
            (protocol/send-group-message! (assoc options
                                            :group-id chat-id
                                            :keypair {:public  public-key
                                                      :private private-key}))

            (and group-chat public?)
            (protocol/send-public-group-message!
              (let [username (get-in accounts [current-account-id :name])]
                (assoc options :group-id chat-id
                               :username username)))

            :else
            (protocol/send-message! (assoc-in options
                                              [:message :to] chat-id))))))))

(register-handler
 :add-request-message!
 (u/side-effect!
  (fn [_ [_ {:keys [content chat-id]}]]
    (dispatch [:chat-received-message/add
               {:message-id (random/id)
                :content (assoc content :bot chat-id)
                :content-type content-type-command-request
                :outgoing false
                :chat-id chat-id
                :from chat-id
                :to "me"}]))))
