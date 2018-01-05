(ns status-im.chat.models.message
  (:require [re-frame.core :as re-frame]
            [status-im.utils.clocks :as clocks]
            [status-im.constants :as constants]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.events.requests :as requests-events]
            [taoensso.timbre :as log]))

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
                                            :clock-value (clocks/receive
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
