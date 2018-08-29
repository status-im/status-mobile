(ns status-im.ui.screens.accounts.events
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.accounts.create.navigation
            [status-im.ui.screens.accounts.models :as models]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.handlers :as handlers]))

;;;; COFX

(re-frame/reg-cofx
 :get-signing-phrase
 (fn [cofx _]
   (models/get-signing-phrase cofx)))

(re-frame/reg-cofx
 :get-status
 (fn [cofx _]
   (models/get-status cofx)))

;;;; FX

(re-frame/reg-fx
 :create-account
 models/create-account!)

;;;; Handlers

(handlers/register-handler-fx
 :create-account
 (fn [cofx _]
   (models/create-account cofx)))

(handlers/register-handler-fx
 :account-created
 [(re-frame/inject-cofx :get-signing-phrase) (re-frame/inject-cofx :get-status)]
 (fn [cofx [_ result password]]
   (models/on-account-created result password false cofx)))

(handlers/register-handler-fx
 :account-set-name
 (fn [cofx _]
   (models/account-set-name cofx)))

(handlers/register-handler-fx
 :account-set-input-text
 (fn [cofx [_ input-key text]]
   (models/account-set-input-text input-key text cofx)))

(handlers/register-handler-fx
 :update-mainnet-warning-shown
 (fn [cofx _]
   (accounts.utils/account-update {:mainnet-warning-shown? true} cofx)))

(handlers/register-handler-fx
 :show-mainnet-is-default-alert
 (fn [cofx]
   (models/show-mainnet-is-default-alert cofx)))

(handlers/register-handler-fx
 :reset-account-creation
 (fn [cofx _]
   (models/reset-account-creation cofx)))

(handlers/register-handler-fx
 :switch-dev-mode
 (fn [cofx [_ dev-mode]]
   (accounts.utils/account-update {:dev-mode? dev-mode} cofx)))

(handlers/register-handler-fx
 :wallet-set-up-passed
 (fn [cofx [_ modal?]]
   (models/wallet-set-up-passed modal? cofx)))
