(ns status-im.accounts.login.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [taoensso.timbre :as log]
            [status-im.utils.types :refer [json->clj]]
            [status-im.data-store.core :as data-store]
            [status-im.components.status :as status]
            [status-im.constants :refer [console-chat-id]]
            [status-im.navigation.handlers :as nav]))

(defmethod nav/preload-data! :login
  [db]
  (update db :login dissoc :error :password))

(defn set-login-from-qr
  [{:keys [login] :as db} [_ _ login-info]]
  (assoc db :login (merge login login-info)))

(register-handler :set-login-from-qr set-login-from-qr)

(defn initialize-account
  [address new-account?]
  (dispatch [:set :login {}])
  (dispatch [:debug-server-stop])
  (dispatch [:set-current-account address])
  (dispatch [:initialize-account address])
  (if new-account?
    (do
      (dispatch [:navigate-to-clean :chat-list])
      (dispatch [:navigate-to :chat console-chat-id]))
    (do
      (dispatch [:navigate-to-clean :chat-list])
      (dispatch [:navigate-to :chat-list]))))

(register-handler
  :change-account
  (u/side-effect!
    (fn [_ [_ address new-account? callback]]
      (status/clear-web-data)
      (data-store/change-account address new-account?
                                 #(callback % address new-account?)))))

(defn on-account-changed
  [error address new-account?]
  (if (nil? error)
    (initialize-account address new-account?)
    (log/debug "Error changing acount: " error)))

(defn logged-in
  [db address]
  (let [is-login-screen? (= (:view-id db) :login)
        new-account? (not is-login-screen?)]
    (log/debug "Logged in: " (:view-id db) is-login-screen? new-account?)
    (dispatch [:change-account address new-account? on-account-changed])))

(register-handler
  :login-account
  (after
    (fn [db [_ address password]]
      (status/login address password
                    (fn [result]
                      (let [data (json->clj result)
                            error (:error data)
                            success (zero? (count error))]
                        (log/debug "Logged in account: ")
                        (if success
                          (logged-in db address)
                          (dispatch [:set-in [:login :error] error])))))))
  (fn [db [_ _ _ account-creation?]]
    (assoc db :account-creation? account-creation?)))
