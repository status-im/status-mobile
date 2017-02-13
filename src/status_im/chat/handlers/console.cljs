(ns status-im.chat.handlers.console
  (:require [re-frame.core :refer [dispatch dispatch-sync after]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.constants :refer [console-chat-id
                                         text-content-type]]
            [status-im.data-store.messages :as messages]
            [taoensso.timbre :as log]
            [status-im.utils.random :as random]))

(def console-commands
  {:password
   (fn [params _]
     (dispatch [:create-account (params "password")]))

   :phone
   (fn [params id]
     (dispatch [:sign-up (params "phone") id]))

   :confirmation-code
   (fn [params id]
     (dispatch [:sign-up-confirm (params "code") id]))

   :faucet
   (fn [params id]
     (dispatch [:open-faucet (params "url") id]))

   :debug
   (fn [params id]
     (let [debug-on? (= (params "mode") "On")]
       (dispatch [:account-update {:debug? debug-on?}])
       (if debug-on?
         (dispatch [:debug-server-start])
         (dispatch [:debug-server-stop]))))})

(def commands-names (set (keys console-commands)))

(def commands-with-delivery-status
  (disj commands-names :password :faucet :debug))

(register-handler :invoke-console-command-handler!
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id command-message] :as parameters}]]
      (let [{:keys [id command params]} command-message
            {:keys [name]} command]
        (dispatch [:prepare-command! chat-id parameters])
        ((console-commands (keyword name)) params id)))))

(register-handler :set-message-status
  (after
    (fn [_ [_ message-id status]]
      (messages/update {:message-id     message-id
                        :message-status status})))
  (fn [db [_ message-id status]]
    (assoc-in db [:message-data :statuses message-id] {:status status})))

(register-handler :console-respond-command
  (u/side-effect!
    (fn [_ [_ {:keys [command]}]]
      (let [{:keys [command handler-data]} command]
        (when command
          (let [{:keys [name]} command]
            (case name
              "js" (let [{:keys [err data messages]} handler-data
                         content (or err data)]
                     (doseq [message messages]
                       (let [{:keys [message type]} message]
                         (dispatch [:received-message
                                    {:message-id   (random/id)
                                     :content      (str type ": " message)
                                     :content-type text-content-type
                                     :outgoing     false
                                     :chat-id      console-chat-id
                                     :from         console-chat-id
                                     :to           "me"}])))
                     (when content
                       (dispatch [:received-message
                                  {:message-id   (random/id)
                                   :content      (str content)
                                   :content-type text-content-type
                                   :outgoing     false
                                   :chat-id      console-chat-id
                                   :from         console-chat-id
                                   :to           "me"}])))
              (log/debug "ignoring command: " command))))))))


