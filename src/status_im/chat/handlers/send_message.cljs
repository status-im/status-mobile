(ns status-im.chat.handlers.send-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [clojure.string :as s]
            [status-im.data-store.messages :as messages]
            [status-im.components.status :as status]
            [status-im.utils.random :as random]
            [status-im.utils.datetime :as time]
            [re-frame.core :refer [enrich after dispatch path]]
            [status-im.chat.utils :as cu]
            [status-im.constants :refer [console-chat-id
                                         wallet-chat-id
                                         text-content-type
                                         content-type-command
                                         content-type-command-request
                                         default-number-of-messages] :as c]
            [status-im.chat.constants :refer [input-height]]
            [status-im.utils.datetime :as datetime]
            [status-im.protocol.core :as protocol]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.chat.handlers.console :as console]))

(defn prepare-command
  [identity chat-id clock-value request
   {:keys [id preview preview-string params command
           to-message handler-data content-type]}]
  (let [content (or request {:command (command :name)
                             :params  params})]
    {:message-id   id
     :from         identity
     :to           chat-id
     :timestamp    (time/now-ms)
     :content      (assoc content :handler-data handler-data
                                  :type (name (:type command))
                                  :content-command (:name command))
     :content-type (or content-type
                       (if request
                         content-type-command-request
                         content-type-command))
     :outgoing     true
     :to-message   to-message
     :type         (:type command)
     :has-handler  (:has-handler command)
     :clock-value  (inc clock-value)
     :show?        true}))

(register-handler :send-chat-message
  (u/side-effect!
    (fn [{:keys [current-chat-id current-public-key current-account-id] :as db}
         [_ {:keys [chat-id] :as command-message}]]
      (let [text (get-in db [:chats current-chat-id :input-text])
            data {:command  command-message
                  :message  text
                  :chat-id  (or chat-id current-chat-id)
                  :identity current-public-key
                  :address  current-account-id}]
        (dispatch [:clear-input current-chat-id])
        (cond
          command-message
          (dispatch [::check-commands-handlers! data])
          (not (s/blank? text))
          (dispatch [::prepare-message data]))))))

(defn console-command? [chat-id command-name]
  (and (= console-chat-id chat-id)
       (console/commands-names (keyword command-name))))

