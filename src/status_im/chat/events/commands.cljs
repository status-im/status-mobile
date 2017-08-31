(ns status-im.chat.events.commands
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [re-frame.core :refer [reg-fx reg-cofx inject-cofx dispatch trim-v]]
            [taoensso.timbre :as log]
            [status-im.data-store.messages :as msg-store]
            [status-im.utils.handlers :refer [register-handler-fx]]
            [status-im.chat.constants :as const]
            [status-im.commands.utils :as commands-utils]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]))

;;;; Helper fns

(defn generate-context
  "Generates context for jail call"
  [{:keys [chats] :accounts/keys [current-account-id]} chat-id to]
  (merge {:platform platform/platform
          :from     current-account-id
          :to       to
          :chat     {:chat-id    chat-id
                     :group-chat (get-in chats [chat-id :group-chat])}}
         i18n/delimeters))

;;;; Coeffects

(reg-cofx
 ::get-persisted-message
 (fn [coeffects _]
   (assoc coeffects :get-persisted-message msg-store/get-by-id)))

;;;; Effects

(reg-fx
  ::update-persisted-message
  (fn [message]
    (msg-store/update message)))

;;;; Handlers

(register-handler-fx
  ::jail-command-data-response
  [trim-v]
  (fn [{:keys [db]} [{{:keys [returned]} :result} {:keys [message-id on-requested]} data-type]]
    (cond-> {}
      returned
      (assoc :db (assoc-in db [:message-data data-type message-id] returned))
      (and returned
           (= :preview data-type))
      (assoc ::update-persisted-message {:message-id message-id
                                         :preview (prn-str returned)})
      on-requested
      (assoc :dispatch (on-requested returned)))))

(register-handler-fx
  :request-command-data
  [trim-v]
  (fn [{:keys [db]}
       [{{:keys [command content-command params type]} :content
         :keys [chat-id jail-id] :as message}
        data-type]]
    (let [{:keys [chats]
           :accounts/keys [current-account-id]
           :contacts/keys [contacts]} db
          jail-id (or jail-id chat-id)
          jail-id (if (get-in chats [jail-id :group-chat])
                    (get-in chats [jail-id :command-suggestions (keyword command) :owner-id])
                    jail-id)]
      (if (get-in contacts [jail-id :commands-loaded?])
        (let [path          [(if (= :response (keyword type)) :responses :commands)
                             (or content-command command)
                             data-type]
              to            (get-in contacts [chat-id :address])
              jail-params   {:parameters params
                             :context (generate-context db chat-id to)}]
          {:call-jail {:jail-id jail-id
                       :path path
                       :params jail-params
                       :callback-events-creator (fn [jail-response]
                                                  [[::jail-command-data-response
                                                    jail-response message data-type]])}})
        {:dispatch-n [[:add-commands-loading-callback jail-id
                       #(dispatch [:request-command-data message data-type])]
                      [:load-commands! jail-id]]}))))

(register-handler-fx
  :execute-command-immediately
  [trim-v]
  (fn [_ [{command-name :name :as command}]]
    (case (keyword command-name)
      :grant-permissions
      {:dispatch [:request-permissions
                  [:read-external-storage]
                  #(dispatch [:initialize-geth])]}
      (log/debug "ignoring command: " command))))

(register-handler-fx
  :request-command-preview
  [trim-v (inject-cofx ::get-persisted-message)]
  (fn [{:keys [db get-persisted-message]} [{:keys [message-id] :as message}]]
    (let [previews (get-in db [:message-data :preview])]
      (when-not (contains? previews message-id)
        (let [{serialized-preview :preview} (get-persisted-message message-id)]
          ;; if preview is already cached in db, do not request it from jail
          ;; and write it directly to message-data path
          (if serialized-preview
            {:db (assoc-in db
                           [:message-data :preview message-id]
                           (reader/read-string serialized-preview))}
            {:dispatch [:request-command-data message :preview]}))))))
