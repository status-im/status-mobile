(ns status-im.multiaccounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.chaos-mode.core :as chaos-mode]
            [status-im.data-store.core :as data-store]
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
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.biometric-auth.core :as biometric-auth]
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

(defn login! [address password]
  (status/login address password #(re-frame/dispatch [:multiaccounts.login.callback/login-success %])))

(defn verify! [address password realm-error]
  (status/verify address password
                 #(re-frame/dispatch
                   [:multiaccounts.login.callback/verify-success % realm-error])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn change-multiaccount! [address
                            password
                            create-database-if-not-exist?
                            current-fleet]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then #(data-store/change-multiaccount address password % create-database-if-not-exist?))
      (then #(js/Promise. (fn [resolve reject]
                            (if (contract-fleet? current-fleet)
                              (fetch-nodes current-fleet resolve reject)
                              (resolve)))))
      (then (fn [nodes] (re-frame/dispatch [:init.callback/multiaccount-change-success address nodes])))
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
    (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
      {:multiaccounts.login/login [address password]})))

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
                                           false
                                           (get-in db [:multiaccounts/multiaccounts address :settings :fleet])]})))

(fx/defn user-login
  [{:keys [db] :as cofx} create-database?]
  (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
    (fx/merge
     cofx
     {:db                            (-> db
                                         (assoc-in [:multiaccounts/login :processing] true)
                                         (assoc :node/on-ready :login))
      :multiaccounts.login/clear-web-data nil
      :data-store/change-multiaccount     [address
                                           password
                                           create-database?
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

(fx/defn show-migration-error-dialog
  [{:keys [db]} realm-error]
  (let [{:keys [message]} realm-error
        address           (get-in db [:multiaccounts/login :address])
        erase-button (i18n/label :migrations-erase-multiaccounts-data-button)]
    {:ui/show-confirmation
     {:title               (i18n/label :invalid-key-title)
      :content             (i18n/label
                            :invalid-key-content
                            {:message                         message
                             :erase-multiaccounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/multiaccount-data-reset-accepted address])}}))
(fx/defn verify-callback
  [cofx verify-result realm-error]
  (let [data    (types/json->clj verify-result)
        error   (:error data)
        success (empty? error)]
    (fx/merge
     cofx
     {:node/stop nil}
     (fn [{:keys [db] :as cofx}]
       (if success
         (case (:error realm-error)
           :decryption-failed
           (show-migration-error-dialog cofx realm-error)

           :database-does-not-exist
           (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
             {:data-store/change-multiaccount [address
                                               password
                                               true
                                               (get-in cofx
                                                       [:db
                                                        :multiaccounts/multiaccounts
                                                        address
                                                        :settings
                                                        :fleet])]}))
         {:db (update db :multiaccounts/login assoc
                      :error error
                      :processing false)})))))

(fx/defn migrations-failed
  [{:keys [db]} {:keys [realm-error erase-button]}]
  (let [{:keys [message details]} realm-error
        address (get-in db [:multiaccounts/login :address])]
    {:ui/show-confirmation
     {:title               (i18n/label :migrations-failed-title)
      :content             (i18n/label
                            :migrations-failed-content
                            (merge
                             {:message                         message
                              :erase-multiaccounts-data-button-text erase-button}
                             details))
      :confirm-button-text erase-button
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/multiaccount-data-reset-accepted address])}}))

(fx/defn verify-multiaccount
  [{:keys [db] :as cofx} {:keys [realm-error]}]
  (fx/merge cofx
            {:db (-> db
                     (assoc :node/on-ready :verify-multiaccount)
                     (assoc :realm-error realm-error))}
            (node/initialize nil)))

(fx/defn unknown-realm-error
  [cofx {:keys [realm-error erase-button]}]
  (let [{:keys [message]} realm-error
        {:keys [address]} (multiaccounts.model/credentials cofx)]
    {:ui/show-confirmation
     {:title               (i18n/label :unknown-realm-error)
      :content             (i18n/label
                            :unknown-realm-error-content
                            {:message                         message
                             :erase-multiaccounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/multiaccount-data-reset-accepted address])}}))

(fx/defn handle-change-multiaccount-error
  [{:keys [db] :as cofx} error]
  (let [{:keys [error] :as realm-error}
        (if (map? error)
          error
          {:message (str error)})
        erase-button (i18n/label :migrations-erase-multiaccounts-data-button)]
    (fx/merge
     cofx
     {:db (assoc-in db [:multiaccounts/login :save-password?] false)}
     (case error
       :migrations-failed
       (migrations-failed {:realm-error  realm-error
                           :erase-button erase-button})

       (:database-does-not-exist :decryption-failed)
       (verify-multiaccount {:realm-error realm-error})

       (unknown-realm-error {:realm-error  realm-error
                             :erase-button erase-button})))))

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

(fx/defn open-login [{:keys [db] :as cofx} address photo-path name public-key]
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
  {:events [:multiaccounts.login.callback/biometric-auth-done]}
  [{:keys [db] :as cofx} password {:keys [bioauth-success bioauth-notrequired bioauth-message]}]
  (if (and password
           (or bioauth-success bioauth-notrequired))
    (fx/merge cofx
              {:db (assoc-in db [:multiaccounts/login :password] password)}
              (navigation/navigate-to-cofx :progress nil)
              (user-login false))
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
 (fn [[address password]]
   (login! address (security/safe-unmask-data password))))

(re-frame/reg-fx
 :multiaccounts.login/verify
 (fn [[address password realm-error]]
   (verify! address (security/safe-unmask-data password) realm-error)))

(re-frame/reg-fx
 :multiaccounts.login/clear-web-data
 clear-web-data!)

(re-frame/reg-fx
 :data-store/change-multiaccount
 (fn [[address password create-database-if-not-exist? current-fleet]]
   (change-multiaccount! address
                         (security/safe-unmask-data password)
                         create-database-if-not-exist?
                         current-fleet)))
