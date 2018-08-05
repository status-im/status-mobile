(ns status-im.ui.screens.accounts.recover.events
  (:require status-im.ui.screens.accounts.recover.navigation
            [re-frame.core :as re-frame]
            [status-im.ui.screens.accounts.recover.models :as models]
            [status-im.utils.handlers :as handlers]))

;;;; FX

(re-frame/reg-fx
 :recover-account-fx
 (fn [[masked-passphrase password]]
   (models/recover-account-fx! masked-passphrase password)))

;;;; Handlers

(handlers/register-handler-fx
 :account-recovered
 (fn [cofx [_ result password]]
   (models/on-account-recovered result password cofx)))

(handlers/register-handler-fx
 :account-recovered-navigate
 (fn [cofx]
   (models/account-recovered-navigate cofx)))

(handlers/register-handler-fx
 :recover-account
 (fn [cofx [_ masked-passphrase password]]
   (models/recover-account masked-passphrase password cofx)))
