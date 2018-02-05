(ns status-im.ui.screens.wallet.events
  (:require [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.ui.screens.wallet.navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.prices :as prices]
            [status-im.utils.transactions :as transactions]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            status-im.ui.screens.wallet.request.events))

(defn get-balance [{:keys [web3 address on-success on-error]}]
  (if (and web3 address)
    (.getBalance
     (.-eth web3)
     address
     (fn [err resp]
       (if-not err
         (on-success resp)
         (on-error err))))
    (on-error "web3 or address not available")))

(defn get-token-balance [{:keys [web3 contract address on-success on-error]}]
  (if (and web3 contract address)
    (erc20/balance-of
     web3
     contract
     (ethereum/normalized-address address)
     (fn [err resp]
       (if-not err
         (on-success resp)
         (on-error err))))
    (on-error "web3, contract or address not available")))

(defn assoc-error-message [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or (when err (str err))
                                                :unknown-error)))

(defn clear-error-message [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

;; FX

(reg-fx
  :get-balance
  (fn [{:keys [web3 address success-event error-event]}]
    (get-balance {:web3        web3
                  :address     address
                  :on-success  #(dispatch [success-event %])
                  :on-error    #(dispatch [error-event %])})))

(reg-fx
  :get-tokens-balance
  (fn [{:keys [web3 symbols chain address success-event error-event]}]
    (doseq [symbol symbols]
      (let [contract (:address (tokens/symbol->token chain symbol))]
        (get-token-balance {:web3        web3
                            :contract    contract
                            :address     address
                            :on-success  #(dispatch [success-event symbol %])
                            :on-error    #(dispatch [error-event %])})))))

(reg-fx
  :get-transactions
  (fn [{:keys [network address success-event error-event]}]
    (transactions/get-transactions network
                                   address
                                   #(dispatch [success-event %])
                                   #(dispatch [error-event %]))))

;; TODO(oskarth): At some point we want to get list of relevant assets to get prices for
(reg-fx
  :get-prices
  (fn [{:keys [from to success-event error-event]}]
    (prices/get-prices from
                       to
                       #(dispatch [success-event %])
                       #(dispatch [error-event %]))))

;; Handlers

(handlers/register-handler-fx
  :update-wallet
  (fn [{{:keys [web3 accounts/account network network-status] :as db} :db} [_ symbols]]
    (let [address (:address account)]
      (when (not= network-status :offline)
        {:get-balance {:web3          web3
                       :address       address
                       :success-event :update-balance-success
                       :error-event   :update-balance-fail}
         :get-tokens-balance {:web3          web3
                              :address       address
                              :symbols       symbols
                              :chain         (ethereum/network->chain-keyword network)
                              :success-event :update-token-balance-success
                              :error-event   :update-token-balance-fail}
         :get-prices  {:from          "ETH"
                       :to            "USD"
                       :success-event :update-prices-success
                       :error-event   :update-prices-fail}
         :db          (-> db
                          (clear-error-message :prices-update)
                          (clear-error-message :balance-update)
                          (assoc-in [:wallet :balance-loading?] true)
                          (assoc :prices-loading? true))}))))

(handlers/register-handler-fx
  :update-transactions
  (fn [{{:keys [accounts/account network network-status] :as db} :db} _]
    (when (not= network-status :offline)
      {:get-transactions {:address       (:address account)
                          :network       network
                          :success-event :update-transactions-success
                          :error-event   :update-transactions-fail}
       :db               (-> db
                             (clear-error-message :transaction-update)
                             (assoc-in [:wallet :transactions-loading?] true))})))

(handlers/register-handler-db
  :update-transactions-success
  (fn [db [_ transactions]]
    (-> db
        (update-in [:wallet :transactions] merge transactions)
        (assoc-in [:wallet :transactions-loading?] false))))

(handlers/register-handler-db
  :update-transactions-fail
  (fn [db [_ err]]
    (log/debug "Unable to get transactions: " err)
    (-> db
        (assoc-error-message :transactions-update err)
        (assoc-in [:wallet :transactions-loading?] false))))

(handlers/register-handler-db
  :update-balance-success
  (fn [db [_ balance]]
    (-> db
        (assoc-in [:wallet :balance :ETH] balance)
        (assoc-in [:wallet :balance-loading?] false))))

(handlers/register-handler-db
  :update-balance-fail
  (fn [db [_ err]]
    (log/debug "Unable to get balance: " err)
    (-> db
        (assoc-error-message :balance-update err)
        (assoc-in [:wallet :balance-loading?] false))))

(handlers/register-handler-db
  :update-token-balance-success
  (fn [db [_ symbol balance]]
    (-> db
        (assoc-in [:wallet :balance symbol] balance)
        (assoc-in [:wallet :balance-loading?] false))))

(handlers/register-handler-db
  :update-token-balance-fail
  (fn [db [_ err]]
    (log/debug "Unable to get token balance: " err)
    (-> db
        (assoc-error-message :balance-update err)
        (assoc-in [:wallet :balance-loading?] false))))

(handlers/register-handler-db
  :update-prices-success
  (fn [db [_ prices]]
    (assoc db
           :prices prices
           :prices-loading? false)))

(handlers/register-handler-db
  :update-prices-fail
  (fn [db [_ err]]
    (log/debug "Unable to get prices: " err)
    (-> db
        (assoc-error-message :prices-update err)
        (assoc :prices-loading? false))))

(handlers/register-handler-fx
  :show-transaction-details
  (fn [{:keys [db]} [_ hash]]
    {:db (assoc-in db [:wallet :current-transaction] hash)
     :dispatch [:navigate-to :wallet-transaction-details]}))

(handlers/register-handler-fx
  :wallet/show-sign-transaction
  (fn [{:keys [db]} [_ id from-chat?]]
    {:db       (assoc-in db [:wallet :send-transaction] {:id id
                                                         :from-chat? from-chat?})
     :dispatch [:navigate-to-modal :wallet-send-transaction-modal]}))

(handlers/register-handler-fx
  :wallet/discard-unsigned-transaction
  (fn [_ [_ transaction-id]]
    {:discard-transaction transaction-id}))

(handlers/register-handler-fx
  :wallet/discard-unsigned-transaction-with-confirmation
  (fn [cofx [_ transaction-id]]
    {:show-confirmation {:title               (i18n/label :t/transactions-delete)
                         :content             (i18n/label :t/transactions-delete-content)
                         :confirm-button-text (i18n/label :t/confirm)
                         :on-accept           #(re-frame/dispatch [:wallet/discard-unsigned-transaction transaction-id])}}))
