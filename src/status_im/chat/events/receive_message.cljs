(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.utils.clocks :as clocks]
            [status-im.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models :as model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.models.unviewed-messages :as unviewed-messages-model]
            [status-im.chat.events.requests :as requests-events]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as msg-store]))

(re-frame/reg-cofx
  :pop-up-chat?
  (fn [cofx]
    (assoc cofx :pop-up-chat? (fn [chat-id]
                                (or (not (chat-store/exists? chat-id))
                                    (chat-store/is-active? chat-id))))))

(re-frame/reg-cofx
  :message-exists?
  (fn [cofx]
    (assoc cofx :message-exists? msg-store/exists?)))

(re-frame/reg-cofx
  :get-last-clock-value
  (fn [cofx]
    (assoc cofx :get-last-clock-value msg-store/get-last-clock-value)))

(defn- get-current-account
  [{:accounts/keys [accounts current-account-id]}]
  (get accounts current-account-id))

(defn- lookup-response-ref
  [access-scope->commands-responses account chat contacts response-name]
  (let [available-commands-responses (commands-model/commands-responses :response
                                                                        access-scope->commands-responses
                                                                        account
                                                                        chat
                                                                        contacts)] 
    (:ref (get available-commands-responses response-name))))

(defn add-message
  [{:keys [db message-exists? get-last-stored-message pop-up-chat?
           get-last-clock-value now random-id] :as cofx}
   {:keys [from group-id chat-id content-type content
           message-id timestamp clock-value]
    :as   message
    :or   {clock-value 0}}]
  (let [{:keys [access-scope->commands-responses] :contacts/keys [contacts]} db
        chat-identifier (or group-id chat-id from)
        current-account (get-current-account db)]
    ;; proceed with adding message if message is not already stored in realm,
    ;; it's not from current user (outgoing message) and it's for relevant chat
    ;; (either current active chat or new chat not existing yet)
    (if (and (not (message-exists? message-id))
             (not= from (:public-key current-account))
             (pop-up-chat? chat-identifier))
      (let [group-chat?      (not (nil? group-id))
            command-request? (= content-type const/content-type-command-request)
            command          (:command content)
            fx               (model/upsert-chat cofx {:chat-id    chat-identifier
                                                      :group-chat group-chat?})
            enriched-message (cond-> (assoc (chat-utils/check-author-direction
                                             (get-last-stored-message chat-identifier)
                                             message)
                                            :chat-id chat-identifier
                                            :timestamp (or timestamp now)
                                            :clock-value (clocks/receive
                                                          clock-value
                                                          (get-last-clock-value chat-identifier)))
                               (and command command-request?)
                               (assoc-in [:content :content-command-ref]
                                         (lookup-response-ref access-scope->commands-responses
                                                              current-account
                                                              (get-in fx [:db :chats chat-identifier])
                                                              contacts
                                                              command)))]
        (cond-> (-> fx
                    (update :db #(-> %
                                     (chat-utils/add-message-to-db chat-identifier chat-identifier enriched-message
                                                                   (:new? enriched-message))
                                     (unviewed-messages-model/add-unviewed-message chat-identifier message-id)
                                     (assoc-in [:chats chat-identifier :last-message] enriched-message)))
                    (assoc :save-message (dissoc enriched-message :new?)))

          command 
          (update :dispatch-n concat [[:request-command-message-data enriched-message :short-preview]
                                      [:request-command-preview enriched-message]])

          command-request?
          (requests-events/add-request chat-identifier enriched-message)))
      {:db db})))

(def ^:private receive-interceptors
  [(re-frame/inject-cofx :message-exists?) (re-frame/inject-cofx :get-last-stored-message)
   (re-frame/inject-cofx :pop-up-chat?) (re-frame/inject-cofx :get-last-clock-value)
   (re-frame/inject-cofx :random-id) (re-frame/inject-cofx :get-stored-chat) re-frame/trim-v])

(handlers/register-handler-fx
  :received-protocol-message!
  receive-interceptors
  (fn [cofx [{:keys [from to payload]}]]
    (add-message cofx (merge payload
                             {:from    from
                              :to      to
                              :chat-id from}))))

(handlers/register-handler-fx
  :received-message
  receive-interceptors
  (fn [cofx [message]]
    (add-message cofx message)))

(handlers/register-handler-fx
  :received-message-when-commands-loaded
  receive-interceptors
  (fn [{:keys [db] :as cofx} [chat-id message]]
    (if (and (:status-node-started? db)
             (get-in db [:contacts/contacts chat-id :jail-loaded?]))
      (add-message cofx message)
      {:dispatch-later [{:ms 400 :dispatch [:received-message-when-commands-loaded chat-id message]}]})))
