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
            [status-im.multiaccounts.core :as multiaccounts]))

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

(fx/defn initialize-wallet
  {:events [::initialize-wallet]}
  [cofx custom-tokens]
  (fx/merge cofx
            (wallet/initialize-tokens custom-tokens)
            (wallet/update-balances nil)
            (wallet/update-prices)
            (transactions/initialize)))

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
                  (navigation/navigate-to-cofx :keycard-recovery-success nil)
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

(defn deserialize-config
  [{:keys [multiaccount current-network networks]}]
  [(types/deserialize multiaccount)
   current-network
   (types/deserialize networks)])

(defn convert-multiaccount-addresses
  [multiaccount]
  (let [update-address #(update % :address eip55/address->checksum)]
    (-> multiaccount
        update-address
        (update :accounts (partial mapv update-address)))))

(fx/defn get-config-callback
  {:events [::get-config-callback]}
  [{:keys [db] :as cofx} config]
  (let [[{:keys [address notifications-enabled?] :as multiaccount}
         current-network networks] (deserialize-config config)
        network-id (str (get-in networks [current-network :config :NetworkId]))]
    (fx/merge cofx
              (cond-> {:db (assoc db
                                  :networks/current-network current-network
                                  :networks/networks networks
                                  :multiaccount (convert-multiaccount-addresses
                                                 multiaccount))}
                (and platform/android?
                     notifications-enabled?)
                (assoc ::notifications/enable nil)
                (not platform/desktop?)
                (assoc ::json-rpc/call
                       [{:method     "wallet_getCustomTokens"
                         :on-success #(re-frame/dispatch [::initialize-wallet %])}]))
              ;; NOTE: initializing mailserver depends on user mailserver
              ;; preference which is why we wait for config callback
              (protocol/initialize-protocol {:default-mailserver true})
              (universal-links/process-stored-event)
              (check-network-version network-id)
              (chat.loading/initialize-chats)
              (contact/initialize-contacts)
              (stickers/init-stickers-packs)
              (mobile-network/on-network-status-change)
              (chaos-mode/check-chaos-mode)
              (multiaccounts/switch-preview-privacy-mode-flag))))

(defn get-new-auth-method [auth-method save-password?]
  (if save-password?
    (when-not (or (= keychain/auth-method-biometric auth-method)
                  (= keychain/auth-method-password auth-method))
      (if (= auth-method keychain/auth-method-biometric-prepare)
        keychain/auth-method-biometric
        keychain/auth-method-password))
    (when (and auth-method
               (not= auth-method keychain/auth-method-none))
      keychain/auth-method-none)))

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
                {:method     "settings_getConfigs"
                 :params     [["multiaccount" "current-network" "networks"]]
                 :on-success #(re-frame/dispatch [::get-config-callback %])}]}
              (when save-password?
                (keychain/save-user-password key-uid password))
              (keychain/save-auth-method key-uid (or new-auth-method auth-method))
              (navigation/navigate-to-cofx :home nil)
              (when platform/desktop?
                (chat-model/update-dock-badge-label)))))

(fx/defn create-only-events
  [{:keys [db] :as cofx}]
  (let [{:keys [multiaccount :networks/networks :networks/current-network]} db]
    (fx/merge cofx
              {:db (assoc db
                          ;;NOTE when login the filters are initialized twice
                          ;;once for contacts and once for chats
                          ;;when creating an account we do it only once by calling
                          ;;load-filters directly because we don't have chats and contacts
                          ;;later on there is a check that filters have been initialized twice
                          ;;so here we set it at 1 already so that it passes the check once it has
                          ;;been initialized
                          :filters/initialized 1
                          :network constants/default-network
                          :networks/networks constants/default-networks)
               :filters/load-filters []
               ::json-rpc/call
               [{:method "settings_saveConfig"
                 :params ["multiaccount" (types/serialize multiaccount)]
                 :on-success #()}
                {:method "settings_saveConfig"
                 :params ["networks" (types/serialize networks)]
                 :on-success #()}
                {:method "settings_saveConfig"
                 :params ["current-network" current-network]
                 :on-success #()}]}
              (finish-keycard-setup)
              (protocol/initialize-protocol {:mailservers []
                                             :mailserver-ranges {}
                                             :mailserver-topics {}
                                             :default-mailserver true})
              (chaos-mode/check-chaos-mode)
              (multiaccounts/switch-preview-privacy-mode-flag)
              (when-not platform/desktop?
                (initialize-wallet nil)))))

(defn- keycard-setup? [cofx]
  (boolean (get-in cofx [:db :hardwallet :flow])))

(fx/defn multiaccount-login-success
  [{:keys [db] :as cofx}]
  (let [{:keys [key-uid password save-password? creating?]} (:multiaccounts/login db)
        recovering? (get-in db [:intro-wizard :recovering?])
        login-only? (not (or creating?
                             recovering?
                             (keycard-setup? cofx)))
        nodes nil]
    (log/debug "[multiaccount] multiaccount-login-success"
               "login-only?" login-only?
               "recovering?" recovering?)
    (fx/merge cofx
              {:db (-> db
                       (dissoc :multiaccounts/login)
                       (update :hardwallet dissoc
                               :on-card-read
                               :card-read-in-progress?
                               :pin
                               :multiaccount))
               ::json-rpc/call
               [{:method "web3_clientVersion"
                 :on-success #(re-frame/dispatch [::initialize-web3-client-version %])}]}
              ;;FIXME
              (when nodes
                (fleet/set-nodes :eth.contract nodes))
              (if login-only?
                (login-only-events key-uid password save-password?)
                (create-only-events))
              (when recovering?
                (navigation/navigate-to-cofx :home nil)))))

(fx/defn open-keycard-login
  [{:keys [db] :as cofx}]
  (let [navigation-stack (:navigation-stack db)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :pin :enter-step] :login)
                       (assoc-in [:hardwallet :pin :status] nil)
                       (assoc-in [:hardwallet :pin :login] []))}
              (if (empty? navigation-stack)
                (navigation/navigate-to-cofx :multiaccounts nil)
                (navigation/navigate-to-cofx :keycard-login-pin nil)))))

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
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db [:multiaccounts/multiaccounts
                                              key-uid
                                              :keycard-pairing]))]
    (if password
      (fx/merge
       cofx
       {:db (update-in db [:multiaccounts/login] assoc
                       :password password
                       :save-password? true)}
       (navigation/navigate-to-cofx :progress nil)
       login)
      (fx/merge
       cofx
       (when keycard-account?
         {:db (assoc-in db [:hardwallet :pin :enter-step] :login)})
       (navigation/navigate-to-cofx
        (if keycard-account? :keycard-login-pin :login)
        nil)))))

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
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])
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
                 (if keycard-multiaccount?
                   (open-keycard-login %)
                   (open-login-callback % nil))))))

(fx/defn biometric-auth-done
  {:events [:biometric-auth-done]}
  [{:keys [db] :as cofx} {:keys [bioauth-success bioauth-message bioauth-code]}]
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])]
    (log/debug "[biometric] biometric-auth-done"
               "bioauth-success" bioauth-success
               "bioauth-message" bioauth-message
               "bioauth-code" bioauth-code)
    (if bioauth-success
      (get-credentials cofx key-uid)
      (fx/merge cofx
                {:db (assoc-in db [:multiaccounts/login :save-password?] false)}
                (biometric/show-message bioauth-message bioauth-code)
                (keychain/save-auth-method key-uid keychain/auth-method-none)
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
