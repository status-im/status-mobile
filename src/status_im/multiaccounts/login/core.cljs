(ns status-im.multiaccounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.subscriptions :as ethereum.subscriptions]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.fleet.core :as fleet]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.protocol.core :as protocol]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.biometric-auth.core :as biometric-auth]
            [status-im.utils.types :as types]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.wallet.core :as wallet]
            [taoensso.timbre :as log]
            [status-im.ethereum.core :as ethereum]))

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

(defn login! [address main-account password]
  (status/login address password main-account [] #(re-frame/dispatch [:multiaccounts.login.callback/login-success %])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn change-multiaccount! [address
                       password
                       current-fleet]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (js/Promise. (fn [resolve reject]
                     (if (contract-fleet? current-fleet)
                       (fetch-nodes current-fleet resolve reject)
                       (resolve))))
      (then (fn [nodes]
              (re-frame/dispatch [:init.callback/multiaccount-change-success address nodes])))
      (catch (fn [error]
               (log/warn "Could not change multiaccount" error)
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [:init.callback/multiaccount-change-error error])))))

;;;; Handlers
(fx/defn login [cofx]
  (if (get-in cofx [:db :hardwallet :multiaccount :whisper-private-key])
    {:hardwallet/login-with-keycard (-> cofx
                                        (get-in [:db :hardwallet :multiaccount])
                                        (select-keys [:whisper-private-key :encryption-public-key])
                                        (assoc :on-result #(re-frame/dispatch [:multiaccounts.login.callback/login-success %])))}
    (let [{:keys [address password main-account]} (get-in cofx [:db :multiaccounts/login])]
      {:multiaccounts.login/login [address main-account password]})))

(fx/defn initialize-wallet [cofx]
  (fx/merge cofx
            (wallet/initialize-tokens)
            (wallet/update-balances)
            (wallet/update-prices)
            (transactions/initialize)))

(fx/defn user-login-without-creating-db
  {:events [:multiaccounts.login.ui/password-input-submitted]}
  [{:keys [db] :as cofx}]
  (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
    (fx/merge
     cofx
     {:db                            (-> db
                                         (assoc-in [:multiaccounts/login :processing] true)
                                         (assoc :node/on-ready :login))
      :multiaccounts.login/clear-web-data nil
      :data-store/change-multiaccount     [address
                                      password
                                      (get-in db [:multiaccounts/multiaccounts address :settings :fleet])]})))

(fx/defn user-login
  [{:keys [db] :as cofx}]
  (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
    (fx/merge cofx
              {:db
               (-> db
                   (assoc-in [:multiaccounts/login :processing] true)
                   (assoc :node/on-ready :login))
               :multiaccounts.login/clear-web-data nil
               :data-store/change-multiaccount
               [address password
                (get-in db [:multiaccounts/multiaccounts address :settings :fleet])]})))

(fx/defn multiaccount-and-db-password-do-not-match
  [{:keys [db] :as cofx} error]
  (log/error "failed to login" error)
  (fx/merge
   cofx
   {:db
    (update db :multiaccounts/login assoc
            :save-password? false
            :error error
            :processing false)

    :utils/show-popup
    {:title   (i18n/label :multiaccount-and-db-password-mismatch-title)
     :content (i18n/label :multiaccount-and-db-password-mismatch-content)}

    :dispatch [:multiaccounts.logout.ui/logout-confirmed]}))

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

(fx/defn user-login-callback
  {:events [:multiaccounts.login.callback/login-success]
   :interceptors [(re-frame/inject-cofx :web3/get-web3)
                  (re-frame/inject-cofx :data-store/get-all-mailservers)
                  (re-frame/inject-cofx :data-store/get-all-installations)
                  (re-frame/inject-cofx :data-store/transport)
                  (re-frame/inject-cofx :data-store/mailserver-topics)]}
  [{:keys [db web3] :as cofx} login-result]
  (let [data    (types/json->clj login-result)
        error   (:error data)
        success (empty? error)
        {:keys [address password save-password?]}
        (multiaccounts.model/credentials cofx)
        network-type (:network/type db)]
    ;; check if logged into multiaccount
    (when address
      (if success
        (fx/merge
         cofx
         {:db                       (-> db
                                        (dissoc :multiaccounts/login)
                                        (update :hardwallet dissoc
                                                :on-card-read
                                                :card-read-in-progress?
                                                :pin
                                                :multiaccount))
          ::json-rpc/call
          [{:method "browsers_getBrowsers"
            :on-success #(re-frame/dispatch [::initialize-browsers %])}
           {:method "permissions_getDappPermissions"
            :on-success #(re-frame/dispatch [::initialize-dapp-permissions %])}]
          :web3/set-default-account [web3 address]
          :web3/fetch-node-version  [web3
                                     #(re-frame/dispatch
                                       [:web3/fetch-node-version-callback %])]}
         (fn [_]
           (when save-password?
             {:keychain/save-user-password [address password]}))
         (tribute-to-talk/init)
         (mobile-network/on-network-status-change)
         (protocol/initialize-protocol)
         (universal-links/process-stored-event)
         (chaos-mode/check-chaos-mode)
         (finish-keycard-setup)
         (when-not platform/desktop?
           (initialize-wallet)))
        (multiaccount-and-db-password-do-not-match cofx error)))))

(fx/defn handle-change-multiaccount-error
  [{:keys [db] :as cofx} error]
  ;; does nothing for now
  ;; will handle errors from go side
)

(fx/defn open-keycard-login
  [{:keys [db] :as cofx}]
  (let [navigation-stack (:navigation-stack db)]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :pin :enter-step] :login)}
              (if (empty? navigation-stack)
                (navigation/navigate-to-cofx :multiaccounts nil)
                (navigation/navigate-to-cofx :enter-pin-login nil)))))

(fx/defn get-user-password
  [_ address]
  {:keychain/can-save-user-password? nil
   :keychain/get-user-password       [address
                                      #(re-frame/dispatch [:multiaccounts.login.callback/get-user-password-success % address])]})

(fx/defn open-login [{:keys [db] :as cofx} address photo-path name public-key accounts]
  (let [keycard-multiaccount? (get-in db [:multiaccounts/multiaccounts address :keycard-instance-uid])]
    (fx/merge cofx
              {:db (-> db
                       (update :multiaccounts/login assoc
                               :public-key public-key
                               :address address
                               :main-account (:address (ethereum/get-default-account accounts))
                               :photo-path photo-path
                               :name name)
                       (update :multiaccounts/login dissoc
                               :error
                               :password))}
              (if keycard-multiaccount?
                (open-keycard-login)
                (get-user-password address)))))

(fx/defn open-login-callback
  {:events [:multiaccounts.login.callback/biometric-auth-done]}
  [{:keys [db] :as cofx} password {:keys [bioauth-success bioauth-notrequired bioauth-message]}]
  (if (and password
           (or bioauth-success bioauth-notrequired))
    (fx/merge cofx
              {:db (assoc-in db [:multiaccounts/login :password] password)}
              (navigation/navigate-to-cofx :progress nil)
              (user-login))
    (fx/merge cofx
              (when bioauth-message
                {:utils/show-popup {:title (i18n/label :t/biometric-auth-login-error-title) :content bioauth-message}})
              (navigation/navigate-to-cofx :login nil))))

(fx/defn do-biometric-auth
  [{:keys [db] :as cofx} password]
  (biometric-auth/authenticate-fx cofx
                                  #(re-frame/dispatch [:multiaccounts.login.callback/biometric-auth-done password %])
                                  {:reason (i18n/label :t/biometric-auth-reason-login)
                                   :ios-fallback-label (i18n/label :t/biometric-auth-login-ios-fallback-label)}))

(re-frame/reg-fx
 :multiaccounts.login/login
 (fn [[address main-account password]]
   (login! address main-account (security/safe-unmask-data password))))

(re-frame/reg-fx
 :multiaccounts.login/clear-web-data
 clear-web-data!)

(re-frame/reg-fx
 :data-store/change-multiaccount
 (fn [[address password current-fleet]]
   (change-multiaccount! address
                    (security/safe-unmask-data password)
                    current-fleet)))
