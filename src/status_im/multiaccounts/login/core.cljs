(ns status-im.multiaccounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.biometric-auth.core :as biometric-auth]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.chat.models :as chat-model]
            [status-im.chat.models.loading :as chat.loading]
            [status-im.constants :as constants]
            [status-im.data-store.core :as data-store]
            [status-im.contact.core :as contact]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.protocol.core :as protocol]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.fleet.core :as fleet]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.notifications.core :as notifications]
            [status-im.stickers.core :as stickers]
            [status-im.ui.screens.db :refer [app-db]]
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
            [status-im.wallet.core :as wallet]
            [taoensso.timbre :as log]))

(def rpc-endpoint "https://goerli.infura.io/v3/f315575765b14720b32382a61a89341a")
(def contract-address "0xfbf4c8e2B41fAfF8c616a0E49Fb4365a5355Ffaf")
(def contract-fleet? #{:eth.contract})

(defn fetch-nodes [current-fleet resolve reject]
  (let [default-nodes (-> (fleet/fleets {})
                          (get-in [:eth.beta :mail])
                          vals)]
    (if config/contract-nodes-enabled?
      (do
        (log/debug "fetching contract fleet" current-fleet)
        (status/get-nodes-from-contract
         rpc-endpoint
         contract-address
         (handlers/response-handler resolve
                                    (fn [error]
                                      (log/warn "could not fetch nodes from contract defaulting to eth.beta")
                                      (resolve default-nodes)))))
      (resolve default-nodes))))

;;;; Handlers
(fx/defn initialize-wallet [cofx]
  (fx/merge cofx
            (wallet/initialize-tokens)
            (wallet/update-balances nil)
            (wallet/update-prices)
            (transactions/initialize)))

(fx/defn login
  {:events [:multiaccounts.login.ui/password-input-submitted]}
  [{:keys [db] :as cofx}]
  (let [{:keys [address password name photo-path]} (:multiaccounts/login db)]
    {:db (assoc-in db [:multiaccounts/login :processing] true)
     ::login [(types/clj->json {:name name :address address :photo-path photo-path})
              password]}))

(fx/defn finish-keycard-setup
  [{:keys [db] :as cofx}]
  (let [flow (get-in db [:hardwallet :flow])]
    (when flow
      (fx/merge cofx
                {:db (update db :hardwallet dissoc :flow)}
                (if (= :import flow)
                  (navigation/navigate-to-cofx :keycard-recovery-success nil)
                  (navigation/navigate-to-cofx :home nil))))))

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

(fx/defn get-config-callback
  {:events [::get-config-callback]}
  [{:keys [db] :as cofx} config stored-pns]
  (let [{:keys [network networks/networks address] :as multiaccount} (types/deserialize config)]
    (fx/merge cofx
              {:db (assoc db
                          :network network
                          :networks/networks networks
                          :chain (ethereum/network->chain-name network)
                          :multiaccount multiaccount)
               :notifications/request-notifications-permissions nil}
              (stickers/init-stickers-packs)
              (mobile-network/on-network-status-change)
              (chaos-mode/check-chaos-mode)
              (protocol/initialize-protocol)
              (when-not platform/desktop?
                (initialize-wallet))
              (when stored-pns
                (notifications/process-stored-event address stored-pns)))))

(fx/defn initialize-multiaccount-db
  [{:keys [db]} address]
  (let [{:universal-links/keys [url]
         :keys [network-status peers-count peers-summary view-id multiaccount
                desktop/desktop hardwallet custom-fleets supported-biometric-auth
                device-UUID semaphores intro-wizard]} db]
    {:db (cond-> (assoc app-db
                        :view-id view-id
                        :node/status (:node/status db)
                        :desktop/desktop (merge desktop (:desktop/desktop app-db))
                        :network-status network-status
                        :network/type (:network/type db)
                        :universal-links/url url
                        :custom-fleets custom-fleets
                        :peers-summary peers-summary
                        :peers-count peers-count
                        :device-UUID device-UUID
                        :intro-wizard {:step (:step intro-wizard)}
                        :supported-biometric-auth supported-biometric-auth
                        :semaphores semaphores
                        :hardwallet hardwallet)
           multiaccount (assoc :multiaccount multiaccount))}))

(fx/defn initialize-web3-client-version
  {:events [::initialize-web3-client-version]}
  [{:keys [db]} resp]
  (if-let [node-version (second (re-find #"StatusIM/v(.*)/.*/.*" resp))]
    {:db (assoc db :web3-node-version node-version)}
    (log/warn (str "unexpected web3 version format: " "'" resp "'"))))

(fx/defn save-user-password
  [cofx address password]
  {:keychain/save-user-password [address password]})

(fx/defn login-only-events
  [{:keys [db] :as cofx} address password save-password?]
  (let [stored-pns (:push-notifications/stored db)]
    (fx/merge cofx
              {:db (assoc db :chats/loading? true)
               ::json-rpc/call
               [{:method "browsers_getBrowsers"
                 :on-success #(re-frame/dispatch [::initialize-browsers %])}
                {:method "permissions_getDappPermissions"
                 :on-success #(re-frame/dispatch [::initialize-dapp-permissions %])}
                {:method "settings_getConfig"
                 :params ["multiaccount"]
                 :on-success #(re-frame/dispatch [::get-config-callback % stored-pns])}]}
              (when save-password?
                (save-user-password address password))
              (navigation/navigate-to-cofx :home nil)
              (stickers/init-stickers-packs)
              (chat.loading/initialize-chats)
              (contact/initialize-contacts)
              (universal-links/process-stored-event)
              (when platform/desktop?
                (chat-model/update-dock-badge-label)))))

(defn- recovering-multiaccount? [cofx]
  (boolean (get-in cofx [:db :multiaccounts/recover])))

(fx/defn create-only-events
  [{:keys [db] :as cofx}]
  (let [{:keys [multiaccount]} db]
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
                 :on-success #()}]}
              (finish-keycard-setup)
              (mobile-network/on-network-status-change)
              (chaos-mode/check-chaos-mode)
              (protocol/initialize-protocol)
              (when-not platform/desktop?
                (initialize-wallet)))))

(defn- keycard-setup? [cofx]
  (boolean (get-in cofx [:db :hardwallet :flow])))

(fx/defn multiaccount-login-success
  [{:keys [db] :as cofx}]
  (let [{:keys [address password save-password? name photo-path creating?]} (:multiaccounts/login db)
        step (get-in db [:intro-wizard :step])
        recovering? (recovering-multiaccount? cofx)
        login-only? (not (or creating?
                             recovering?
                             (keycard-setup? cofx)))
        nodes nil]
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
                 :on-success #(re-frame/dispatch [::initialize-web3-client-version %])}]
               :notifications/get-fcm-token nil}
              ;;FIXME
              (when nodes
                (fleet/set-nodes :eth.contract nodes))
              (initialize-multiaccount-db address)
              (if login-only?
                (login-only-events address password save-password?)
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

(fx/defn get-user-password
  [_ address]
  {:keychain/can-save-user-password? nil
   :keychain/get-user-password       [address
                                      #(re-frame/dispatch [:multiaccounts.login.callback/get-user-password-success % address])]})

(fx/defn open-login
  [{:keys [db] :as cofx} address photo-path name public-key]
  (let [keycard-multiaccount? (get-in db [:multiaccounts/multiaccounts address :keycard-instance-uid])]
    (fx/merge cofx
              {:db (-> db
                       (update :multiaccounts/login assoc
                               :public-key public-key
                               :address address
                               :photo-path photo-path
                               :name name)
                       (update :multiaccounts/login dissoc
                               :error
                               :password))}
              (if keycard-multiaccount?
                (open-keycard-login)
                (get-user-password address)))))

(fx/defn open-login-callback
  {:events [::biometric-auth-done]}
  [{:keys [db] :as cofx} password {:keys [bioauth-success bioauth-notrequired bioauth-message]}]
  (if (and password
           (or bioauth-success bioauth-notrequired))
    (fx/merge cofx
              {:db (assoc-in db [:multiaccounts/login :password] password)}
              (navigation/navigate-to-cofx :progress nil)
              login)
    (fx/merge cofx
              (when bioauth-message
                {:utils/show-popup {:title (i18n/label :t/biometric-auth-login-error-title) :content bioauth-message}})
              (navigation/navigate-to-cofx :login nil))))

(fx/defn do-biometric-auth
  [{:keys [db] :as cofx} password]
  (biometric-auth/authenticate-fx cofx
                                  #(re-frame/dispatch [::biometric-auth-done password %])
                                  {:reason (i18n/label :t/biometric-auth-reason-login)
                                   :ios-fallback-label (i18n/label :t/biometric-auth-login-ios-fallback-label)}))

(re-frame/reg-fx
 ::login
 (fn [[account-data password]]
   (status/login account-data
                 (security/safe-unmask-data password))))
