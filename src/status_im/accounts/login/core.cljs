(ns status-im.accounts.login.core
  (:require [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.data-store.core :as data-store]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.utils.security :as security]
            [status-im.utils.platform :as platform]
            [status-im.protocol.core :as protocol]
            [status-im.models.wallet :as models.wallet]
            [status-im.models.transactions :as transactions]
            [status-im.i18n :as i18n]
            [status-im.node.core :as node]))

(defn login! [address password]
  (status/login address password #(re-frame/dispatch [:accounts.login.callback/login-success %])))

(defn verify! [address password realm-error]
  (status/verify address password
                 #(re-frame/dispatch
                   [:accounts.login.callback/verify-success % realm-error])))

(defn clear-web-data! []
  (status/clear-web-data))

(defn change-account! [address password create-database-if-not-exist?]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then #(data-store/change-account address password % create-database-if-not-exist?))
      (then (fn [] (re-frame/dispatch [:init.callback/account-change-success address])))
      (catch (fn [error]
               (log/warn "Could not change account" error)
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [:init.callback/account-change-error error])))))

;;;; Handlers
(fx/defn login [cofx]
  (let [{:keys [address password]} (accounts.db/credentials cofx)]
    {:accounts.login/login [address password]}))

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
      :data-store/change-account     [address password create-database?]})))

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
        (accounts.db/credentials cofx)]
    ;; check if logged into account
    (when address
      (if success
        (fx/merge
         cofx
         {:db                       (dissoc db :accounts/login)
          :web3/set-default-account [web3 address]
          :web3/fetch-node-version  [web3
                                     #(re-frame/dispatch
                                       [:web3/fetch-node-version-callback %])]}
         (fn [_]
           (when save-password?
             {:keychain/save-user-password [address password]}))
         (protocol/initialize-protocol)
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
             {:data-store/change-account [address password true]}))
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

(fx/defn open-login [{:keys [db]} address photo-path name]
  {:db (-> db
           (update :accounts/login assoc
                   :address address
                   :photo-path photo-path
                   :name name)
           (update :accounts/login dissoc
                   :error
                   :password))
   :keychain/can-save-user-password? nil
   :keychain/get-user-password [address
                                #(re-frame/dispatch [:accounts.login.callback/get-user-password-success %])]})

(fx/defn open-login-callback
  [{:keys [db] :as cofx} password]
  (if password
    (fx/merge cofx
              {:db (assoc-in db [:accounts/login :password] password)}
              (navigation/navigate-to-cofx :progress nil)
              (user-login false))
    (navigation/navigate-to-clean cofx :login nil)))

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
 (fn [[address password create-database-if-not-exist?]]
   (change-account! address (security/safe-unmask-data password)
                    create-database-if-not-exist?)))
