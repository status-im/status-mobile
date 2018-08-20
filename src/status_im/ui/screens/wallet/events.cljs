(ns status-im.ui.screens.wallet.events
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame :refer [dispatch reg-fx]]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.prices :as prices]
            [status-im.utils.transactions :as transactions]
            [status-im.models.wallet :as models.wallet]
            [taoensso.timbre :as log]
            status-im.ui.screens.wallet.request.events
            [status-im.constants :as constants]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.money :as money]
            [status-im.models.transactions :as wallet.transactions]))

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

(defn clear-error-message [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

;; FX

(reg-fx
 :get-balance
 (fn [{:keys [web3 account-id success-event error-event]}]
   (get-balance {:web3       web3
                 :account-id account-id
                 :on-success #(re-frame/dispatch [success-event %])
                 :on-error   #(re-frame/dispatch [error-event %])})))

(reg-fx
 :get-tokens-balance
 (fn [{:keys [web3 symbols chain account-id success-event error-event]}]
   (doseq [symbol symbols]
     (let [contract (:address (tokens/symbol->token chain symbol))]
       (get-token-balance {:web3       web3
                           :contract   contract
                           :account-id account-id
                           :on-success #(re-frame/dispatch [success-event symbol %])
                           :on-error   #(re-frame/dispatch [error-event symbol %])})))))

(reg-fx
 :get-transactions
 (fn [{:keys [web3 chain account-id token-addresses success-event error-event]}]
   (transactions/get-transactions chain
                                  account-id
                                  #(re-frame/dispatch [success-event % account-id])
                                  #(re-frame/dispatch [error-event %]))
   (doseq [direction [:inbound :outbound]]
     (erc20/get-token-transactions web3
                                   chain
                                   token-addresses
                                   direction
                                   account-id
                                   #(re-frame/dispatch [success-event % account-id])))))

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

(defn tokens-symbols [v chain]
  (set/difference (set v) (set (map :symbol (tokens/nfts-for chain)))))

(defn update-wallet [{{:keys [web3 network network-status] {:keys [address settings]} :account/account :as db} :db}]
  (let [network     (get-in db [:account/account :networks network])
        chain       (ethereum/network->chain-keyword network)
        mainnet?    (= :mainnet chain)
        assets      (get-in settings [:wallet :visible-tokens chain])
        tokens      (tokens-symbols (get-in settings [:wallet :visible-tokens chain]) chain)
        currency-id (or (get-in settings [:wallet :currency]) :usd)
        currency    (get constants/currencies currency-id)]
    (when (not= network-status :offline)
      {:get-balance        {:web3          web3
                            :account-id    address
                            :success-event :update-balance-success
                            :error-event   :update-balance-fail}
       :get-tokens-balance {:web3          web3
                            :account-id    address
                            :symbols       assets
                            :chain         chain
                            :success-event :update-token-balance-success
                            :error-event   :update-token-balance-fail}
       :get-prices         {:from          (if mainnet? (conj tokens "ETH") ["ETH"])
                            :to            [(:code currency)]
                            :success-event :update-prices-success
                            :error-event   :update-prices-fail}
       :db                 (-> db
                               (clear-error-message :prices-update)
                               (clear-error-message :balance-update)
                               (assoc-in [:wallet :balance-loading?] true)
                               (assoc :prices-loading? true))})))

;; Handlers
(handlers/register-handler-fx
 :update-wallet
 (fn [cofx _]
   (update-wallet cofx)))

(handlers/register-handler-fx
 :update-transactions
 (fn [cofx _]
   (wallet.transactions/run-update cofx)))

(defn combine-entries [transaction token-transfer]
  (merge transaction (select-keys token-transfer [:symbol :from :to :value :type :token :transfer])))

(defn update-confirmations [tx1 tx2]
  (assoc tx1 :confirmations (max (:confirmations tx1)
                                 (:confirmations tx2))))

(defn- tx-and-transfer?
  "A helper function that checks if first argument is a transaction and the second argument a token transfer object."
  [tx1 tx2]
  (and (not (:transfer tx1)) (:transfer tx2)))

(defn- both-transfer?
  [tx1 tx2]
  (and (:transfer tx1) (:transfer tx2)))

(defn dedupe-transactions [tx1 tx2]
  (cond (tx-and-transfer? tx1 tx2) (combine-entries tx1 tx2)
        (tx-and-transfer? tx2 tx1) (combine-entries tx2 tx1)
        (both-transfer? tx1 tx2)   (update-confirmations tx1 tx2)
        :else tx2))

(defn own-transaction? [address [_ {:keys [type to from]}]]
  (let [normalized (ethereum/normalized-address address)]
    (or (and (= :inbound type) (= normalized (ethereum/normalized-address to)))
        (and (= :outbound type) (= normalized (ethereum/normalized-address from)))
        (and (= :failed type) (= normalized (ethereum/normalized-address from))))))

(handlers/register-handler-db
 :update-transactions-success
 (fn [db [_ transactions address]]
   ;; NOTE(goranjovic): we want to only show transactions that belong to the current account
   ;; this filter is to prevent any late transaction updates initated from another account on the same
   ;; device from being applied in the current account.
   (let [own-transactions (into {} (filter #(own-transaction? address %) transactions))]
     (-> db
         (update-in [:wallet :transactions] #(merge-with dedupe-transactions % own-transactions))
         (assoc-in [:wallet :transactions-loading?] false)))))

(handlers/register-handler-db
 :update-transactions-fail
 (fn [db [_ err]]
   (log/debug "Unable to get transactions: " err)
   (-> db
       (assoc-error-message :transactions-update :error-unable-to-get-transactions)
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
       (assoc-error-message :balance-update :error-unable-to-get-balance)
       (assoc-in [:wallet :balance-loading?] false))))

(defn update-token-balance-success [symbol balance {:keys [db]}]
  {:db (-> db
           (assoc-in [:wallet :balance symbol] balance)
           (assoc-in [:wallet :balance-loading?] false))})

(handlers/register-handler-fx
 :update-token-balance-success
 (fn [cofx [_ symbol balance]]
   (update-token-balance-success symbol balance cofx)))

(handlers/register-handler-db
 :update-token-balance-fail
 (fn [db [_ symbol err]]
   (log/debug "Unable to get token " symbol "balance: " err)
   (-> db
       (assoc-error-message :balance-update :error-unable-to-get-token-balance)
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
       (assoc-error-message :prices-update :error-unable-to-get-prices)
       (assoc :prices-loading? false))))

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
    :dispatch [:navigate-to-modal :wallet-send-transaction-modal]}))

(handlers/register-handler-db
 :wallet/update-gas-price-success
 (fn [db [_ price edit?]]
   (if edit?
     (:db (models.wallet/edit-value
           :gas-price
           (money/to-fixed
            (money/wei-> :gwei price))
           {:db db}))
     (assoc-in db [:wallet :send-transaction :gas-price] price))))

(handlers/register-handler-fx
 :wallet/update-estimated-gas
 (fn [{:keys [db]} [_ obj]]
   {:update-estimated-gas {:web3          (:web3 db)
                           :obj           obj
                           :success-event :wallet/update-estimated-gas-success}}))

(handlers/register-handler-db
 :wallet/update-estimated-gas-success
 (fn [db [_ gas]]
   (if gas
     (assoc-in db [:wallet :send-transaction :gas] (money/bignumber (int (* gas 1.2))))
     db)))

(handlers/register-handler-fx
 :wallet-setup-navigate-back
 (fn [{:keys [db]}]
   {:db (-> db
            (assoc-in [:wallet :send-transaction] {})
            (navigation/navigate-back))}))