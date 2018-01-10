(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.models.message :as message-model]
            [status-im.constants :as constants]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]))

;;;; Coeffects

(re-frame/reg-cofx
  :pop-up-chat?
  (fn [cofx]
    (assoc cofx :pop-up-chat? (fn [chat-id]
                                (or (not (chat-store/exists? chat-id))
                                    (chat-store/is-active? chat-id))))))

;;;; FX

(handlers/register-handler-fx
  ::received-message
  message-model/receive-interceptors
  (fn [cofx [message]]
    (message-model/receive cofx message)))

(handlers/register-handler-fx
  :chat-received-message/add
  message-model/receive-interceptors
  (fn [{:keys [db] :as cofx} [{:keys [content] :as message}]]
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
      (message-model/receive cofx message))))

;; TODO janherich: get rid of this special case once they hacky app start-up sequence is refactored
(handlers/register-handler-fx
  :chat-received-message/add-when-commands-loaded
  message-model/receive-interceptors
  (fn [{:keys [db] :as cofx} [{:keys [chat-id] :as message}]]
    (if (and (:status-node-started? db)
             (get-in db [:contacts/contacts chat-id :jail-loaded?]))
      (message-model/receive cofx message)
      {:dispatch-later [{:ms 400 :dispatch [:chat-received-message/add-when-commands-loaded message]}]})))

;; TODO(alwx): refactor this when status-im.commands.handlers.jail is refactored
(handlers/register-handler-fx
  :chat-received-message/bot-response
  (fn [{:contacts/keys [contacts]} [_ {:keys [chat-id] :as params} {:keys [result bot-id] :as data}]]
    (let [{:keys [returned context]} result
          {:keys [markup text-message err]} returned
          {:keys [log-messages update-db default-db]} context
          content (or err text-message)]
      (when update-db
        (re-frame/dispatch [:update-bot-db {:bot bot-id
                                            :db  update-db}]))
      (re-frame/dispatch [:suggestions-handler (assoc params
                                                 :bot-id bot-id
                                                 :result data
                                                 :default-db default-db)])
      (doseq [message log-messages]
        (let [{:keys [message type]} message]
          (when (or (not= type "debug")
                    js/goog.DEBUG
                    (get-in contacts [chat-id :debug?]))
            (re-frame/dispatch [:chat-received-message/add
                                {:message-id   (random/id)
                                 :content      (str type ": " message)
                                 :content-type constants/content-type-log-message
                                 :outgoing     false
                                 :chat-id      chat-id
                                 :from         chat-id
                                 :to           "me"}]))))
      (when content
        (re-frame/dispatch [:chat-received-message/add
                            {:message-id   (random/id)
                             :content      (str content)
                             :content-type constants/text-content-type
                             :outgoing     false
                             :chat-id      chat-id
                             :from         chat-id
                             :to           "me"}])))))
