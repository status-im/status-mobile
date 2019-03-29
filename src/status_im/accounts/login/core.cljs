(ns status-im.accounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.fleet.core :as fleet]
            [status-im.utils.fx :as fx]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.security :as security]
            [status-im.utils.platform :as platform]
            [status-im.protocol.core :as protocol]
            [status-im.models.wallet :as models.wallet]
            [status-im.utils.handlers :as handlers]
            [status-im.models.transactions :as transactions]
            [status-im.i18n :as i18n]
            [status-im.node.core :as node]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.chaos-mode.core :as chaos-mode]))

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
  (status/login address password #(re-frame/dispatch [:accounts.login.callback/login-success %])))

(defn verify! [address password realm-error]
  (status/verify address password
                 #(re-frame/dispatch
                   [:accounts.login.callback/verify-success % realm-error])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn change-account! [address
                       password
                       create-database-if-not-exist?
                       current-fleet]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then #(data-store/change-account address password % create-database-if-not-exist?))
      (then #(js/Promise. (fn [resolve reject]
                            (if (contract-fleet? current-fleet)
                              (fetch-nodes current-fleet resolve reject)
                              (resolve)))))
      (then (fn [nodes] (re-frame/dispatch [:init.callback/account-change-success address nodes])))
      (catch (fn [error]
               (log/warn "Could not change account" error)
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [:init.callback/account-change-error error])))))

;;;; Handlers
(fx/defn login [cofx]
  (if (get-in cofx [:db :hardwallet :whisper-private-key])
    {:hardwallet/login-with-keycard (-> cofx
                                        (get-in [:db :hardwallet])
                                        (select-keys [:whisper-private-key :encryption-public-key])
                                        (assoc :on-result #(re-frame/dispatch [:accounts.login.callback/login-success %])))}
    (let [{:keys [address password]} (accounts.db/credentials cofx)]
      {:accounts.login/login [address password]})))

(fx/defn initialize-wallet [cofx]
  (fx/merge cofx
            (models.wallet/initialize-tokens)
            (models.wallet/update-wallet)
            (transactions/start-sync)))

(fx/defn user-login [{:keys [db] :as cofx} create-database?]
  (let [{:keys [address password]} (accounts.db/credentials cofx)]
    (fx/merge
     cofx
     {:db                            (-> db
                                         (assoc-in [:accounts/login :processing] true)
                                         (assoc :node/on-ready :login))
      :accounts.login/clear-web-data nil
      :data-store/change-account     [address
                                      password
                                      create-database?
                                      (get-in db [:accounts/accounts address :settings :fleet])]})))

(fx/defn account-and-db-password-do-not-match
  [{:keys [db] :as cofx} error]
  (fx/merge
   cofx
   {:db
    (update db :accounts/login assoc
            :save-password? false
            :error error
            :processing false)

    :utils/show-popup
    {:title   (i18n/label :account-and-db-password-mismatch-title)
     :content (i18n/label :account-and-db-password-mismatch-content)}

    :dispatch [:accounts.logout.ui/logout-confirmed]}))

(fx/defn user-login-callback
  [{:keys [db web3] :as cofx} login-result]
  (let [data    (types/json->clj login-result)
        error   (:error data)
        success (empty? error)
        {:keys [address password save-password?]}
        (accounts.db/credentials cofx)
        network-type (:network/type db)]
    ;; check if logged into account
    (when address
      (if success
        (fx/merge
         cofx
         {:db                       (-> db
                                        (dissoc :accounts/login)
                                        (update :hardwallet dissoc
                                                :on-card-read
                                                :card-read-in-progress?
                                                :pin
                                                :whisper-private-key
                                                :encryption-public-key))
          :web3/set-default-account [web3 address]
          :web3/fetch-node-version  [web3
                                     #(re-frame/dispatch
                                       [:web3/fetch-node-version-callback %])]}
         (fn [_]
           (when save-password?
             {:keychain/save-user-password [address password]}))
         (mobile-network/on-network-status-change)
         (protocol/initialize-protocol)
         (universal-links/process-stored-event)
         (chaos-mode/check-chaos-mode)
         #(when-not platform/desktop?
            (initialize-wallet %)))
        (account-and-db-password-do-not-match cofx error)))))

(fx/defn show-migration-error-dialog
  [{:keys [db]} realm-error]
  (let [{:keys [message]} realm-error
        address           (get-in db [:accounts/login :address])
        erase-button (i18n/label :migrations-erase-accounts-data-button)]
    {:ui/show-confirmation
     {:title               (i18n/label :invalid-key-title)
      :content             (i18n/label
                            :invalid-key-content
                            {:message                         message
                             :erase-accounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/account-data-reset-accepted address])}}))
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
           (let [{:keys [address password]} (accounts.db/credentials cofx)]
             {:data-store/change-account [address
                                          password
                                          true
                                          (get-in cofx
                                                  [:db
                                                   :accounts/accounts
                                                   address
                                                   :settings
                                                   :fleet])]}))
         {:db (update db :accounts/login assoc
                      :error error
                      :processing false)})))))

