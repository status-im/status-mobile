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
 :recover/set-phrase
 (fn [cofx [_ recovery-phrase]]
   (models/set-phrase recovery-phrase cofx)))

(handlers/register-handler-fx
 :recover/validate-phrase
 (fn [cofx _]
   (models/validate-phrase cofx)))

(handlers/register-handler-fx
 :recover/set-password
 (fn [cofx [_ masked-password]]
   (models/set-password masked-password cofx)))

(handlers/register-handler-fx
 :recover/validate-password
 (fn [cofx _]
   (models/validate-password cofx)))

(handlers/register-handler-fx
 :account-recovered
 [(re-frame/inject-cofx :get-signing-phrase) (re-frame/inject-cofx :get-status)]
 (fn [cofx [_ result password]]
   (models/on-account-recovered result password cofx)))

(handlers/register-handler-fx
 :recover-account
 (fn [cofx _]
   (models/recover-account cofx)))

(handlers/register-handler-fx
 :recover-account-with-checks
 (fn [cofx _]
   (models/recover-account-with-checks cofx)))
