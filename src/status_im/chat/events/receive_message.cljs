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
            [status-im.chat.events.commands :as commands-events]
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

(defn- add-message-to-db
  [db {:keys [message-id] :as message} chat-id]
  (-> db
      (chat-utils/add-message-to-db chat-id chat-id message (:new? message))
      (unviewed-messages-model/add-unviewed-message chat-id message-id)))

(defn add-message
  [{:keys [db message-exists? pop-up-chat? get-last-clock-value now] :as cofx}
   {:keys [from group-id chat-id content-type content message-id timestamp clock-value]
    :as   message
    :or   {clock-value 0}}]
  (let [{:keys [access-scope->commands-responses] :contacts/keys [contacts]} db 
        {:keys [public-key] :as current-account} (get-current-account db)
        chat-identifier (or group-id chat-id from)]
    ;; proceed with adding message if message is not already stored in realm,
    ;; it's not from current user (outgoing message) and it's for relevant chat
    ;; (either current active chat or new chat not existing yet)
    (when (and (not (message-exists? message-id))
               (not= from public-key)
               (pop-up-chat? chat-identifier)) 
      (let [fx               (if (get-in db [:chats chat-identifier])
                               (model/upsert-chat cofx {:chat-id    chat-identifier
                                                        :group-chat (boolean group-id)})
                               (model/add-chat cofx chat-identifier))
            command-request? (= content-type const/content-type-command-request)
            command          (:command content)
            enriched-message (cond-> (assoc message
                                            :chat-id     chat-identifier
                                            :timestamp   (or timestamp now)
                                            :show?       true
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
                    (update :db add-message-to-db enriched-message chat-identifier)
                    (assoc :save-message (dissoc enriched-message :new?)))
          command-request?
          (requests-events/add-request chat-identifier enriched-message))))))

(def ^:private receive-interceptors
  [(re-frame/inject-cofx :message-exists?) (re-frame/inject-cofx :pop-up-chat?)
   (re-frame/inject-cofx :get-last-clock-value) (re-frame/inject-cofx :get-stored-chat)
   re-frame/trim-v])

;; we need this internal event without jail checking, otherwise no response for the jail
;; call to generate preview would result to infinite loop of `:received-message` events
(handlers/register-handler-fx
  ::received-message
  receive-interceptors
  (fn [cofx [message]]
    (add-message cofx message)))

(handlers/register-handler-fx
  :received-message
  receive-interceptors
  (fn [{:keys [db] :as cofx} [{:keys [content] :as message}]]
    (if (:command content)
      ;; we are dealing with received command message, we can't add it right away,
      ;; we first need to fetch preview and add it only after we already have the preview.
      ;; note that `request-command-message-data` implicitly wait till jail is ready and
      ;; call is made only after that
      (commands-events/request-command-message-data
       db message
       {:data-type             :preview
        :proceed-event-creator (fn [preview]
                                 [::received-message
                                  (assoc-in message [:content :preview] preview)])})
      ;; regular non command message, we can add it right away
      (add-message cofx message))))

;; TODO janherich: get rid of this special case once they hacky app start-up sequence is refactored
(handlers/register-handler-fx
  :received-message-when-commands-loaded
  receive-interceptors
  (fn [{:keys [db] :as cofx} [{:keys [chat-id] :as message}]]
    (if (and (:status-node-started? db)
             (get-in db [:contacts/contacts chat-id :jail-loaded?]))
      (add-message cofx message)
      {:dispatch-later [{:ms 400 :dispatch [:received-message-when-commands-loaded message]}]})))
