(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as messages-store]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.models.message :as message-model]))

;;;; Coeffects

(re-frame/reg-cofx
  :pop-up-chat?
  (fn [cofx]
    (assoc cofx :pop-up-chat? (fn [chat-id]
                                (or (not (chat-store/exists? chat-id))
                                    (chat-store/is-active? chat-id))))))

(re-frame/reg-cofx
  :message-exists?
  (fn [cofx]
    (assoc cofx :message-exists? messages-store/exists?)))

(re-frame/reg-cofx
  :get-last-clock-value
  (fn [cofx]
    (assoc cofx :get-last-clock-value messages-store/get-last-clock-value)))

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
