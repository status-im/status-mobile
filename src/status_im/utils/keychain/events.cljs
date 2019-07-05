(ns status-im.utils.keychain.events
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.handlers :as handlers]))

(defn handle-key-error [event {:keys [error key]}]
  (if (= :weak-key error)
    (log/warn "weak key used, database might not be encrypted properly")
    (log/warn "invalid key detected"))
  (re-frame/dispatch (into [] (concat event [(or key "")
                                             (or error :invalid-key)]))))

(handlers/register-handler-fx
 :keychain.callback/can-save-user-password?-success
 (fn [{:keys [db]} [_ can-save-user-password?]]
   {:db (assoc-in db [:accounts/login :can-save-password?] can-save-user-password?)}))
