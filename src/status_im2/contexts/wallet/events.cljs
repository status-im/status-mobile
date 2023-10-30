(ns status-im2.contexts.wallet.events
  (:require
    [native-module.core :as native-module]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/defn get-wallet-token
  {:events [:wallet/get-wallet-token]}
  [{:keys [db]}]
  (let [params (map :address (:profile/wallet-accounts db))]
    {:json-rpc/call [{:method     "wallet_getWalletToken"
                      :params     [params]
                      :on-success #(rf/dispatch [:wallet/get-wallet-token-success %])
                      :on-error   (fn [error]
                                    (log/info "failed to get wallet token"
                                              {:event  :wallet/get-wallet-token
                                               :error  error
                                               :params params}))}]}))

(rf/defn get-wallet-token-success
  {:events [:wallet/get-wallet-token-success]}
  [{:keys [db]} data]
  {:db (assoc db
              :wallet/tokens          data
              :wallet/tokens-loading? false)})

(rf/defn scan-address-success
  {:events [:wallet/scan-address-success]}
  [{:keys [db]} address]
  {:db (assoc db :wallet/scanned-address address)})

(rf/defn clean-scanned-address
  {:events [:wallet/clean-scanned-address]}
  [{:keys [db]}]
  {:db (dissoc db :wallet/scanned-address)})

(rf/reg-event-fx :wallet/create-derived-addresses
 (fn [{:keys [db]} [password {:keys [path]} on-success]]
   (let [{:keys [wallet-root-address]} (:profile/profile db)
         sha3-pwd                      (native-module/sha3 (str (security/safe-unmask-data password)))]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getDerivedAddresses"
              :params     [sha3-pwd wallet-root-address [path]]
              :on-success on-success
              :on-error   #(log/info "failed to derive address " %)}]]]})))

(rf/reg-event-fx :wallet/add-account
 (fn [{:keys [db]} [password {:keys [emoji account-name color]} {:keys [public-key address path]}]]
   (let [key-uid        (get-in db [:profile/profile :key-uid])
         sha3-pwd       (native-module/sha3 (security/safe-unmask-data password))
         account-config {:key-uid    key-uid
                         :wallet     false
                         :chat       false
                         :type       :generated
                         :name       account-name
                         :emoji      emoji
                         :path       path
                         :address    address
                         :public-key public-key
                         :colorID    color}]
     {:fx [[:json-rpc/call
            [{:method     "accounts_addAccount"
              :params     [sha3-pwd account-config]
              :on-success #(rf/dispatch [:navigate-to :wallet-accounts])
              :on-error   #(log/info "failed to create account " %)}]]]})))

(rf/reg-event-fx :wallet/derive-address-and-add-account
 (fn [_ [password account-details]]
   (let [on-success (fn [derived-adress-details]
                      (rf/dispatch [:wallet/add-account password account-details
                                    (first derived-adress-details)]))]
     {:fx [[:dispatch [:wallet/create-derived-addresses password account-details on-success]]]})))
