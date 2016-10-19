(ns status-im.chat.handlers.console
  (:require [re-frame.core :refer [dispatch dispatch-sync after]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.constants :refer [console-chat-id]]
            [status-im.data-store.messages :as messages]))

(def console-commands
  {:password
   (fn [params _]
     (dispatch [:create-account (params "password")]))

   :phone
   (fn [params id]
     (dispatch [:sign-up (params "phone") id]))

   :confirmation-code
   (fn [params id]
     (dispatch [:sign-up-confirm (params "code") id]))})

(def commands-names (set (keys console-commands)))

(def commands-with-delivery-status
  (disj commands-names :password))

(register-handler :invoke-console-command-handler!
  (u/side-effect!
    (fn [_ [_ {:keys [staged-command] :as parameters}]]
      (let [{:keys [id command params]} staged-command
            {:keys [name]} command]
        (dispatch [:prepare-command! parameters])
        ((console-commands (keyword name)) params id)))))

(register-handler :set-message-status
  (after
    (fn [_ [_ message-id status]]
      (messages/update {:message-id     message-id
                        :message-status status})))
  (fn [db [_ message-id status]]
    (assoc-in db [:message-statuses message-id] {:status status})))
