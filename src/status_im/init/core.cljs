(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.realm.core :as realm]
            [status-im.i18n :as i18n]
            [status-im.models.account :as models.account]
            [status-im.models.browser :as browser]
            [status-im.models.chat :as chat]
            [status-im.models.contacts :as models.contacts]
            [status-im.models.protocol :as models.protocol]
            [status-im.models.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]
            [status-im.node.models :as node]
            [status-im.notifications.core :as notifications]
            [status-im.ui.screens.accounts.login.models :as login]
            [status-im.ui.screens.contacts.events :as contacts]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as universal-links]
            [taoensso.timbre :as log]))

;; TODO (yenda) move keychain functions to dedicated namespace
(defn- reset-keychain []
  (.. (keychain/reset)
      (then
       #(re-frame/dispatch [:init/initialize-keychain]))))

(defn- handle-reset-data  []
  (.. (realm/delete-realms)
      (then reset-keychain)
      (catch reset-keychain)))

(defn handle-invalid-key-parameters [encryption-key]
  {:title               (i18n/label :invalid-key-title)
   :content             (i18n/label :invalid-key-content)
   :confirm-button-text (i18n/label :invalid-key-confirm)
   ;; On cancel we initialize the app with the invalid key, to allow the user
   ;; to recover the seed phrase
   :on-cancel           #(do
                           (log/warn "initializing app with invalid key")
                           (re-frame/dispatch [:init/initialize-app encryption-key]))
   :on-accept           handle-reset-data})

(defn handle-decryption-failed-parameters [encryption-key]
  {:title               (i18n/label :decryption-failed-title)
   :content             (i18n/label :decryption-failed-content)
   :confirm-button-text (i18n/label :decryption-failed-confirm)
   ;; On cancel we initialize the app with the same key, in case the error was
   ;; not related/fs error
   :on-cancel           #(do
                           (log/warn "initializing app with same key after decryption failed")
                           (re-frame/dispatch [:init/initialize-app encryption-key]))
   :on-accept           handle-reset-data})

(defn initialize-keychain [cofx]
  {:keychain/get-encryption-key [:init/initialize-app]})

(defn set-device-uuid [device-uuid {:keys [db]}]
  {:db (assoc db :device-UUID device-uuid)})

(defn initialize-db
  "Initialize db to initial state"
  [{{:keys [status-module-initialized? status-node-started?
            network-status network peers-count peers-summary device-UUID]
     :or {network (get app-db :network)}} :db}]
  {:db (assoc app-db
              :contacts/contacts {}
              :network-status network-status
              :peers-count (or peers-count 0)
              :peers-summary (or peers-summary [])
              :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
              :status-node-started? status-node-started?
              :network network
              :device-UUID device-UUID)})

(defn initialize-app [encryption-key error cofx]
  (cond
    (= :invalid-key error)
    {:show-confirmation (handle-invalid-key-parameters encryption-key)}

    (= :decryption-failed error)
    {:show-confirmation (handle-decryption-failed-parameters encryption-key)}

    :else
    (handlers-macro/merge-fx cofx
                             {:init/init-device-UUID                          nil
                              :init/init-store                                encryption-key
                              :ui/listen-to-window-dimensions-change          nil
                              :init/testfairy-alert                           nil
                              :notifications/handle-initial-push-notification nil}
                             (initialize-db))))

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
        (login/open-login address photo-path name cofx)))))

(defn after-decryption [cofx]
  (handlers-macro/merge-fx cofx
                           {:network/listen-to-network-status
                            [#(re-frame/dispatch [:network/update-connection-status %])
                             #(re-frame/dispatch [:network/update-network-status %])]}
                           (node/start)
                           (load-accounts)
                           (initialize-views)))

(defn initialize-account-db [address {:keys [db web3]}]
  (let [{:universal-links/keys [url]
         :keys [accounts/accounts accounts/create contacts/contacts networks/networks
                network network-status peers-count peers-summary view-id navigation-stack
                status-module-initialized? status-node-started? device-UUID semaphores]
         :or   {network (get app-db :network)}} db
        console-contact (get contacts constants/console-chat-id)
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
           (assoc-in [:accounts/create :step] :enter-name)
           console-contact
           (assoc :contacts/contacts {constants/console-chat-id console-contact}))}))

(defn login-only-events [address {:keys [db] :as cofx}]
  (when (not= (:view-id db) :create-account)
    (handlers-macro/merge-fx cofx
                             (navigation/navigate-to-clean :home)
                             (universal-links/process-stored-event)
                             (notifications/process-stored-event address))))

(defn initialize-account [address {:keys [web3] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:web3/set-default-account    [web3 address]
                            :web3/fetch-node-version     web3
                            :notifications/get-fcm-token nil}
                           (initialize-account-db address)
                           (models.protocol/initialize-protocol address)
                           (models.contacts/load-contacts)
                           (chat/initialize-chats)
                           (chat/process-pending-messages)
                           (browser/initialize-browsers)
                           (browser/initialize-dapp-permissions)
                           (models.wallet/update-wallet)
                           (transactions/run-update)
                           (transactions/start-sync)
                           (models.account/update-sign-in-time)
                           (login-only-events address)))
