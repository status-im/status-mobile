(ns status-im.ui.screens.wallet.events
  (:require [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.prices :as prices]
            [status-im.utils.transactions :as transactions]
            [taoensso.timbre :as log]
            status-im.ui.screens.wallet.request.events
            [status-im.utils.money :as money]
            [status-im.constants :as constants]))

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
  (assoc-in db [:wallet :errors error-type] (or (when err (str err))
                                                :unknown-error)))

(defn clear-error-message [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

;; FX

(reg-fx
 :get-balance
 (fn [{:keys [web3 account-id success-event error-event]}]
   (get-balance {:web3           web3
                 :account-id     account-id
                 :on-success     #(re-frame/dispatch [success-event %])
                 :on-error       #(re-frame/dispatch [error-event %])})))

(reg-fx
 :get-tokens-balance
 (fn [{:keys [web3 symbols chain account-id success-event error-event]}]
   (doseq [symbol symbols]
     (let [contract (:address (tokens/symbol->token chain symbol))]
       (get-token-balance {:web3           web3
                           :contract       contract
                           :account-id     account-id
                           :on-success     #(re-frame/dispatch [success-event symbol %])
                           :on-error       #(re-frame/dispatch [error-event %])})))))

(reg-fx
 :get-transactions
 (fn [{:keys [web3 chain account-id token-addresses success-event error-event]}]
   (transactions/get-transactions chain
                                  account-id
                                  #(re-frame/dispatch [success-event %])
                                  #(re-frame/dispatch [error-event %]))
   (doseq [direction [:inbound :outbound]]
     (erc20/get-token-transactions web3
                                   chain
                                   token-addresses
                                   direction
                                   account-id
                                   #(re-frame/dispatch [success-event %])))))

;; TODO(oskarth): At some point we want to get list of relevant assets to get prices for
(reg-fx
 :get-prices
 (fn [{:keys [from to success-event error-event]}]
   (prices/get-prices from
                      to
                      #(re-frame/dispatch [success-event %])
                      #(re-frame/dispatch [error-event %]))))

(reg-fx
 :update-gas-price
 (fn [{:keys [web3 success-event edit?]}]
   (ethereum/gas-price web3 #(re-frame/dispatch [success-event %2 edit?]))))

(reg-fx
 :update-estimated-gas
 (fn [{:keys [web3 obj success-event]}]
   (ethereum/estimate-gas-web3 web3 (clj->js obj) #(re-frame/dispatch [success-event %2]))))

;; Handlers
(handlers/register-handler-fx
 :update-wallet
 (fn [{{:keys [web3 account/account network network-status] {:keys [address settings]} :account/account :as db} :db} _]
   (let [network     (get-in db [:account/account :networks network])
         chain       (ethereum/network->chain-keyword network)
         mainnet?    (= :mainnet chain)
         symbols     (get-in settings [:wallet :visible-tokens chain])
         currency-id (or (get-in settings [:wallet :currency]) :usd)
         currency    (get constants/currencies currency-id)]
     (when (not= network-status :offline)
       {:get-balance {:web3          web3
                      :account-id    address
                      :success-event :update-balance-success
                      :error-event   :update-balance-fail}
        :get-tokens-balance {:web3          web3
                             :account-id    address
                             :symbols       symbols
                             :chain         chain
                             :success-event :update-token-balance-success
                             :error-event   :update-token-balance-fail}
        :get-prices  {:from          (if mainnet? (conj symbols "ETH") ["ETH"])
                      :to            [(:code currency)]
                      :success-event :update-prices-success
                      :error-event   :update-prices-fail}
        :db          (-> db
                         (clear-error-message :prices-update)
                         (clear-error-message :balance-update)
                         (assoc-in [:wallet :balance-loading?] true)
                         (assoc :prices-loading? true))}))))

(handlers/register-handler-fx
 :update-transactions
 (fn [{{:keys [network network-status web3] :as db} :db} _]
   (when (not= network-status :offline)
     (let [network         (get-in db [:account/account :networks network])
           chain           (ethereum/network->chain-keyword network)
           all-tokens      (tokens/tokens-for chain)
           token-addresses (map :address all-tokens)]
       {:get-transactions {:account-id      (get-in db [:account/account :address])
                           :token-addresses token-addresses
                           :chain           chain
                           :web3            web3
                           :success-event   :update-transactions-success
                           :error-event     :update-transactions-fail}
        :db               (-> db
                              (clear-error-message :transactions-update)
                              (assoc-in [:wallet :transactions-loading?] true))}))))

(defn combine-entries [transaction token-transfer]
  (merge transaction (select-keys token-transfer
                                  (if (= :ETH (:symbol transaction))
                                    [:symbol :from :to :value :type :token]
                                    [:confirmations]))))

(defn- tx-and-transfer?
  "A helper function that checks if first argument is a transaction and the second argument a token transfer object."
  [tx1 tx2]
  (and (not (:transfer tx1)) (:transfer tx2)))

(defn dedupe-transactions [tx1 tx2]
  (cond (tx-and-transfer? tx1 tx2) (combine-entries tx1 tx2)
        (tx-and-transfer? tx2 tx1) (combine-entries tx2 tx1)
        :else tx2))

(handlers/register-handler-db
 :update-transactions-success
 (fn [db [_ transactions]]
   (-> db
       (update-in [:wallet :transactions] #(merge-with dedupe-transactions % transactions))
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
 (fn [{:keys [db]} [_ {:keys [id method]} from-chat?]]
   {:db       (assoc-in db [:wallet :send-transaction] {:id         id
                                                        :method     method
                                                        :from-chat? from-chat?})
    :dispatch [:navigate-to-modal :wallet-send-transaction-modal]}))

(handlers/register-handler-db
 :wallet/update-gas-price-success
 (fn [db [_ price edit?]]
   (assoc-in db [:wallet (if edit? :edit :send-transaction) :gas-price] price)))

(handlers/register-handler-fx
 :wallet/update-estimated-gas
 (fn [{:keys [db]} [_ obj]]
   {:update-estimated-gas {:web3          (:web3 db)
                           :obj           obj
                           :success-event :wallet/update-estimated-gas-success}}))

(handlers/register-handler-db
 :wallet/update-estimated-gas-success
 (fn [db [_ gas]]
   (assoc-in db [:wallet :send-transaction :gas] (money/bignumber gas))))

(handlers/register-handler-fx
 :wallet/show-error
 (fn []
   {:show-error (i18n/label :t/wallet-error)}))
