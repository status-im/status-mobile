(ns status-im.chat.events.commands
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

;;;; Helper fns

(defn- generate-context
  "Generates context for jail call"
  [current-account-id chat-id to group-id]
  (merge {:platform     platform/os
          :from         current-account-id
          :to           to
          :chat         {:chat-id    chat-id
                         :group-chat (not (nil? group-id))}}
         i18n/delimeters))

(defn request-command-message-data
  "Requests command message data from jail"
  [db
   {{command-name :command
     content-command-name :content-command
     :keys [content-command-scope-bitmask bot scope-bitmask params type]} :content
    :keys [chat-id group-id jail-id] :as message}
   {:keys [data-type] :as opts}]
  (let [{:accounts/keys [current-account-id]
         :contacts/keys [contacts]} db
        jail-id               (or bot jail-id chat-id)
        jail-command-name     (or content-command-name command-name)]
    (if-not (get contacts jail-id) ;; bot is not even in contacts, do nothing
      {:db db}
      (if (get-in contacts [jail-id :jail-loaded?])
        (let [path        [(if (= :response (keyword type)) :responses :commands)
                           [jail-command-name
                            (or content-command-scope-bitmask scope-bitmask)]
                           data-type]
              to          (get-in contacts [chat-id :address])
              jail-params {:parameters params
                           :context    (generate-context current-account-id chat-id to group-id)}]
          {:db        db
           :call-jail {:jail-id                 jail-id
                       :path                    path
                       :params                  jail-params
                       :callback-events-creator (fn [jail-response]
                                                  [[::jail-command-data-response
                                                    jail-response message opts]])}}) 
        {:db (update-in db [:contacts/contacts jail-id :jail-loaded-events]
                        conj [:request-command-message-data message opts])}))))

;;;; Handlers

(handlers/register-handler-fx
  ::jail-command-data-response
  [re-frame/trim-v]
  (fn [{:keys [db]} [{{:keys [returned]} :result} {:keys [chat-id]} {:keys [proceed-event-creator]}]]
    (when proceed-event-creator
      {:dispatch (proceed-event-creator returned)})))

(handlers/register-handler-fx
  :request-command-message-data
  [re-frame/trim-v (re-frame/inject-cofx :get-local-storage-data)]
  (fn [{:keys [db]} [message opts]]
    (request-command-message-data db message opts)))

(handlers/register-handler-fx
  :execute-command-immediately
  [re-frame/trim-v]
  (fn [_ [{command-name :name}]]
    (case (keyword command-name)
      :grant-permissions
      {:dispatch [:request-permissions
                  [:read-external-storage]
                  #(re-frame/dispatch [:initialize-geth])]}
      (log/debug "ignoring command: " command-name))))
