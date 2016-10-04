(ns status-im.accounts.login.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [taoensso.timbre :as log]
            [status-im.utils.types :refer [json->clj]]
            [status-im.db :refer [default-view]]
            [status-im.persistence.realm.core :as realm]
            [status-im.components.status :as status]
            [status-im.constants :refer [console-chat-id]]))


(defn set-login-from-qr
  [{:keys [login] :as db} [_ _ login-info]]
  (assoc db :login (merge login login-info)))

(register-handler :set-login-from-qr set-login-from-qr)

(defn initialize-account
  [address new-account?]
  (dispatch [:set :login {}])
  (dispatch [:set-current-account address])
  (dispatch [:initialize-account address])
  (if new-account?
    (do
      (dispatch [:navigate-to-clean :chat-list])
      (dispatch [:navigate-to :chat console-chat-id]))
    (do
      (dispatch [:navigate-to-clean :accounts])
      (dispatch [:navigate-to default-view]))))

(register-handler
  :change-realm-account
  (u/side-effect!
    (fn [db [_ address new-account? callback]]
      (realm/change-account-realm address new-account?
                                  #(callback % address new-account?)))))

(defn on-account-changed
  [error address new-account?]
  (if (nil? error)
    (initialize-account address new-account?)
    (log/debug "Error changing acount realm: " error)))

(defn logged-in
  [db address]
  (let [is-login-screen? (= (:view-id db) :login)
        new-account? (not is-login-screen?)]
    (log/debug "Logged in: ")
    (dispatch [:change-realm-account address new-account? on-account-changed])))

(register-handler
  :login-account
  (u/side-effect!
    (fn [db [_ address password]]
      (status/login address password
                    (fn [result]
                      (let [data (json->clj result)
                            error (:error data)
                            success (zero? (count error))]
                        (log/debug "Logged in account: ")
                        (if success
                          (logged-in db address)
                          (dispatch [:set-in [:login :error] error]))))))))
