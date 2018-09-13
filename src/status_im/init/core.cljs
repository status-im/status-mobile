(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.realm.core :as realm]
            [status-im.i18n :as i18n]
            [status-im.models.browser :as browser]
            [status-im.models.contacts :as models.contacts]
            [status-im.models.dev-server :as models.dev-server]
            [status-im.protocol.core :as protocol]
            [status-im.models.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.notifications.core :as notifications]
            [status-im.ui.screens.contacts.events :as contacts]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

(defn init-store!
  "Try to decrypt the database, move on if successful otherwise go back to
  initial state"
  [encryption-key]
  (.. (data-store/init encryption-key)
      (then #(re-frame/dispatch [:init.callback/init-store-success]))
      (catch (fn [error]
               (log/warn "Could not decrypt database" error)
               (re-frame/dispatch [:init.callback/init-store-error encryption-key])))))

(defn testfairy-alert! []
  (when config/testfairy-enabled?
    (utils/show-popup
     (i18n/label :testfairy-title)
     (i18n/label :testfairy-message))))

;; TODO (yenda) move keychain functions to dedicated namespace
(defn reset-keychain! []
  (.. (keychain/reset)
      (then
       #(re-frame/dispatch [:init.callback/keychain-reset]))))

(defn reset-data!  []
  (.. (realm/delete-realms)
      (then reset-keychain!)
      (catch reset-keychain!)))

(defn initialize-keychain
  "Entrypoint, fetches the key from the keychain and initialize the app"
  [cofx]
  {:keychain/get-encryption-key [:init.callback/get-encryption-key-success]})

(defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys [status-module-initialized? status-node-started? view-id
            network-status network peers-count peers-summary device-UUID]
     :or   {network (get app-db :network)}} :db}]
  {:db (assoc app-db
              :contacts/contacts {}
              :network-status network-status
              :peers-count (or peers-count 0)
              :peers-summary (or peers-summary [])
              :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
              :status-node-started? status-node-started?
              :network network
              :device-UUID device-UUID
              :view-id view-id)})

(defn initialize-app
  [encryption-key cofx]
  (handlers-macro/merge-fx cofx
                           {:init/get-device-UUID                           nil
                            :init/init-store                                encryption-key
                            :ui/listen-to-window-dimensions-change          nil
                            :init/testfairy-alert                           nil
                            :notifications/handle-initial-push-notification nil
                            :network/listen-to-network-status               nil
                            :network/listen-to-connection-status            nil
                            :hardwallet/check-nfc-support                   nil
                            :hardwallet/check-nfc-enabled                   nil}
                           (initialize-app-db)
                           (node/start)))

(defn set-device-uuid [device-uuid {:keys [db]}]
  {:db (assoc db :device-UUID device-uuid)})

(defn handle-change-account-error
  [cofx]
  {:ui/show-confirmation
   {:title               (i18n/label :invalid-key-title)
    :content             (i18n/label :invalid-key-content)
    :confirm-button-text (i18n/label :invalid-key-confirm)
    ;; On cancel we initialize the app with the invalid key, to allow the user
    ;; to recover the seed phrase
    :on-cancel           #(re-frame/dispatch [:init.ui/data-reset-cancelled ""])
    :on-accept           #(re-frame/dispatch [:init.ui/data-reset-accepted])}})

(defn handle-init-store-error
  [encryption-key cofx]
  {:ui/show-confirmation
   {:title               (i18n/label :decryption-failed-title)
    :content             (i18n/label :decryption-failed-content)
    :confirm-button-text (i18n/label :decryption-failed-confirm)
    ;; On cancel we initialize the app with the same key, in case the error was
    ;; not related/fs error
    :on-cancel           #(re-frame/dispatch [:init.ui/data-reset-cancelled encryption-key])
    :on-accept           #(re-frame/dispatch [:init.ui/data-reset-accepted])}})

(defn load-accounts [{:keys [db all-accounts]}]
  (let [accounts (->> all-accounts
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    {:db (assoc db :accounts/accounts accounts)}))

(defn initialize-views [cofx]
  (let [{{:accounts/keys [accounts] :as db} :db} cofx]
    (if (empty? accounts)
      (navigation/navigate-to-clean :intro cofx)
      (let [{:keys [address photo-path name]} (first (sort-by :last-sign-in > (vals accounts)))]
        (accounts.login/open-login address photo-path name cofx)))))

(defn load-accounts-and-initialize-views
  "DB has been decrypted, load accounts and initialize-view"
  [cofx]
  (handlers-macro/merge-fx cofx
                           (load-accounts)
                           (initialize-views)))

(defn initialize-account-db [address {:keys [db web3]}]
  (let [{:universal-links/keys [url]
         :keys [accounts/accounts accounts/create contacts/contacts networks/networks
                network network-status peers-count peers-summary view-id navigation-stack
                status-module-initialized? status-node-started? device-UUID semaphores]
         :or   {network (get app-db :network)}} db
        current-account (get accounts address)
        account-network-id (get current-account :network network)
        account-network (get-in current-account [:networks account-network-id])]
    {:db (cond-> (assoc app-db
                        :current-public-key (:public-key current-account)
                        :view-id view-id
                        :navigation-stack navigation-stack
                        :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                        :status-node-started? status-node-started?
                        :accounts/create create
                        :networks/networks networks
                        :account/account current-account
                        :network-status network-status
                        :network network
                        :chain (ethereum/network->chain-name account-network)
                        :universal-links/url url
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :semaphores semaphores
                        :web3 web3)
           (= view-id :create-account)
           (assoc-in [:accounts/create :step] :enter-name))}))

(defn initialize-wallet [cofx]
  (when-not platform/desktop?
    (handlers-macro/merge-fx cofx
                             (models.wallet/update-wallet)
                             (transactions/run-update)
                             (transactions/start-sync))))

(defn login-only-events [address {:keys [db] :as cofx}]
  (when (not= (:view-id db) :create-account)
    (handlers-macro/merge-fx cofx
                             {:notifications/request-notifications-permissions nil}
                             (navigation/navigate-to-cofx :home nil)
                             (universal-links/process-stored-event)
                             (notifications/process-stored-event address))))

(defn initialize-account [address {:keys [web3] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:web3/set-default-account    [web3 address]
                            :web3/fetch-node-version     [web3
                                                          #(re-frame/dispatch
                                                            [:web3/fetch-node-version-callback %])]
                            :notifications/get-fcm-token nil}
                           (initialize-account-db address)
                           (protocol/initialize-protocol address)
                           (models.contacts/load-contacts)
                           (models.dev-server/start-if-needed)
                           (chat-loading/initialize-chats)
                           (chat-loading/initialize-pending-messages)
                           (browser/initialize-browsers)
                           (browser/initialize-dapp-permissions)
                           (initialize-wallet)
                           (accounts.update/update-sign-in-time)
                           (login-only-events address)))

(re-frame/reg-fx
 :init/init-store
 init-store!)

(re-frame/reg-fx
 :init/status-module-initialized
 status/module-initialized!)

(re-frame/reg-fx
 :init/testfairy-alert
 testfairy-alert!)

(re-frame/reg-fx
 :init/get-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:init.callback/get-device-UUID-success %]))))

(re-frame/reg-fx
 :init/reset-data
 reset-data!)
