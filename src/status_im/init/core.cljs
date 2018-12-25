(ns status-im.init.core
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.loading :as chat-loading]
            [status-im.accounts.core :as accounts.core]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.realm.core :as realm]
            [status-im.extensions.registry :as extensions.registry]
            [status-im.i18n :as i18n]
            [status-im.browser.core :as browser]
            [status-im.contact.core :as contact]
            [status-im.models.dev-server :as models.dev-server]
            [status-im.protocol.core :as protocol]
            [status-im.pairing.core :as pairing]
            [status-im.models.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.notifications.core :as notifications]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.chat.models :as chat-model]
            [status-im.accounts.db :as accounts.db]))

(defn init-store!
  "Try to decrypt the database, move on if successful otherwise go back to
  initial state"
  [encryption-key]
  (.. (data-store/init encryption-key)
      (then #(re-frame/dispatch [:init.callback/init-store-success]))
      (catch (fn [error]
               (log/warn "Could not decrypt database" error)
               (re-frame/dispatch [:init.callback/init-store-error encryption-key])))))

(defn restore-native-settings! []
  (when platform/desktop?
    (.getValue rn-dependencies/desktop-config "logging_enabled"
               #(re-frame/dispatch [:set-in [:desktop/desktop :logging-enabled]
                                    (if (boolean? %1)
                                      %1 (cljs.reader/read-string %1))]))))