(fx/defn migrations-failed
  [{:keys [db]} {:keys [realm-error erase-button]}]
  (let [{:keys [message details]} realm-error
        address (get-in db [:accounts/login :address])]
    {:ui/show-confirmation
     {:title               (i18n/label :migrations-failed-title)
      :content             (i18n/label
                            :migrations-failed-content
                            (merge
                             {:message                         message
                              :erase-accounts-data-button-text erase-button}
                             details))
      :confirm-button-text erase-button
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/account-data-reset-accepted address])}}))

(fx/defn verify-account
  [{:keys [db] :as cofx} {:keys [realm-error]}]
  (fx/merge cofx
            {:db (-> db
                     (assoc :node/on-ready :verify-account)
                     (assoc :realm-error realm-error))}
            (node/initialize nil)))

(fx/defn unknown-realm-error
  [cofx {:keys [realm-error erase-button]}]
  (let [{:keys [message]} realm-error
        {:keys [address]} (accounts.db/credentials cofx)]
    {:ui/show-confirmation
     {:title               (i18n/label :unknown-realm-error)
      :content             (i18n/label
                            :unknown-realm-error-content
                            {:message                         message
                             :erase-accounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-cancel           #(re-frame/dispatch
                             [:init.ui/data-reset-cancelled ""])
      :on-accept           #(re-frame/dispatch
                             [:init.ui/account-data-reset-accepted address])}}))

(fx/defn handle-change-account-error
  [{:keys [db] :as cofx} error]
  (let [{:keys [error] :as realm-error}
        (if (map? error)
          error
          {:message (str error)})
        erase-button (i18n/label :migrations-erase-accounts-data-button)]
    (fx/merge
     cofx
     {:db (assoc-in db [:accounts/login :save-password?] false)}
     (case error
       :migrations-failed
       (migrations-failed {:realm-error  realm-error
                           :erase-button erase-button})

       (:database-does-not-exist :decryption-failed)
       (verify-account {:realm-error realm-error})

       (unknown-realm-error {:realm-error  realm-error
                             :erase-button erase-button})))))

(fx/defn open-keycard-login
  [{:keys [db] :as cofx}]
  (let [navigation-stack (:navigation-stack db)]
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :pin :enter-step] :login)}
              (if (empty? navigation-stack)
                (navigation/navigate-to-cofx :accounts nil)
                (navigation/navigate-to-cofx :enter-pin nil)))))

(fx/defn get-user-password
  [_ address]
  {:keychain/can-save-user-password? nil
   :keychain/get-user-password       [address
                                      #(re-frame/dispatch [:accounts.login.callback/get-user-password-success %])]})

(fx/defn open-login [{:keys [db] :as cofx} address photo-path name]
  (let [keycard-account? (get-in db [:accounts/accounts address :keycard-instance-uid])]
    (fx/merge cofx
              {:db (-> db
                       (update :accounts/login assoc
                               :address address
                               :photo-path photo-path
                               :name name)
                       (update :accounts/login dissoc
                               :error
                               :password))}
              (if keycard-account?
                (open-keycard-login)
                (get-user-password address)))))

(fx/defn open-login-callback
  [{:keys [db] :as cofx} password]
  (if password
    (fx/merge cofx
              {:db (assoc-in db [:accounts/login :password] password)}
              (navigation/navigate-to-cofx :progress nil)
              (user-login false))
    (navigation/navigate-to-cofx cofx :login nil)))

(re-frame/reg-fx
 :accounts.login/login
 (fn [[address password]]
   (login! address (security/safe-unmask-data password))))

(re-frame/reg-fx
 :accounts.login/verify
 (fn [[address password realm-error]]
   (verify! address (security/safe-unmask-data password) realm-error)))

(re-frame/reg-fx
 :accounts.login/clear-web-data
 clear-web-data!)

(re-frame/reg-fx
 :data-store/change-account
 (fn [[address password create-database-if-not-exist? current-fleet]]
   (change-account! address
                    (security/safe-unmask-data password)
                    create-database-if-not-exist?
                    current-fleet)))
