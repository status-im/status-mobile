(ns status-im.multiaccounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.constants :as constants]
            [status-im.contact.core :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.fleet.core :as fleet]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.notifications.core :as notifications]
            [status-im.protocol.core :as protocol]
            [status-im.stickers.core :as stickers]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.utils :as utils]
            [status-im.wallet.core :as wallet]
            [taoensso.timbre :as log]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.multiaccounts.biometric.core :as biometric]
            [status-im.utils.identicon :as identicon]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.popover.core :as popover]
            [status-im.hardwallet.nfc :as nfc]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.data-store.settings :as data-store.settings]))

(def rpc-endpoint "https://goerli.infura.io/v3/f315575765b14720b32382a61a89341a")
(def contract-address "0xfbf4c8e2B41fAfF8c616a0E49Fb4365a5355Ffaf")
(def contract-fleet? #{:eth.contract})

(defn fetch-nodes [current-fleet resolve reject]
  (let [default-nodes (-> (node/fleets {})
                          (get-in [:eth.staging :mail])
                          vals)]
    (if config/contract-nodes-enabled?
      (do
        (log/debug "fetching contract fleet" current-fleet)
        (status/get-nodes-from-contract
         rpc-endpoint
         contract-address
         (handlers/response-handler resolve
                                    (fn [error]
                                      (log/warn "could not fetch nodes from contract defaulting to eth.staging")
                                      (resolve default-nodes)))))
      (resolve default-nodes))))

(re-frame/reg-fx
 ::login
 (fn [[account-data hashed-password]]
   (status/login account-data hashed-password)))

(defn rpc->accounts [accounts]
  (reduce (fn [acc {:keys [chat type wallet] :as account}]
            (if chat
              acc
              (let [account (cond->
                             (update account :address
                                     eip55/address->checksum)
                              type
                              (update :type keyword))]
                ;; if the account is the default wallet we
                ;; put it first in the list
                (if wallet
                  (into [account] acc)
                  (conj acc account)))))
          []
          accounts))

(fx/defn initialize-wallet
  {:events [::initialize-wallet]}
  [{:keys [db] :as cofx} accounts custom-tokens]
  (fx/merge
   cofx
   {:db (assoc db :multiaccount/accounts
               (rpc->accounts accounts))}
   (wallet/initialize-tokens custom-tokens)
   (wallet/update-balances nil)
   (wallet/update-prices)))

(fx/defn login
  {:events [:multiaccounts.login.ui/password-input-submitted]}
  [{:keys [db]}]
  (let [{:keys [key-uid password name photo-path]} (:multiaccounts/login db)]
    {:db (-> db
             (assoc-in [:multiaccounts/login :processing] true)
             (dissoc :intro-wizard)
             (update :hardwallet dissoc :flow))
     ::login [(types/clj->json {:name       name
                                :key-uid    key-uid
                                :photo-path photo-path})
              (ethereum/sha3 (security/safe-unmask-data password))]}))

(fx/defn finish-keycard-setup
  [{:keys [db] :as cofx}]
  (let [flow (get-in db [:hardwallet :flow])]
    (when flow
      (fx/merge cofx
                {:db (update db :hardwallet dissoc :flow)}
                (if (= :import flow)
                  (navigation/navigate-to-cofx :intro-stack {:screen :keycard-recovery-success})
                  (navigation/navigate-to-cofx :welcome nil))))))

(fx/defn  initialize-dapp-permissions
  {:events [::initialize-dapp-permissions]}
  [{:keys [db]} all-dapp-permissions]
  (let [dapp-permissions (reduce (fn [acc {:keys [dapp] :as dapp-permissions}]
                                   (assoc acc dapp dapp-permissions))
                                 {}
                                 all-dapp-permissions)]
    {:db (assoc db :dapps/permissions dapp-permissions)}))

(fx/defn initialize-browsers
  {:events [::initialize-browsers]}
  [{:keys [db]} all-stored-browsers]
  (let [browsers (reduce (fn [acc {:keys [browser-id] :as browser}]
                           (assoc acc browser-id browser))
                         {}
                         all-stored-browsers)]
    {:db (assoc db :browser/browsers browsers)}))

(fx/defn initialize-web3-client-version
  {:events [::initialize-web3-client-version]}
  [{:keys [db]} node-version]
  {:db (assoc db :web3-node-version node-version)})

(fx/defn handle-close-app-confirmed
  {:events [::close-app-confirmed]}
  [_]
  {:ui/close-application nil})

(fx/defn check-network-version
  [cofx network-id]
  {::json-rpc/call
   [{:method "net_version"
     :on-success
     (fn [fetched-network-id]
       (when (not= network-id fetched-network-id)
         ;;TODO: this shouldn't happen but in case it does
         ;;we probably want a better error message
         (utils/show-popup
          (i18n/label :t/ethereum-node-started-incorrectly-title)
          (i18n/label :t/ethereum-node-started-incorrectly-description
                      {:network-id         network-id
                       :fetched-network-id fetched-network-id})
          #(re-frame/dispatch [::close-app-confirmed]))))}]})

(re-frame/reg-fx
 ;;TODO: this could be replaced by a single API call on status-go side
 ::initialize-wallet
 (fn [callback]
   (-> (js/Promise.all
        (clj->js
         [(js/Promise.
           (fn [resolve reject]
             (json-rpc/call {:method "accounts_getAccounts"
                             :on-success resolve
                             :on-error reject})))
          (js/Promise.
           (fn [resolve reject]
             (json-rpc/call {:method "wallet_getCustomTokens"
                             :on-success resolve
                             :on-error reject})))]))
       (.then (fn [[accounts custom-tokens]]
                (callback accounts
                          (mapv #(update % :symbol keyword) custom-tokens))))
       (.catch (fn [error]
                 (log/error "Failed to initialize wallet"))))))

(fx/defn initialize-appearance [cofx]
  {::multiaccounts/switch-theme (get-in cofx [:db :multiaccount :appearance])})

(fx/defn get-settings-callback
  {:events [::get-settings-callback]}
  [{:keys [db] :as cofx} settings]
  (let [{:keys [address notifications-enabled?
                networks/current-network networks/networks] :as settings}
        (data-store.settings/rpc->settings settings)
        multiaccount (dissoc settings :networks/current-network :networks/networks)
        network-id (str (get-in networks [current-network :config :NetworkId]))]
    (fx/merge cofx
              (cond-> {:db (-> db
                               (dissoc :multiaccounts/login)
                               (assoc :networks/current-network current-network
                                      :networks/networks networks
                                      :multiaccount multiaccount))}
                (and platform/android?
                     notifications-enabled?)
                (assoc ::notifications/enable nil)
                (not platform/desktop?)
                (assoc ::initialize-wallet
                       (fn [accounts custom-tokens]
                         (re-frame/dispatch [::initialize-wallet
                                             accounts custom-tokens]))))
              (initialize-appearance)
              ;; NOTE: initializing mailserver depends on user mailserver
              ;; preference which is why we wait for config callback
              (protocol/initialize-protocol {:default-mailserver true})
              (universal-links/process-stored-event)
              (check-network-version network-id)
              (chat.loading/initialize-chats)
              (contact/initialize-contacts)
              (stickers/init-stickers-packs)
              (mobile-network/on-network-status-change)
              (multiaccounts/switch-preview-privacy-mode-flag))))

(defn get-new-auth-method [auth-method save-password?]
  (when save-password?
    (when-not (or (= keychain/auth-method-biometric auth-method)
                  (= keychain/auth-method-password auth-method))
      (if (= auth-method keychain/auth-method-biometric-prepare)
        keychain/auth-method-biometric
        keychain/auth-method-password))))

(fx/defn login-only-events
  [{:keys [db] :as cofx} key-uid password save-password?]
  (let [auth-method     (:auth-method db)
        new-auth-method (get-new-auth-method auth-method save-password?)]
    (log/debug "[login] login-only-events"
               "auth-method" auth-method
               "new-auth-method" new-auth-method)
    (fx/merge cofx
              {:db (assoc db :chats/loading? true)
               ::json-rpc/call
               [{:method     "mailservers_getMailserverTopics"
                 :on-success #(re-frame/dispatch [::protocol/initialize-protocol {:mailserver-topics (or % {})}])}
                {:method     "mailservers_getChatRequestRanges"
                 :on-success #(re-frame/dispatch [::protocol/initialize-protocol {:mailserver-ranges (or % {})}])}
                {:method     "browsers_getBrowsers"
                 :on-success #(re-frame/dispatch [::initialize-browsers %])}
                {:method     "permissions_getDappPermissions"
                 :on-success #(re-frame/dispatch [::initialize-dapp-permissions %])}
                {:method     "mailservers_getMailservers"
                 :on-success #(re-frame/dispatch [::protocol/initialize-protocol {:mailservers (or % [])}])}
                {:method     "settings_getSettings"
                 :on-success #(re-frame/dispatch [::get-settings-callback %])}]}
              (when save-password?
                (keychain/save-user-password key-uid password))
              (keychain/save-auth-method key-uid (or new-auth-method auth-method))
              (when platform/desktop?
                (chat-model/update-dock-badge-label)))))

(fx/defn create-only-events
  [{:keys [db] :as cofx}]
  (let [{:keys [multiaccount :multiaccount/accounts]} db]
    (fx/merge cofx
              {:db (-> db
                       (dissoc :multiaccounts/login)
                       (assoc
                         ;;NOTE when login the filters are initialized twice
                         ;;once for contacts and once for chats
                         ;;when creating an account we do it only once by calling
                         ;;load-filters directly because we don't have chats and contacts
                         ;;later on there is a check that filters have been initialized twice
                         ;;so here we set it at 1 already so that it passes the check once it has
                         ;;been initialized
                        :filters/initialized 1))
               :filters/load-filters [[(:waku-enabled multiaccount) []]]}
              (finish-keycard-setup)
              (protocol/initialize-protocol {:mailservers []
                                             :mailserver-ranges {}
                                             :mailserver-topics {}
                                             :default-mailserver true})
              (multiaccounts/switch-preview-privacy-mode-flag)
              (when-not platform/desktop?
                (initialize-wallet accounts nil)))))

(defn- keycard-setup? [cofx]
  (boolean (get-in cofx [:db :hardwallet :flow])))

(fx/defn multiaccount-login-success
  [{:keys [db now] :as cofx}]
  (let [{:keys [key-uid password save-password? creating?]} (:multiaccounts/login db)
        recovering?                                         (get-in db [:intro-wizard :recovering?])
        login-only?                                         (not (or creating?
                                                                     recovering?
                                                                     (keycard-setup? cofx)))
        nodes                                               nil]
    (log/debug "[multiaccount] multiaccount-login-success"
               "login-only?" login-only?
               "recovering?" recovering?)
    (fx/merge cofx
              {:db (-> db
                       (dissoc :connectivity/ui-status-properties)
                       (update :hardwallet dissoc
                               :on-card-read
                               :card-read-in-progress?
                               :pin
                               :multiaccount)
                       (assoc :logged-in-since now))
               ::json-rpc/call
               [{:method     "web3_clientVersion"
                 :on-success #(re-frame/dispatch [::initialize-web3-client-version %])}]}
              ;;FIXME
              (when nodes
                (fleet/set-nodes :eth.contract nodes))
              (if login-only?
                (login-only-events key-uid password save-password?)
                (create-only-events))
              (when recovering?
                (navigation/navigate-to-cofx :tabs {:screen :chat-stack
                                                    :params {:screen :home}})))))

(fx/defn open-login
  [{:keys [db] :as cofx} key-uid photo-path name public-key]
  (fx/merge cofx
            {:db (-> db
                     (update :multiaccounts/login assoc
                             :public-key public-key
                             :key-uid key-uid
                             :photo-path photo-path
                             :name name)
                     (assoc :profile/photo-added? (= (identicon/identicon public-key) photo-path))
                     (update :multiaccounts/login dissoc
                             :error
                             :password))}
            (keychain/get-auth-method key-uid)))

(fx/defn open-login-callback
  {:events [:multiaccounts.login.callback/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (let [key-uid          (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db [:multiaccounts/multiaccounts
                                              key-uid
                                              :keycard-pairing]))]
    (if password
      (fx/merge
       cofx
       {:db (update-in db [:multiaccounts/login] assoc
                       :password password
                       :save-password? true)}
       (navigation/navigate-to-cofx :intro-stack {:screen :progress})
       login)
      (fx/merge
       cofx
       (when keycard-account?
         {:db (-> db
                  (assoc-in [:hardwallet :pin :enter-step] :login)
                  (assoc-in [:hardwallet :pin :status] nil)
                  (assoc-in [:hardwallet :pin :login] []))})
       (if keycard-account?
         (navigation/navigate-to-cofx :intro-stack {:screen :keycard-login-pin})
         (navigation/navigate-to-cofx :intro-stack {:screen :login}))))))

(fx/defn get-credentials
  [{:keys [db] :as cofx} key-uid]
  (let [keycard-multiaccount? (boolean (get-in db [:multiaccounts/multiaccounts key-uid :keycard-pairing]))]
    (log/debug "[login] get-credentials"
               "keycard-multiacc?" keycard-multiaccount?)
    (if keycard-multiaccount?
      (keychain/get-hardwallet-keys cofx key-uid)
      (keychain/get-user-password cofx key-uid))))

(fx/defn get-auth-method-success
  "Auth method: nil - not supported, \"none\" - not selected, \"password\", \"biometric\", \"biometric-prepare\""
  {:events [:multiaccounts.login/get-auth-method-success]}
  [{:keys [db] :as cofx} auth-method]
  (let [key-uid               (get-in db [:multiaccounts/login :key-uid])
        keycard-multiaccount? (boolean (get-in db [:multiaccounts/multiaccounts key-uid :keycard-pairing]))]
    (log/debug "[login] get-auth-method-success"
               "auth-method" auth-method
               "keycard-multiacc?" keycard-multiaccount?)
    (fx/merge cofx
              {:db (assoc db :auth-method auth-method)}
              #(case auth-method
                 keychain/auth-method-biometric
                 (biometric/biometric-auth %)
                 keychain/auth-method-password
                 (get-credentials % key-uid)

                 ;;nil or "none" or "biometric-prepare"
                 (open-login-callback % nil)))))

(fx/defn biometric-auth-done
  {:events [:biometric-auth-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (let [key-uid     (get-in db [:multiaccounts/login :key-uid])
        auth-method (get db :auth-method)]
    (log/debug "[biometric] biometric-auth-done"
               "bioauth-success" bioauth-success
               "bioauth-message" bioauth-message
               "bioauth-code" bioauth-code)
    (if bioauth-success
      (get-credentials cofx key-uid)
      (fx/merge cofx
                {:db (assoc-in db
                               [:multiaccounts/login :save-password?]
                               (= auth-method keychain/auth-method-biometric))}
                (when-not (= auth-method keychain/auth-method-biometric)
                  (keychain/save-auth-method key-uid keychain/auth-method-none))
                (biometric/show-message bioauth-message bioauth-code)
                (open-login-callback nil)))))

(fx/defn save-password
  {:events [:multiaccounts/save-password]}
  [{:keys [db] :as cofx} save-password?]
  (let [bioauth-supported?   (boolean (get db :supported-biometric-auth))
        previous-auth-method (get db :auth-method)]
    (log/debug "[login] save-password"
               "save-password?" save-password?
               "bioauth-supported?" bioauth-supported?
               "previous-auth-method" previous-auth-method)
    (fx/merge
     cofx
     {:db (cond-> db
            (not= previous-auth-method
                  keychain/auth-method-biometric-prepare)
            (assoc :auth-method keychain/auth-method-none)
            (or save-password?
                (not bioauth-supported?)
                (and (not save-password?)
                     bioauth-supported?
                     (= previous-auth-method keychain/auth-method-none)))
            (assoc-in [:multiaccounts/login :save-password?] save-password?))}
     (when bioauth-supported?
       (if save-password?
         (popover/show-popover {:view :secure-with-biometric})
         (when-not (= previous-auth-method keychain/auth-method-none)
           (popover/show-popover {:view :disable-password-saving})))))))
