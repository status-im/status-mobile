(ns status-im.init.events
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.core :as data-store]
            [status-im.i18n :as i18n]
            [status-im.init.core :as init]
            status-im.web3.events
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;; Try to decrypt the database, move on if successful otherwise go back to
;; initial state
(re-frame/reg-fx
 :init/init-store
 (fn [encryption-key]
   (.. (data-store/init encryption-key)
       (then #(re-frame/dispatch [:init/after-decryption]))
       (catch (fn [error]
                (log/warn "Could not decrypt database" error)
                (re-frame/dispatch [:init/initialize-app encryption-key :decryption-failed]))))))

(re-frame/reg-fx
 :init/initialize-geth
 (fn [config]
   (status/start-node (types/clj->json config) config/fleet)))

(re-frame/reg-fx
 :init/status-module-initialized
 (fn [_]
   (status/module-initialized!)))

(re-frame/reg-fx
 :init/testfairy-alert
 (fn [_]
   (when config/testfairy-enabled?
     (utils/show-popup
      (i18n/label :testfairy-title)
      (i18n/label :testfairy-message)))))

(re-frame/reg-fx
 :init/init-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:init/set-device-UUID %]))))

;; Entrypoint, fetches the key from the keychain and initialize the app
(handlers/register-handler-fx
 :init/initialize-keychain
 (fn [cofx _]
   (init/initialize-keychain cofx)))

;; Check the key is valid, shows options if not, otherwise continues loading
;; the database
(handlers/register-handler-fx
 :init/initialize-app
 (fn [cofx [_ encryption-key error]]
   (init/initialize-app encryption-key error cofx)))

;; DB has been decrypted, load accounts, initialize geth, etc
(handlers/register-handler-fx
 :init/after-decryption
 [(re-frame/inject-cofx :data-store/get-all-accounts)]
 (fn [cofx _]
   (init/after-decryption cofx)))

(handlers/register-handler-fx
 :init/initialize-account
 [(re-frame/inject-cofx :web3/get-web3)
  (re-frame/inject-cofx :get-default-contacts)
  (re-frame/inject-cofx :get-default-dapps)
  (re-frame/inject-cofx :data-store/all-chats)
  (re-frame/inject-cofx :data-store/get-messages)
  (re-frame/inject-cofx :data-store/get-user-statuses)
  (re-frame/inject-cofx :data-store/unviewed-messages)
  (re-frame/inject-cofx :data-store/message-ids)
  (re-frame/inject-cofx :data-store/get-unanswered-requests)
  (re-frame/inject-cofx :data-store/get-local-storage-data)
  (re-frame/inject-cofx :data-store/get-all-contacts)
  (re-frame/inject-cofx :data-store/get-all-mailservers)
  (re-frame/inject-cofx :data-store/transport)
  (re-frame/inject-cofx :data-store/all-browsers)
  (re-frame/inject-cofx :data-store/all-dapp-permissions)]
 (fn [cofx [_ address events-after]]
   (init/initialize-account address events-after cofx)))

(handlers/register-handler-fx
 :init/initialize-geth
 (fn [cofx _]
   (init/initialize-geth cofx)))

(handlers/register-handler-fx
 :init/set-device-UUID
 (fn [cofx [_ device-uuid]]
   (init/set-device-uuid device-uuid cofx)))
