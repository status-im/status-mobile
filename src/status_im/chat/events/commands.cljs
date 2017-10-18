(ns status-im.chat.events.commands
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.handlers :as handlers]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.chat.models.commands :as commands-model]))

;;;; Helper fns

(defn- generate-context
  "Generates context for jail call"
  [{:keys [chats] :accounts/keys [current-account-id]} chat-id to group-id]
  (merge {:platform     platform/platform
          :from         current-account-id
          :to           to
          :chat         {:chat-id    chat-id
                         :group-chat (or (get-in chats [chat-id :group-chat])
                                         (not (nil? group-id)))}}
         i18n/delimeters))

(defn request-command-message-data
  "Requests command message data from jail"
  [db
   {{command-name         :command
     content-command-name :content-command
     :keys                [content-command-scope
                           scope
                           params
                           type
                           bot]} :content
    :keys                        [chat-id jail-id group-id] :as message}
   data-type]
  (let [{:keys          [chats]
         :accounts/keys [current-account-id]
         :contacts/keys [contacts]} db
        jail-id (or bot jail-id chat-id)
        jail-id (if (get-in chats [jail-id :group-chat])
                  (get-in chats [jail-id :possible-commands (keyword command-name) :owner-id])
                  jail-id)]
    (if (get-in contacts [jail-id :commands-loaded?])
      (let [path        [(if (= :response (keyword type)) :responses :commands)
                         [(if content-command-name content-command-name command-name)
                          (commands-model/scope->bit-mask (or scope content-command-scope))]
                         data-type]
            to          (get-in contacts [chat-id :address])
            jail-params {:parameters params
                         :context    (generate-context db chat-id to group-id)}]
        {:call-jail {:jail-id                 jail-id
                     :path                    path
                     :params                  jail-params
                     :callback-events-creator (fn [jail-response]
                                                [[::jail-command-data-response
                                                  jail-response message data-type]])}})
      {:dispatch-n [[:add-commands-loading-callback jail-id
                     #(re-frame/dispatch [:request-command-message-data message data-type])]
                    [:load-commands! jail-id]]})))

;;;; Handlers

(handlers/register-handler-fx
  ::jail-command-data-response
  [re-frame/trim-v]
  (fn [{:keys [db]} [{{:keys [returned]} :result} {:keys [message-id on-requested]} data-type]]
    (cond-> {}
      returned
      (assoc :db (assoc-in db [:message-data data-type message-id] returned))
      (and returned
           (= :preview data-type))
      (assoc :update-message {:message-id message-id
                              :preview (prn-str returned)})
      on-requested
      (assoc :dispatch (on-requested returned)))))

(handlers/register-handler-fx
  :request-command-message-data
  [re-frame/trim-v]
  (fn [{:keys [db]} [message data-type]]
    (request-command-message-data db message data-type)))

(handlers/register-handler-fx
  :execute-command-immediately
  [re-frame/trim-v]
  (fn [_ [{command-name :name :as command}]]
    (case (keyword command-name)
      :grant-permissions
      {:dispatch [:request-permissions
                  [:read-external-storage]
                  #(re-frame/dispatch [:initialize-geth])]}
      (log/debug "ignoring command: " command))))

(handlers/register-handler-fx
  :request-command-preview
  [re-frame/trim-v (re-frame/inject-cofx :get-stored-message)]
  (fn [{:keys [db get-stored-message]} [{:keys [message-id] :as message}]]
    (let [previews (get-in db [:message-data :preview])]
      (when-not (contains? previews message-id)
        (let [{serialized-preview :preview} (get-stored-message message-id)]
          ;; if preview is already cached in db, do not request it from jail
          ;; and write it directly to message-data path
          (if serialized-preview
            {:db (assoc-in db
                           [:message-data :preview message-id]
                           (reader/read-string serialized-preview))}
            (request-command-message-data db message :preview)))))))
