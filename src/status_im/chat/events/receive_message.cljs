(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.constants :as chat-consts]
            [status-im.chat.sign-up :as sign-up]
            [status-im.chat.models.message :as chat-message]
            [status-im.data-store.chats :as chat-store]
            [status-im.data-store.messages :as msg-store]
            [status-im.utils.handlers :as handlers]))

;; TODO(alwx): this namespace is weird

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
    (assoc cofx :message-exists? msg-store/exists?)))

(re-frame/reg-cofx
  :get-last-clock-value
  (fn [cofx]
    (assoc cofx :get-last-clock-value msg-store/get-last-clock-value)))

;;;; Handlers

(handlers/register-handler-fx
  :chat-received-message/add-protocol-message
  chat-message/receive-interceptors
  (fn [cofx [{:keys [from to payload]}]]
    (chat-message/receive cofx [(merge payload
                                  {:from    from
                                   :to      to
                                   :chat-id from})])))

(handlers/register-handler-fx
  :chat-received-message/add
  chat-message/receive-interceptors
  (fn [cofx [message]]
    ;; TODO(alwx): !!!!!!! several messages
    (chat-message/receive cofx [message])))

(handlers/register-handler-fx
 :chat-received-message/add-bulk
 chat-message/receive-interceptors
 (fn [cofx [messages]]
   (chat-message/receive cofx messages)))

(handlers/register-handler-fx
  :chat-received-message/move-to-internal-failure
  chat-message/receive-interceptors
  (fn [{:keys [get-db-message] :as cofx} _]
    (when-not (get-db-message chat-consts/move-to-internal-failure-message-id)
      (chat-message/receive cofx [sign-up/move-to-internal-failure-message]))))

;; TODO(alwx): should be completely removed
(handlers/register-handler-fx
  :chat-received-message/add-when-commands-loaded
  chat-message/receive-interceptors
  (fn [{:keys [db] :as cofx} [chat-id message]]
    (if (and (:status-node-started? db)
          (get-in db [:contacts/contacts chat-id :commands-loaded?]))
      (chat-message/receive cofx [message])
      {:dispatch-later [{:ms 400 :dispatch [:chat-received-message/add-when-commands-loaded chat-id message]}]})))
