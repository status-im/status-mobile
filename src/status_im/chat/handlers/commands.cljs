(ns status-im.chat.handlers.commands
  (:require [cljs.reader :as reader]
            [clojure.string :as str]
            [re-frame.core :refer [enrich after dispatch]]
            [status-im.data-store.messages :as messages]
            [status-im.utils.handlers :as handlers]
            [status-im.components.status :as status]
            [status-im.chat.constants :as const]
            [status-im.commands.utils :as cu]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(defn generate-context [{:keys [current-account-id chats] :as db} chat-id to]
  (merge {:platform platform/platform
          :from     current-account-id
          :to       to
          :chat     {:chat-id    chat-id
                     :group-chat (get-in chats [chat-id :group-chat])}}
         i18n/delimeters))

(handlers/register-handler :request-command-data
  (handlers/side-effect!
    (fn [{:keys [current-account-id chats]
          :contacts/keys [contacts] :as db}
         [_ {{:keys [command params content-command type]} :content
             :keys [message-id chat-id jail-id on-requested from] :as message} data-type]]
      (let [jail-id  (or jail-id chat-id)
            jail-id' (if (get-in chats [jail-id :group-chat])
                       (get-in chats [jail-id :command-suggestions (keyword command) :owner-id])
                       jail-id)]
        (if-not (get-in contacts [jail-id' :commands-loaded?])
          (do (dispatch [:add-commands-loading-callback
                         jail-id'
                         #(dispatch [:request-command-data message data-type])])
              (dispatch [:load-commands! jail-id']))
          (let [path     [(if (= :response (keyword type)) :responses :commands)
                          (if content-command content-command command)
                          data-type]
                to       (get-in contacts [chat-id :address])
                params   {:parameters params
                          :context    (generate-context db chat-id to)}
                callback #(let [result (get-in % [:result :returned])
                                result' (if (:markup result)
                                         (update result :markup cu/generate-hiccup)
                                         result)] 
                            (dispatch [:set-in [:message-data data-type message-id] result'])
                            (when (and result (= :preview data-type))
                              ;; update message in realm with serialized preview
                              (messages/update {:message-id message-id
                                                :preview (prn-str result)}))
                            (when on-requested (on-requested result')))]
            ;chat-id path params callback lock? type
            (status/call-jail {:jail-id  jail-id'
                               :path     path
                               :params   params
                               :callback callback})))))))

(handlers/register-handler :execute-command-immediately
  (handlers/side-effect!
    (fn [_ [_ {command-name :name :as command}]]
      (case (keyword command-name)
        :grant-permissions
        (dispatch [:request-permissions
                   [:read-external-storage]
                   #(dispatch [:initialize-geth])])
        (log/debug "ignoring command: " command)))))

(handlers/register-handler :request-command-preview
  (handlers/side-effect!
    (fn [db [_ {:keys [message-id] :as message}]]
      (let [previews (get-in db [:message-data :preview])]
        (when-not (contains? previews message-id)
          (let [{serialized-preview :preview} (messages/get-by-id message-id)]
            ;; if preview is already cached in db, do not request it from jail
            ;; and write it directly to message-data path
            (if serialized-preview
              (dispatch [:set-in [:message-data :preview message-id]
                         (-> serialized-preview
                             reader/read-string
                             (update :markup cu/generate-hiccup))])
              (dispatch [:request-command-data message :preview]))))))))
