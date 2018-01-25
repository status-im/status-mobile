(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.models.message :as message-model]
            [status-im.commands.events.jail :as jail-events]
            [status-im.constants :as constants]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.bots.events :as bots-events]))

;;;; Coeffects

(re-frame/reg-cofx
  :pop-up-chat?
  (fn [cofx]
    (assoc cofx :pop-up-chat? (fn [chat-id]
                                (or (not (chat-store/exists? chat-id))
                                    (chat-store/is-active? chat-id))))))

;;;; Helper functions
(defn add-received-message
  ([cofx message]
    (add-received-message cofx message nil))
  ([{:keys [db get-stored-message] :as cofx} {:keys [content message-id] :as message} {:keys [unique?]}]
   (when (or (not unique?)
             (not (get-stored-message message-id)))
     (if (:command content)
       ;; we are dealing with received command message, we can't add it right away,
       ;; we first need to fetch short-preview + preview and add it only after we already have those.
       ;; note that `request-command-message-data` implicitly wait till jail is ready and
       ;; calls are made only after that
       (commands-events/request-command-message-data
         db message
         {:data-type             :short-preview
          :proceed-event-creator (fn [short-preview]
                                   [:request-command-message-data
                                    message
                                    {:data-type             :preview
                                     :proceed-event-creator (fn [preview]
                                                              [::received-message
                                                               (update message :content merge
                                                                       {:short-preview short-preview
                                                                        :preview       preview})])}])})
       ;; regular non command message, we can add it right away
       (message-model/receive cofx message)))))


;;;; Handlers

(handlers/register-handler-fx
  ::received-message
  message-model/receive-interceptors
  (fn [cofx [message]]
    (message-model/receive cofx message)))

(handlers/register-handler-fx
  :chat-received-message/add
  message-model/receive-interceptors
  (fn [cofx [message params]]
    (add-received-message cofx message params)))

;; TODO janherich: get rid of this special case once they hacky app start-up sequence is refactored
(handlers/register-handler-fx
  :chat-received-message/add-when-commands-loaded
  message-model/receive-interceptors
  (fn [{:keys [db] :as cofx} [{:keys [chat-id] :as message}]]
    (if (and (:status-node-started? db)
             (get-in db [:contacts/contacts chat-id :jail-loaded?]))
      (message-model/receive cofx message)
      {:dispatch-later [{:ms 400 :dispatch [:chat-received-message/add-when-commands-loaded message]}]})))

(handlers/register-handler-fx
  :chat-received-message/bot-response
  message-model/receive-interceptors
  (fn [{:contacts/keys [contacts] :keys [db] :as cofx}
       [{:keys [chat-id] :as params} {:keys [result bot-id] :as data}]]
    (let [{:keys [returned context]} result
          {:keys [markup text-message err]} returned
          {:keys [log-messages update-db default-db]} context
          content (or err text-message)
          params' (assoc params
                    :bot-id bot-id
                    :result data
                    :default-db default-db)]
      (cond-> (jail-events/handle-suggestions db params')

        update-db
        (as-> fx'
          (bots-events/update-bot-db (:db fx') {:bot bot-id
                                                :db  update-db}))

        content
        (as-> fx'
          (add-received-message (merge cofx fx') {:message-id   (random/id)
                                                  :content      (str content)
                                                  :content-type constants/text-content-type
                                                  :outgoing     false
                                                  :chat-id      chat-id
                                                  :from         chat-id
                                                  :to           "me"}))

        (seq log-messages)
        (as-> fx'
          (->> log-messages
               (map (fn [{:keys [message type]}]
                      [:chat-received-message/add
                       {:message-id   (random/id)
                        :content      (str type ": " message)
                        :content-type constants/content-type-log-message
                        :outgoing     false
                        :chat-id      chat-id
                        :from         chat-id
                        :to           "me"}]))
               (update fx' :dispatch-n concat)))))))
