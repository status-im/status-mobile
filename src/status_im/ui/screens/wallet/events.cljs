(ns status-im.ui.screens.wallet.events
  (:require [re-frame.core :as re-frame]
            [status-im.models.transactions :as wallet.transactions]
            [status-im.models.wallet :as models]
            [status-im.ui.screens.navigation :as navigation]
            status-im.ui.screens.wallet.navigation
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.utils.prices :as prices]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils.utils]))

(defn get-balance [{:keys [web3 account-id on-success on-error]}]
  (if (and web3 account-id)
    (.getBalance
     (.-eth web3)
     account-id
     (fn [err resp]
       (if-not err
         (on-success resp)
         (on-error err))))
    (on-error "web3 or account-id not available")))

(defn get-token-balance [{:keys [web3 contract account-id on-success on-error]}]
  (if (and web3 contract account-id)
    (erc20/balance-of
     web3
     contract
     (ethereum/normalized-address account-id)
     (fn [err resp]
       (if-not err
         (on-success resp)
         (on-error err))))
    (on-error "web3, contract or account-id not available")))

(defn assoc-error-message [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or err :unknown-error)))

;; FX

(re-frame/reg-fx
 :get-balance
 (fn [{:keys [web3 account-id success-event error-event]}]
   (get-balance {:web3       web3
                 :account-id account-id
                 :on-success #(re-frame/dispatch [success-event %])
                 :on-error   #(re-frame/dispatch [error-event %])})))

(re-frame/reg-fx
 :get-tokens-balance
 (fn [{:keys [web3 symbols all-tokens chain account-id success-event error-event]}]
   (doseq [symbol symbols]
     (let [contract (:address (tokens/symbol->token all-tokens chain symbol))]
       (get-token-balance {:web3       web3
                           :contract   contract
                           :account-id account-id
                           :on-success #(re-frame/dispatch [success-event symbol %])
                           :on-error   #(re-frame/dispatch [error-event symbol %])})))))

;; TODO(oskarth): At some point we want to get list of relevant assets to get prices for
(re-frame/reg-fx
 :get-prices
 (fn [{:keys [from to mainnet? success-event error-event]}]
   (prices/get-prices from
                      to
                      mainnet?
                      #(re-frame/dispatch [success-event %])
                      #(re-frame/dispatch [error-event %]))))

(re-frame/reg-fx
 :update-gas-price
 (fn [{:keys [web3 success-event edit?]}]
   (ethereum/gas-price web3 #(re-frame/dispatch [success-event %2 edit?]))))

(re-frame/reg-fx
 :update-estimated-gas
 (fn [{:keys [web3 obj success-event]}]
   (ethereum/estimate-gas-web3 web3 (clj->js obj) #(re-frame/dispatch [success-event %2]))))

(defn- validate-token-name! [web3 {:keys [address symbol name]}]
  (erc20/name web3 address #(when (and (seq %2) ;;NOTE(goranjovic): skipping check if field not set in contract
                                       (not= name %2))
                              (let [message (i18n/label :t/token-auto-validate-name-error
                                                        {:symbol   symbol
                                                         :expected name
                                                         :actual   %2
                                                         :address  address})]
                                (log/warn message)
                                (utils.utils/show-popup (i18n/label :t/warning) message)))))

(defn- validate-token-symbol! [web3 {:keys [address symbol]}]
  (erc20/symbol web3 address #(when (and (seq %2) ;;NOTE(goranjovic): skipping check if field not set in contract
                                         (not= (clojure.core/name symbol) %2))
                                (let [message (i18n/label :t/token-auto-validate-symbol-error
                                                          {:symbol   symbol
                                                           :expected (clojure.core/name symbol)
                                                           :actual   %2
                                                           :address  address})]
                                  (log/warn message)
                                  (utils.utils/show-popup (i18n/label :t/warning) message)))))

(defn- validate-token-decimals! [web3 {:keys [address symbol decimals nft? skip-decimals-check?]}]
  ;;NOTE(goranjovic): only skipping check if skip-decimals-check? flag is present because we can't differentiate
  ;;between unset decimals and 0 decimals.
  (when-not skip-decimals-check?
    (erc20/decimals web3 address #(when (and %2
                                             (not nft?)
                                             (not= decimals (int %2)))
                                    (let [message (i18n/label :t/token-auto-validate-decimals-error
                                                              {:symbol   symbol
                                                               :expected decimals
                                                               :actual   %2
                                                               :address  address})]
                                      (log/warn message)
                                      (utils.utils/show-popup (i18n/label :t/warning) message))))))

(re-frame/reg-fx
 :wallet/validate-tokens
 (fn [{:keys [web3 tokens]}]
   (doseq [token tokens]
     (validate-token-decimals! web3 token)
     (validate-token-symbol! web3 token)
     (validate-token-name! web3 token))))

;; Handlers
(handlers/register-handler-fx
 :update-wallet
 (fn [cofx _]
   (models/update-wallet cofx)))

(handlers/register-handler-fx
 :update-wallet-and-nav-back
 (fn [cofx [_ on-close]]
   (fx/merge cofx
             (when on-close
               {:dispatch on-close})
             (navigation/navigate-back)
             (models/update-wallet))))

(handlers/register-handler-fx
 :update-transactions
 (fn [{:keys [db]} _]
   {::wallet.transactions/sync-transactions-now
    (select-keys db [:network-status :account/account :app-state :network :web3])}))

(handlers/register-handler-fx
 :update-balance-success
 (fn [{:keys [db]} [_ balance]]
   {:db (-> db
            (assoc-in [:wallet :balance :ETH] balance)
            (assoc-in [:wallet :balance-loading?] false))}))

(handlers/register-handler-fx
 :update-balance-fail
 (fn [{:keys [db]} [_ err]]
   (log/debug "Unable to get balance: " err)
   {:db (-> db
            (assoc-error-message :balance-update :error-unable-to-get-balance)
            (assoc-in [:wallet :balance-loading?] false))}))

(fx/defn update-token-balance-success [{:keys [db]} symbol balance]
  {:db (-> db
           (assoc-in [:wallet :balance symbol] balance)
           (assoc-in [:wallet :balance-loading?] false))})

(handlers/register-handler-fx
 :update-token-balance-success
 (fn [cofx [_ symbol balance]]
   (update-token-balance-success cofx symbol balance)))

(handlers/register-handler-fx
 :update-token-balance-fail
 (fn [{:keys [db]} [_ symbol err]]
   (log/debug "Unable to get token " symbol "balance: " err)
   {:db (-> db
            (assoc-error-message :balance-update :error-unable-to-get-token-balance)
            (assoc-in [:wallet :balance-loading?] false))}))

(handlers/register-handler-fx
 :update-prices-success
 (fn [{:keys [db]} [_ prices]]
   {:db (assoc db
               :prices prices
               :prices-loading? false)}))

(handlers/register-handler-fx
 :update-prices-fail
 (fn [{:keys [db]} [_ err]]
   (log/debug "Unable to get prices: " err)
   {:db (-> db
            (assoc-error-message :prices-update :error-unable-to-get-prices)
            (assoc :prices-loading? false))}))

(handlers/register-handler-fx
 :show-transaction-details
 (fn [{:keys [db]} [_ hash]]
   {:db       (assoc-in db [:wallet :current-transaction] hash)
    :dispatch [:navigate-to :wallet-transaction-details]}))

(handlers/register-handler-fx
 :wallet/show-sign-transaction
 (fn [{:keys [db]} [_ {:keys [id method]} from-chat?]]
   {:db       (assoc-in db [:wallet :send-transaction] {:id         id
                                                        :method     method
                                                        :from-chat? from-chat?})
    :dispatch [:navigate-to-clean :wallet-send-transaction-modal]}))

(handlers/register-handler-fx
 :wallet/update-gas-price-success
 (fn [{:keys [db] :as cofx} [_ price edit?]]
   (if edit?
     (models/edit-value
      :gas-price
      (money/to-fixed
       (money/wei-> :gwei price))
      cofx)
     {:db (assoc-in db [:wallet :send-transaction :gas-price] price)})))

(handlers/register-handler-fx
 :wallet/update-estimated-gas
 (fn [{:keys [db]} [_ obj]]
   {:update-estimated-gas {:web3          (:web3 db)
                           :obj           obj
                           :success-event :wallet/update-estimated-gas-success}}))

(handlers/register-handler-fx
 :wallet/update-estimated-gas-success
 (fn [{:keys [db]} [_ gas]]
   (when gas
     (let [adjusted-gas (money/bignumber (int (* gas 1.2)))
           db-with-adjusted-gas (assoc-in db [:wallet :send-transaction :gas] adjusted-gas)]
       {:db (if (some? (-> db :wallet :send-transaction :original-gas))
              db-with-adjusted-gas
              (assoc-in db-with-adjusted-gas [:wallet :send-transaction :original-gas] adjusted-gas))}))))

(handlers/register-handler-fx
 :wallet.setup-ui/navigate-back-pressed
 (fn [{:keys [db] :as cofx}]
   (fx/merge cofx
             {:db (assoc-in db [:wallet :send-transaction] {})}
             (navigation/navigate-back))))
