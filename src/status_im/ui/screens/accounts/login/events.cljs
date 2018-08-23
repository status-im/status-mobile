(ns status-im.ui.screens.accounts.login.events
  (:require status-im.ui.screens.accounts.login.navigation
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.login.models :as models]))

;;;; FX

(re-frame/reg-fx :stop-node models/stop-node!)

(re-frame/reg-fx
 :login
 (fn [[address password save-password]]
   (models/login! address password save-password)))

(re-frame/reg-fx
 :clear-web-data
 models/clear-web-data!)

(re-frame/reg-fx
 :change-account
 (fn [[address]]
   (models/handle-change-account! address)))

;;;; Handlers

(handlers/register-handler-fx
 :open-login
 (fn [cofx [_ address photo-path name]]
   (models/open-login address photo-path name cofx)))

(handlers/register-handler-fx
 :do-login
 (fn [cofx [_ address photo-path name password]]
   (handlers-macro/merge-fx cofx
                            (models/navigate-to-login address photo-path name password)
                            ;; models/login-account takes care about empty password
                            (models/login-account address password (not (empty? password))))))

(handlers/register-handler-fx
 :login-account-internal
 (fn [cofx [_ address password save-password]]
   (models/login-account-internal address password save-password cofx)))

(handlers/register-handler-fx
 :start-node
 (fn [cofx [_ address password save-password]]
   (models/start-node address password save-password cofx)))

(handlers/register-handler-fx
 :login-account
 (fn [cofx [_ address password save-password]]
   (models/login-account address password save-password cofx)))

(handlers/register-handler-fx
 :login-handler
 (fn [cofx [_ login-result address password save-password]]
   (models/login-handler login-result address password save-password cofx)))

(handlers/register-handler-fx
 :change-account-handler
 (fn [cofx [_ address]]
   (models/change-account-handler address cofx)))

