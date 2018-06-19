(ns status-im.chat.events.receive-message
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.models.message :as message-model]
            [status-im.constants :as constants]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

;;;; Handlers

(handlers/register-handler-fx
 ::received-message
 message-model/receive-interceptors
 (fn [cofx [message]]
   (when (message-model/add-to-chat? cofx message)
     (message-model/receive message cofx))))

(re-frame.core/reg-fx
 :chat-received-message/add-fx
 (fn [messages]
   (re-frame/dispatch [:chat-received-message/add messages])))

(defn- request-command-message-data [message {:keys [db]}]
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
                                                                  :preview       preview})])}])}))

(defn add-message [{:keys [content] :as message} {:keys [db] :as cofx}]
  (when (message-model/add-to-chat? cofx message)
    (if (:command content)
      ;; we are dealing with received command message, we can't add it right away,
      ;; we first need to fetch short-preview + preview and add it only after we already have those.
      ;; note that `request-command-message-data` implicitly wait till jail is ready and
      ;; calls are made only after that
      (let [{:keys [command params]} content
            tx-hash (:tx-hash params)]
        (handlers-macro/merge-fx cofx
                                 (message-model/update-transactions command tx-hash {:with-delay? true})
                                 (request-command-message-data message)))
      ;; regular non command message, we can add it right away
      (message-model/receive message cofx))))

(defn- filter-messages [messages cofx]
  (:accumulated (reduce (fn [{:keys [seen-ids] :as acc}
                             {:keys [message-id] :as message}]
                          (if (and (message-model/add-to-chat? cofx message)
                                   (not (seen-ids message-id)))
                            (-> acc
                                (update :seen-ids conj message-id)
                                (update :accumulated conj message))
                            acc))
                        {:seen-ids    #{}
                         :accumulated []}
                        messages)))

(defn add-messages [messages {:keys [db] :as cofx}]
  (let [messages-to-add  (filter-messages messages cofx)
        plain-messages   (remove (comp :command :content) messages-to-add)
        command-messages (filter (comp :command :content) messages-to-add)]
    (handlers-macro/merge-effects (message-model/receive-many plain-messages cofx)
                                  cofx
                                  add-message
                                  command-messages)))

(handlers/register-handler-fx
 :chat-received-message/add
 message-model/receive-interceptors
 (fn [cofx [messages]]
   (add-messages messages cofx)))

;; TODO(alwx): refactor this when status-im.commands.handlers.jail is refactored
(handlers/register-handler-fx
 :chat-received-message/bot-response
 message-model/receive-interceptors
 (fn [{:keys [random-id now]} [{:keys [chat-id] :as params} {:keys [result bot-id] :as data}]]
   (let [{:keys [returned context]} result
         {:keys [markup text-message err]} returned
         {:keys [update-db default-db]} context
         content (or err text-message)]
     (when update-db
       (re-frame/dispatch [:update-bot-db {:bot bot-id
                                           :db  update-db}]))
     (re-frame/dispatch [:suggestions-handler (assoc params
                                                     :bot-id bot-id
                                                     :result data
                                                     :default-db default-db)])
     (when content
       (re-frame/dispatch [:chat-received-message/add
                           [{:message-id   random-id
                             :timestamp    now
                             :content      (str content)
                             :content-type constants/text-content-type
                             :clock-value  (utils.clocks/send 0)
                             :chat-id      chat-id
                             :from         chat-id
                             :to           "me"
                             :show?        true}]])))))
