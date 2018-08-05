(ns status-im.ui.screens.accounts.login.events
  (:require status-im.ui.screens.accounts.login.navigation
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.login.models :as models]))

;;;; FX

(re-frame/reg-fx :stop-node models/stop-node!)

(re-frame/reg-fx
 :login
 (fn [[address password]]
   (models/login! address password)))

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
 :login-account-internal
 (fn [cofx [_ address password]]
   (models/login-account-internal address password cofx)))

(handlers/register-handler-fx
 :start-node
 (fn [cofx [_ address password]]
   (models/start-node address password cofx)))

(handlers/register-handler-fx
 :login-account
 (fn [cofx [_ address password]]
   (models/login-account address password cofx)))

(handlers/register-handler-fx
 :login-handler
 (fn [cofx [_ login-result address]]
   (models/login-handler login-result address cofx)))

(handlers/register-handler-fx
 :change-account-handler
 (fn [cofx [_ address]]
   (models/change-account-handler address cofx)))
