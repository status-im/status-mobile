(ns status-im.accounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

;; login flow:
;;
;; - event `:ui/login` is dispatched
;; - node is initialized with user config or default config
;; - `node.started` signal is received, applying `:login` fx
;; - `:callback/login` event is dispatched, account is changed in datastore, web-data is cleared
;; - `:init.callback/account-change-success` event is dispatched

(defn login! [address password save-password?]
  (status/login address password #(re-frame/dispatch [:accounts.login.callback/login-success %])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn change-account! [address]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then (fn [encryption-key]
              (data-store/change-account address encryption-key)
              (re-frame/dispatch [:init.callback/account-change-success address])))
      (catch (fn [error]
               (log/warn "Could not change account" error)
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [:init.callback/account-change-error])))))

;;;; Handlers
(defn login [cofx]
  (let [{:keys [address password save-password?]} (accounts.db/credentials cofx)]
    {:accounts.login/login [address password save-password?]}))

(defn user-login [{:keys [db] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:db (assoc-in db [:accounts/login :processing] true)}
                           (node/initialize (get-in db [:accounts/login :address]))))

(defn user-login-callback [login-result {db :db :as cofx}]
  (let [data    (types/json->clj login-result)
        error   (:error data)
        success (empty? error)]
    (if success
      (let [{:keys [address password save-password?]} (accounts.db/credentials cofx)]
        (merge {:accounts.login/clear-web-data nil
                :data-store/change-account address}
               (when save-password?
                 {:keychain/save-user-password [address password]})))
      {:db (update db :accounts/login assoc
                   :error error
                   :processing false)})))

(defn open-login [address photo-path name {:keys [db]}]
  {:db (-> db
           (update :accounts/login assoc
                   :address address
                   :photo-path photo-path
                   :name name)
           (update :accounts/login dissoc
                   :error
                   :password))
   :keychain/can-save-user-password? nil
   :keychain/get-user-password [address
                                #(re-frame/dispatch [:accounts.login.callback/get-user-password-success %])]})

(defn open-login-callback
  [password {:keys [db] :as cofx}]
  (if password
    (handlers-macro/merge-fx cofx
                             {:db (assoc-in db [:accounts/login :password] password)}
                             (navigation/navigate-to-cofx :progress nil)
                             (user-login))
    (navigation/navigate-to-cofx :login nil cofx)))

(re-frame/reg-fx
 :accounts.login/login
 (fn [[address password save-password?]]
   (login! address password save-password?)))

(re-frame/reg-fx
 :accounts.login/clear-web-data
 clear-web-data!)

(re-frame/reg-fx
 :data-store/change-account
 (fn [address]
   (change-account! address)))
