(ns status-im.ui.screens.accounts.login.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.accounts.login.models :as models]
            [status-im.utils.handlers :as handlers]))

;;;; FX
(re-frame/reg-fx
 :login
 (fn [[address password save-password?]]
   (models/login! address password save-password?)))

(re-frame/reg-fx
 :clear-web-data
 models/clear-web-data!)

(re-frame/reg-fx
 :data-store/change-account
 (fn [address]
   (models/change-account! address)))

;;;; Handlers
(handlers/register-handler-fx
 :ui/login
 (fn [cofx _]
   (models/user-login cofx)))

(handlers/register-handler-fx
 :callback/login
 (fn [cofx [_ login-result]]
   (models/user-login-callback login-result cofx)))

(handlers/register-handler-fx
 :ui/open-login
 (fn [cofx [_ address photo-path name]]
   (models/open-login address photo-path name cofx)))

(handlers/register-handler-fx
 :callback/open-login
 (fn [cofx [_ password]]
   (models/open-login-callback password cofx)))
