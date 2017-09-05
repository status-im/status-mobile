(ns status-im.ui.screens.accounts.login.events
  (:require
    status-im.ui.screens.accounts.login.navigation

    [re-frame.core :refer [dispatch reg-fx]]
    [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
    [taoensso.timbre :as log]
    [status-im.utils.types :refer [json->clj]]
    [status-im.data-store.core :as data-store]
    [status-im.native-module.core :as status]
    [status-im.constants :refer [console-chat-id]]))

;;;; FX

(reg-fx
  ::login
  (fn [[address password]]
    (status/login address password #(dispatch [:login-handler % address]))))

(reg-fx
  ::clear-web-data
  (fn []
    (status/clear-web-data)))

(reg-fx
  ::change-account
  (fn [[address new-account?]]
    (data-store/change-account address new-account?
                               #(dispatch [:change-account-handler % address new-account?]))))

;;;; Handlers

(register-handler-fx
  :open-login
  (fn [{db :db} [_ address photo-path name]]
    {:db       (update db
                       :accounts/login assoc
                       :address address
                       :photo-path photo-path
                       :name name)
     :dispatch [:navigate-to :login]}))

(register-handler-fx
  :login-account
  (fn [{db :db} [_ address password account-creation?]]
    {:db     (-> db
                 (assoc :accounts/account-creation? account-creation?)
                 (assoc-in [:accounts/login :processing] true))
     ::login [address password]}))

(register-handler-fx
  :login-handler
  (fn [{db :db} [_ result address]]
    (let [data (json->clj result)
          error (:error data)
          success (zero? (count error))
          db' (assoc-in db [:accounts/login :processing] false)]
      (log/debug "Logged in account: ")
      (merge
        {:db (if success db' (assoc-in db' [:accounts/login :error] error))}
        (when success
          (let [is-login-screen? (= (:view-id db) :login)
                new-account? (not is-login-screen?)]
            (log/debug "Logged in: " (:view-id db) is-login-screen? new-account?)
            {::clear-web-data nil
             ::change-account [address new-account?]}))))))

(register-handler-fx
  :change-account-handler
  (fn [{db :db} [_ error address new-account?]]
    (if (nil? error)
      {:db         (assoc db :accounts/login {})
       :dispatch-n (concat
                     [[:debug-server-stop]
                      [:set-current-account address]
                      [:initialize-account address]]
                     (if new-account?
                       [[:navigate-to-clean :chat-list]
                        [:navigate-to :chat console-chat-id]]
                       [[:navigate-to-clean :chat-list]
                        [:navigate-to :chat-list]]))}
      (log/debug "Error changing acount: " error))))