(register-handler ::check-commands-handlers!
  (u/side-effect!
    (fn [_ [_ {:keys [command message chat-id] :as params}]]
      (let [{:keys [command] :as message} command]
        (let [params'      (assoc params :command-message message)
              command-name (:name (:command message))]
          (if (:sent-to-jail? message)
            ;; todo there could be other reasons for "long-running"
            ;; hanling of the command besides sendTransaction
            (dispatch [:navigate-to-modal :confirm])
            (cond
              (console-command? chat-id command-name)
              (dispatch [:invoke-console-command-handler! params'])

              (:has-handler command)
              (dispatch [::invoke-command-handlers! params'])

              :else
              (dispatch [:prepare-command! chat-id params'])))))
      (dispatch [:set-chat-ui-props :sending-disabled? false])
      (when-not (s/blank? message)
        (dispatch [::prepare-message params])))))

(register-handler :clear-input
  (path :chats)
  (fn [db [_ chat-id]]
    (assoc-in db [chat-id :input-text] nil)))

(register-handler :prepare-command!
  (u/side-effect!
    (fn [{:keys [current-public-key network-status] :as db}
         [_ add-to-chat-id {:keys [chat-id command-message command handler-data] :as params}]]
      (let [clock-value   (messages/get-last-clock-value chat-id)
            request       (:request (:handler-data command))
            hidden-params (->> (:params (:command command))
                               (filter #(= (:hidden %) true))
                               (map :name))
            command'      (->> (assoc command-message :handler-data handler-data)
                               (prepare-command current-public-key chat-id clock-value request)
                               (cu/check-author-direction db chat-id))]
        (log/debug "Handler data: " request handler-data (dissoc params :commands :command-message))
        (dispatch [:update-message-overhead! chat-id network-status])
        (dispatch [:set-chat-ui-props :sending-disabled? false])
        (dispatch [::send-command! add-to-chat-id (assoc params :command command') hidden-params])
        (when (cu/console? chat-id)
          (dispatch `[:console-respond-command params]))
        (when (and (= "send" (get-in command-message [:command :name]))
                   (not= add-to-chat-id wallet-chat-id))
          (let [ct               (if request
                                   c/content-type-wallet-request
                                   c/content-type-wallet-command)
                command-message' (assoc command-message :id (random/id)
                                                        :content-type ct)
                params'          (assoc params :command-message command-message')]
            (dispatch [:prepare-command! wallet-chat-id params'])))))))

(register-handler ::send-command!
  (u/side-effect!
    (fn [_ [_ add-to-chat-id params hidden-params]]
      (dispatch [::add-command add-to-chat-id params])
      (dispatch [::save-command! add-to-chat-id params hidden-params])
      (when (not= add-to-chat-id wallet-chat-id)
        (dispatch [::dispatch-responded-requests! params])
        (dispatch [::send-command-protocol! params])))))

(register-handler ::add-command
  (after (fn [_ [_ _ {:keys [handler]}]]
           (when handler (handler))))
  (fn [db [_ add-to-chat-id {:keys [chat-id command]}]]
    (cu/add-message-to-db db add-to-chat-id chat-id command)))

(register-handler ::save-command!
  (u/side-effect!
    (fn [_ [_ chat-id {:keys [command]} hidden-params]]
      (let [command (-> command
                        (update-in [:content :params] #(apply dissoc % hidden-params))
                        (dissoc :to-message :has-handler))]
        (messages/save chat-id command)))))

(register-handler ::dispatch-responded-requests!
  (u/side-effect!
    (fn [_ [_ {:keys [command chat-id]}]]
      (let [{:keys [to-message]} command]
        (when to-message
          (dispatch [:request-answered! chat-id to-message]))))))

(register-handler ::invoke-command-handlers!
  (u/side-effect!
    (fn [db [_ {:keys [chat-id address command-message]
                :as   parameters}]]
      (let [{:keys [id command params]} command-message
            {:keys [type name bot]} command
            path   [(if (= :command type) :commands :responses)
                    name
                    :handler]
            to     (get-in db [:contacts chat-id :address])
            params {:parameters params
                    :context    {:from       address
                                 :to         to
                                 :message-id id}}
            identity (or bot chat-id)]
        (dispatch
          [:check-and-load-commands!
           identity
           (status/call-jail
             identity
             path
             params
             #(dispatch [:command-handler! chat-id parameters %]))])))))

(register-handler ::prepare-message
  (u/side-effect!
    (fn [{:keys [network-status] :as db} [_ {:keys [chat-id identity message] :as params}]]
      (let [{:keys [group-chat public?]} (get-in db [:chats chat-id])
            clock-value (messages/get-last-clock-value chat-id)
            message'    (cu/check-author-direction
                          db chat-id
                          {:message-id   (random/id)
                           :chat-id      chat-id
                           :content      message
                           :from         identity
                           :content-type text-content-type
                           :outgoing     true
                           :timestamp    (time/now-ms)
                           :clock-value  (inc clock-value)
                           :show?        true})
            message''   (cond-> message'
                                (and group-chat public?)
                                (assoc :group-id chat-id :message-type :public-group-user-message)
                                (and group-chat (not public?))
                                (assoc :group-id chat-id :message-type :group-user-message)
                                (not group-chat)
                                (assoc :to chat-id :message-type :user-message))
            params'     (assoc params :message message'')]
        (dispatch [:update-message-overhead! chat-id network-status])
        (dispatch [:set-chat-ui-props :sending-disabled? false])
        (dispatch [::add-message params'])
        (dispatch [::save-message! params'])))))

(register-handler ::add-message
  (fn [db [_ {:keys [chat-id message]}]]
    (cu/add-message-to-db db chat-id chat-id message)))

(register-handler ::save-message!
  (after (fn [_ [_ params]]
           (dispatch [::send-message! params])))
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id message]}]]
      (dispatch [:upsert-chat! {:chat-id   chat-id
                                :timestamp (time/now-ms)}])
      (messages/save chat-id message))))

(register-handler :clear-response-suggestions
  (fn [db [_ chat-id]]
    (-> db
        (update-in [:suggestions] dissoc chat-id)
        (update-in [:has-suggestions?] dissoc chat-id)
        (assoc-in [:animations :to-response-height chat-id] input-height))))

(register-handler ::send-dapp-message
  (u/side-effect!
    (fn [db [_ chat-id {:keys [content]}]]
      (let [data   (get-in db [:local-storage chat-id])
            path   [:functions
                    :message-handler]
            params {:parameters {:message content}
                    :context    {:data data}}]
        (dispatch [:clear-response-suggestions chat-id])
        (status/call-jail chat-id
                          path
                          params
                          (fn [{:keys [result]}]
                            (log/debug "Message handler result: " result)
                            (dispatch [::received-dapp-message chat-id result])))))))

(register-handler ::received-dapp-message
  (u/side-effect!
    (fn [_ [_ chat-id {:keys [returned]}]]
      (let [{:keys [data messages err]} returned
            content (or err data)]
        (doseq [message messages]
          (let [{:keys [message type]} message]
            (dispatch [:received-message
                       {:message-id   (random/id)
                        :content      (str type ": " message)
                        :content-type text-content-type
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"}])))
        (when content
          (dispatch [:received-message
                     {:message-id   (random/id)
                      :content      (str content)
                      :content-type text-content-type
                      :outgoing     false
                      :chat-id      chat-id
                      :from         chat-id
                      :to           "me"}]))))))

(register-handler ::send-message!
  (u/side-effect!
    (fn [{:keys [web3 chats network-status current-account-id accounts]
          :as   db} [_ {{:keys [message-type]
                         :as   message} :message
                        chat-id         :chat-id}]]
      (let [{:keys [dapp?]} (get-in db [:contacts chat-id])]
        (if dapp?
          (dispatch [::send-dapp-message chat-id message])
          (when message
            (let [message' (select-keys message [:from :message-id])
                  payload  (select-keys message [:timestamp :content :content-type
                                                 :clock-value :show?])
                  payload  (if (= network-status :offline)
                             (assoc payload :show? false)
                             payload)
                  options  {:web3    web3
                            :message (assoc message' :payload payload)}]
              (cond
                (= message-type :group-user-message)
                (let [{:keys [public-key private-key]} (chats chat-id)]
                  (protocol/send-group-message! (assoc options
                                                  :group-id chat-id
                                                  :keypair {:public  public-key
                                                            :private private-key})))

                (= message-type :public-group-user-message)
                (protocol/send-public-group-message!
                  (let [username (get-in accounts [current-account-id :name])]
                    (assoc options :group-id chat-id
                                   :username username)))

                :else
                (protocol/send-message! (assoc-in options
                                                  [:message :to] (:to message)))))))))))

(register-handler ::send-command-protocol!
  (u/side-effect!
    (fn [{:keys [web3 current-public-key chats network-status
                 current-account-id accounts] :as db}
         [_ {:keys [chat-id command]}]]
      (log/debug "sending command: " command)
      (when (cu/not-console? chat-id)
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
