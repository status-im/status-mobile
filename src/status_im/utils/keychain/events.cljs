(ns status-im.utils.keychain.events
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :keychain.callback/can-save-user-password?-success
 (fn [{:keys [db]} [_ can-save-user-password?]]
   {:db (assoc-in db [:multiaccounts/login :can-save-password?] can-save-user-password?)}))
