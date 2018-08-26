(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.models.account :as models.account]
            [status-im.models.browser :as browser]
            [status-im.models.chat :as chat]
            [status-im.models.contacts :as models.contacts]
            [status-im.utils.keychain.core :as keychain]
            [status-im.models.protocol :as models.protocol]
            [status-im.models.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]
            [status-im.transport.inbox :as inbox]
            [status-im.data-store.realm.core :as realm]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.ui.screens.contacts.events :as contacts]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.utils.universal-links.core :as universal-links]))

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

(defn initialize-views [{{:accounts/keys [accounts]
                          :push-notifications/keys [initial?]
                          :as db} :db}]
  {:db (if (empty? accounts)
         (assoc db :view-id :intro :navigation-stack (list :intro))
         (let [{:keys [address photo-path name]} (first (sort-by :last-sign-in > (vals accounts)))]
           (-> db
               (assoc :view-id :login
                      :navigation-stack (list :login))
               (update :accounts/login assoc
                       :address address
                       :photo-path photo-path
                       :name name))))
   :notifications/handle-initial-push-notification initial?})

(defn initialize-geth [{:keys [db]}]
  (when-not (:status-node-started? db)
    (let [default-networks (:networks/networks db)
          default-network  (:network db)]
      {:init/initialize-geth (get-in default-networks [default-network :config])})))

(defn initialize-db
  "Initialize db to the initial state"
  [{{:universal-links/keys [url]
     :push-notifications/keys [initial?]
     :keys [status-module-initialized? status-node-started?
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
              :universal-links/url url
              :push-notifications/initial? initial?
              :device-UUID device-UUID)})

(defn initialize-app [encryption-key error cofx]
  (cond
    (= :invalid-key error)
    {:show-confirmation (handle-invalid-key-parameters encryption-key)}

    (= :decryption-failed error)
    {:show-confirmation (handle-decryption-failed-parameters encryption-key)}

    :else
    (handlers-macro/merge-fx cofx
                             {:init/init-device-UUID                   nil
                              :init/init-store                         encryption-key
                              :ui/listen-to-window-dimensions-change   nil
                              :init/testfairy-alert                    nil}
                             (initialize-db))))

(defn after-decryption [cofx]
  (handlers-macro/merge-fx cofx
                           {:network/listen-to-network-status
                            [#(re-frame/dispatch [:network/update-connection-status %])
                             #(re-frame/dispatch [:network/update-network-status %])]}
                           (initialize-geth)
                           (accounts.models/load-accounts)
                           (initialize-views)))

(defn initialize-account-db [address {:keys [db web3]}]
  (let [{:keys [accounts/accounts accounts/create contacts/contacts networks/networks
                network network-status peers-count peers-summary view-id navigation-stack
                status-module-initialized? status-node-started? device-UUID
                push-notifications/initial? semaphores]
         :or   {network (get app-db :network)}} db
        console-contact (get contacts constants/console-chat-id)
        current-account (accounts address)
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
                        :push-notifications/initial? initial?
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :semaphores semaphores
                        :web3 web3)
           console-contact
           (assoc :contacts/contacts {constants/console-chat-id console-contact}))}))

(defn initialize-account [address events-after {:keys [web3] :as cofx}]
  (handlers-macro/merge-fx cofx
                           {:web3/set-default-account    [web3 address]
                            :web3/fetch-node-version     web3
                            :notifications/get-fcm-token nil
                            :dispatch-n                  (or events-after [])}
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
                           (models.account/update-sign-in-time)))

(defn status-node-started [{{:node/keys [after-start] :as db} :db}]
  ;;TODO (yenda) instead of passing events we can pass effects here and simply return them
  (merge {:db (assoc db :status-node-started? true)}
         (when after-start {:dispatch-n [after-start]})))

(defn status-node-stopped [{{:node/keys [after-stop]} :db}]
  ;;TODO (yenda) instead of passing events we can pass effects here and simply return them
  (when after-stop {:dispatch-n [after-stop]}))

(defn status-module-initialized [{:keys [db]}]
  {:db                                (assoc db :status-module-initialized? true)
   :init/status-module-initialized-fx nil})