;; TODO (yenda) move keychain functions to dedicated namespace
(defn reset-keychain! []
  (.. (keychain/reset)
      (then
       #(re-frame/dispatch [:init.callback/keychain-reset]))))

(defn reset-data! []
  (.. (realm/delete-realms)
      (then reset-keychain!)
      (catch reset-keychain!)))

(defn reset-account-data! [address]
  (let [callback #(re-frame/dispatch [:init.callback/account-db-removed])]
    (.. (realm/delete-account-realm address)
        (then callback)
        (catch callback))))

(fx/defn initialize-keychain
  "Entrypoint, fetches the key from the keychain and initialize the app"
  [cofx]
  {:keychain/get-encryption-key [:init.callback/get-encryption-key-success]})

(fx/defn start-app [cofx]
  (fx/merge cofx
            {:init/get-device-UUID                  nil
             :init/restore-native-settings          nil
             :ui/listen-to-window-dimensions-change nil
             :notifications/init                    nil
             :network/listen-to-network-status      nil
             :network/listen-to-connection-status   nil
             :hardwallet/register-card-events       nil}
            (initialize-keychain)))

(fx/defn initialize-app-db
  "Initialize db to initial state"
  [{{:keys      [status-module-initialized? view-id hardwallet
                 initial-props desktop/desktop
                 network-status network peers-count peers-summary device-UUID
                 push-notifications/stored]
     :node/keys [status]
     :or        {network (get app-db :network)}} :db}]
  ;TODO remove retrieve-pairing when keycard login will be ready
  {:hardwallet/retrieve-pairing nil
   :db                          (assoc app-db
                                       :contacts/contacts {}
                                       :initial-props initial-props
                                       :desktop/desktop (merge desktop (:desktop/desktop app-db))
                                       :network-status network-status
                                       :peers-count (or peers-count 0)
                                       :peers-summary (or peers-summary [])
                                       :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                                       :node/status status
                                       :network network
                                       :hardwallet hardwallet
                                       :device-UUID device-UUID
                                       :view-id view-id
                                       :push-notifications/stored stored)})

(fx/defn initialize-app
  [cofx encryption-key]
  (fx/merge cofx
            {:init/init-store              encryption-key
             :hardwallet/check-nfc-support nil
             :hardwallet/check-nfc-enabled nil}
            (initialize-app-db)))

(fx/defn set-device-uuid
  [{:keys [db]} device-uuid]
  {:db (assoc db :device-UUID device-uuid)})

(fx/defn handle-init-store-error
  [encryption-key cofx]
  {:ui/show-confirmation
   {:title               (i18n/label :decryption-failed-title)
    :content             (i18n/label :decryption-failed-content)
    :confirm-button-text (i18n/label :decryption-failed-confirm)
    ;; On cancel we initialize the app with the same key, in case the error was
    ;; not related/fs error
    :on-cancel           #(re-frame/dispatch [:init.ui/data-reset-cancelled encryption-key])
    :on-accept           #(re-frame/dispatch [:init.ui/data-reset-accepted])}})

(fx/defn load-accounts [{:keys [db all-accounts]}]
  (let [accounts (->> all-accounts
                      (map (fn [{:keys [address] :as account}]
                             [address account]))
                      (into {}))]
    {:db (assoc db :accounts/accounts accounts)}))

(fx/defn initialize-views
  [cofx]
  (let [{{:accounts/keys [accounts] :as db} :db} cofx]
    (if (empty? accounts)
      (navigation/navigate-to-clean cofx :intro nil)
      (let [account-with-notification
            (when-not platform/desktop?
              (notifications/lookup-contact-pubkey-from-hash
               cofx
               (first (keys (:push-notifications/stored db)))))
            selection-fn
            (if (not-empty account-with-notification)
              #(filter (fn [account]
                         (= account-with-notification
                            (:public-key account)))
                       %)
              #(sort-by :last-sign-in > %))
            {:keys [address photo-path name]} (first (selection-fn (vals accounts)))]
        (accounts.login/open-login cofx address photo-path name)))))

(fx/defn load-accounts-and-initialize-views
  "DB has been decrypted, load accounts and initialize-view"
  [cofx]
  (fx/merge cofx
            (load-accounts)
            (initialize-views)))

(fx/defn initialize-account-db [{:keys [db web3]} address]
  (let [{:universal-links/keys [url]
         :keys                 [accounts/accounts accounts/create networks/networks network
                                network-status peers-count peers-summary view-id navigation-stack
                                desktop/desktop hardwallet
                                status-module-initialized? device-UUID semaphores accounts/login]
         :node/keys            [status on-ready]
         :or                   {network (get app-db :network)}} db
        current-account (get accounts address)
        account-network-id (get current-account :network network)
        account-network (get-in current-account [:networks account-network-id])]
    {:db (cond-> (assoc app-db
                        :view-id view-id
                        :navigation-stack navigation-stack
                        :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                        :node/status status
                        :node/on-ready on-ready
                        :accounts/create create
                        :desktop/desktop (merge desktop (:desktop/desktop app-db))
                        :networks/networks networks
                        :account/account current-account
                        :accounts/login login
                        :accounts/accounts accounts
                        :network-status network-status
                        :network network
                        :chain (ethereum/network->chain-name account-network)
                        :universal-links/url url
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :semaphores semaphores
                        :hardwallet hardwallet
                        :web3 web3)
           (= view-id :create-account)
           (assoc-in [:accounts/create :step] :enter-name))}))

(defn login-only-events [cofx address stored-pns]
  (fx/merge cofx
            {:notifications/request-notifications-permissions nil}
            (navigation/navigate-to-cofx :home nil)
            (universal-links/process-stored-event)
            (notifications/process-stored-event address stored-pns)
            (when platform/desktop?
              (chat-model/update-dock-badge-label))))

(defn dev-mode? [cofx]
  (get-in cofx [:db :account/account :dev-mode?]))

(defn creating-account? [cofx]
  (= (get-in cofx [:db :view-id])
     :create-account))

(defn finishing-hardwallet-setup? [cofx]
  (= (get-in cofx [:db :view-id])
     :hardwallet-success))

(fx/defn initialize-account [{:keys [db] :as cofx} address]
  (let [stored-pns (:push-notifications/stored db)]
    (fx/merge cofx
              {:notifications/get-fcm-token nil}
              (initialize-account-db address)
              (contact/load-contacts)
              (pairing/load-installations)
              #(when (dev-mode? %)
                 (models.dev-server/start))
              (browser/initialize-browsers)

              (browser/initialize-dapp-permissions)
              (extensions.registry/initialize)
              (accounts.update/update-sign-in-time)
              #(when-not (or (creating-account? %)
                             (finishing-hardwallet-setup? %))
                 (login-only-events % address stored-pns)))))

(re-frame/reg-fx
 :init/init-store
 init-store!)

(re-frame/reg-fx
 :init/restore-native-settings
 restore-native-settings!)

(re-frame/reg-fx
 :init/status-module-initialized
 status/module-initialized!)

(re-frame/reg-fx
 :init/get-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:init.callback/get-device-UUID-success %]))))

(re-frame/reg-fx
 :init/reset-data
 reset-data!)

(re-frame/reg-fx
 :init/reset-account-data
 reset-account-data!)

