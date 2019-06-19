(ns status-im.wallet.core
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.tokens :as tokens]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.utils.config :as config]
            [status-im.utils.core :as utils.core]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.utils.prices :as prices]
            [status-im.utils.utils :as utils.utils]
            [taoensso.timbre :as log]
            [status-im.wallet.db :as wallet.db]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.signing.core :as signing]))

(re-frame/reg-fx
 :wallet/get-balance
 (fn [{:keys [account-id on-success on-error]}]
   (json-rpc/call
    {:method     "eth_getBalance"
     :params     [account-id "latest"]
     :on-success on-success
     :on-error   on-error})))

;; TODO(oskarth): At some point we want to get list of relevant
;; assets to get prices for
(re-frame/reg-fx
 :wallet/get-prices
 (fn [{:keys [from to mainnet? success-event error-event chaos-mode?]}]
   (prices/get-prices from
                      to
                      mainnet?
                      #(re-frame/dispatch [success-event %])
                      #(re-frame/dispatch [error-event %])
                      chaos-mode?)))

(defn assoc-error-message [db error-type err]
  (assoc-in db [:wallet :errors error-type] (or err :unknown-error)))

(fx/defn on-update-prices-fail
  [{:keys [db]} err]
  (log/debug "Unable to get prices: " err)
  {:db (-> db
           (assoc-error-message :prices-update :error-unable-to-get-prices)
           (assoc :prices-loading? false))})

(fx/defn on-update-balance-fail
  [{:keys [db]} err]
  (log/debug "Unable to get balance: " err)
  {:db (-> db
           (assoc-error-message :balance-update :error-unable-to-get-balance)
           (assoc-in [:wallet :balance-loading?] false))})

(fx/defn on-update-token-balance-fail
  [{:keys [db]} symbol err]
  (log/debug "Unable to get token " symbol "balance: " err)
  {:db (-> db
           (assoc-error-message :balance-update :error-unable-to-get-token-balance)
           (assoc-in [:wallet :balance-loading?] false))})

(fx/defn open-transaction-details
  [{:keys [db] :as cofx} hash]
  (fx/merge cofx
            {:db (assoc-in db [:wallet :current-transaction] hash)}
            (navigation/navigate-to-cofx :wallet-transaction-details nil)))

(defn- validate-token-name!
  [{:keys [address symbol name]}]
  (json-rpc/eth-call
   {:contract address
    :method "name()"
    :outputs ["string"]
    :on-success
    (fn [[contract-name]]
      (when (and (not (empty? contract-name))
                 (not= name contract-name))
        (let [message (i18n/label :t/token-auto-validate-name-error
                                  {:symbol   symbol
                                   :expected name
                                   :actual   contract-name
                                   :address  address})]
          (log/warn message)
          (utils.utils/show-popup (i18n/label :t/warning) message))))}))

(defn- validate-token-symbol!
  [{:keys [address symbol]}]
  (json-rpc/eth-call
   {:contract address
    :method "symbol()"
    :outputs ["string"]
    :on-success
    (fn [[contract-symbol]]
      ;;NOTE(goranjovic): skipping check if field not set in contract
      (when (and (not (empty? contract-symbol))
                 (not= (clojure.core/name symbol) contract-symbol))
        (let [message (i18n/label :t/token-auto-validate-symbol-error
                                  {:symbol   symbol
                                   :expected (clojure.core/name symbol)
                                   :actual   contract-symbol
                                   :address  address})]
          (log/warn message)
          (utils.utils/show-popup (i18n/label :t/warning) message))))}))

(defn- validate-token-decimals!
  [{:keys [address symbol decimals nft?]}]
  (when-not nft?
    (json-rpc/eth-call
     {:contract address
      :method "decimals()"
      :outputs ["uint256"]
      :on-success
      (fn [[contract-decimals]]
        (when (and (not (nil? contract-decimals))
                   (not= decimals contract-decimals))
          (let [message (i18n/label :t/token-auto-validate-decimals-error
                                    {:symbol   symbol
                                     :expected decimals
                                     :actual   contract-decimals
                                     :address  address})]
            (log/warn message)
            (utils.utils/show-popup (i18n/label :t/warning) message))))})))

(re-frame/reg-fx
 :wallet/validate-tokens
 (fn [tokens]
   (doseq [token tokens]
     (validate-token-decimals! token)
     (validate-token-symbol! token)
     (validate-token-name! token))))

(re-frame/reg-fx
 :wallet/get-tokens-balance
 (fn [{:keys [wallet-address tokens on-success on-error]}]
   (doseq [{:keys [address symbol]} tokens]
     (json-rpc/eth-call
      {:contract   address
       :method     "balanceOf(address)"
       :params     [wallet-address]
       :outputs    ["uint256"]
       :on-success
       (fn [[balance]]
         (on-success symbol (money/bignumber balance)))
       :on-error   #(on-error symbol %)}))))

(defn clear-error-message [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

(defn tokens-symbols
  [visible-token-symbols all-tokens chain]
  (set/difference (set visible-token-symbols)
                  (set (map :symbol (tokens/nfts-for all-tokens chain)))))

(fx/defn initialize-tokens
  [{:keys [db] :as cofx}]
  (let [custom-tokens (get-in db [:account/account :settings :wallet :custom-tokens])
        chain         (ethereum/chain-keyword db)
        ;;TODO why do we need all tokens ? chain can be changed only through relogin
        all-tokens    (merge-with
                       merge
                       (utils.core/map-values #(utils.core/index-by :address %)
                                              tokens/all-default-tokens)
                       custom-tokens)]
    (fx/merge
     cofx
     (merge
      {:db (assoc db :wallet/all-tokens all-tokens)}
      (when config/erc20-contract-warnings-enabled?
        {:wallet/validate-tokens (get tokens/all-default-tokens chain)})))))

(fx/defn update-balances
  [{{:keys [network-status :wallet/all-tokens]
     {:keys [settings]} :account/account :as db} :db :as cofx}]
  (let [normalized-address (ethereum/current-address db)
        chain  (ethereum/chain-keyword db)
        assets (get-in settings [:wallet :visible-tokens chain])
        tokens (->> (tokens/tokens-for all-tokens chain)
                    (remove #(or (:hidden? %))))]
    (when (not= network-status :offline)
      (fx/merge
       cofx
       {:wallet/get-balance
        {:account-id normalized-address
         :on-success #(re-frame/dispatch
                       [:wallet.callback/update-balance-success %])
         :on-error   #(re-frame/dispatch
                       [:wallet.callback/update-balance-fail %])}

        :wallet/get-tokens-balance
        {:wallet-address normalized-address
         :tokens         tokens
         :on-success
         (fn [symbol balance]
           (if (and assets
                    (assets symbol))
             (re-frame/dispatch
              [:wallet.callback/update-token-balance-success symbol balance])
             ;; NOTE: when there it is not a visible assets
             ;; we make an initialization round
             (when (> balance 0)
               (re-frame/dispatch
                [:wallet/token-found symbol balance]))))
         :on-error
         (fn [symbol error]
           (re-frame/dispatch
            [:wallet.callback/update-token-balance-fail symbol error]))}

        :db
        (-> db
            (clear-error-message :balance-update)
            (assoc-in [:wallet :balance-loading?] true))}
       (when-not assets
         (accounts.update/update-settings
          (assoc-in settings
                    [:wallet :visible-tokens chain]
                    #{})
          {}))))))

(fx/defn update-prices
  [{{:keys [network-status :wallet/all-tokens]
     {:keys [address settings]} :account/account :as db} :db}]
  (let [chain       (ethereum/chain-keyword db)
        mainnet?    (= :mainnet chain)
        assets      (get-in settings [:wallet :visible-tokens chain])
        tokens      (tokens-symbols assets all-tokens chain)
        currency-id (or (get-in settings [:wallet :currency]) :usd)
        currency    (get constants/currencies currency-id)]
    (when (not= network-status :offline)
      {:wallet/get-prices
       {:from          (if mainnet?
                         (conj tokens "ETH")
                         [(-> (tokens/native-currency chain)
                              (wallet.utils/exchange-symbol))])
        :to            [(:code currency)]
        :mainnet?      mainnet?
        :success-event :wallet.callback/update-prices-success
        :error-event   :wallet.callback/update-prices-fail
        :chaos-mode?   (:chaos-mode? settings)}

       :db
       (-> db
           (clear-error-message :prices-update)
           (assoc :prices-loading? true))})))

(defn- set-checked [ids id checked?]
  (if checked?
    (conj (or ids #{}) id)
    (disj ids id)))

(fx/defn on-update-prices-success
  [{:keys [db]} prices]
  {:db (assoc db
              :prices prices
              :prices-loading? false)})

(fx/defn update-balance
  [{:keys [db]} balance]
  {:db (-> db
           (assoc-in [:wallet :balance :ETH] (money/bignumber balance))
           (assoc-in [:wallet :balance-loading?] false))})

(fx/defn update-token-balance
  [{:keys [db]} symbol balance]
  {:db (-> db
           (assoc-in [:wallet :balance symbol] (money/bignumber balance))
           (assoc-in [:wallet :balance-loading?] false))})

(defn update-toggle-in-settings
  [{{:account/keys [account] :as db} :db} symbol checked?]
  (let [chain        (ethereum/chain-keyword db)
        settings     (get account :settings)]
    (update-in settings [:wallet :visible-tokens chain] #(set-checked % symbol checked?))))

(fx/defn toggle-visible-token
  [cofx symbol checked?]
  (let [new-settings (update-toggle-in-settings cofx symbol checked?)]
    (accounts.update/update-settings cofx new-settings {})))

(fx/defn add-custom-token
  [{:keys [db] :as cofx} {:keys [symbol address] :as token}]
  (let [chain        (ethereum/chain-keyword db)
        settings     (update-toggle-in-settings cofx symbol true)
        new-settings (assoc-in settings [:wallet :custom-tokens chain address] token)]
    (accounts.update/update-settings cofx new-settings {})))

(fx/defn remove-custom-token
  [{:keys [db] :as cofx} {:keys [symbol address]}]
  (let [chain        (ethereum/chain-keyword db)
        settings     (update-toggle-in-settings cofx symbol false)
        new-settings (update-in settings [:wallet :custom-tokens chain] dissoc address)]
    (accounts.update/update-settings cofx new-settings {})))

(fx/defn configure-token-balance-and-visibility
  [cofx symbol balance]
  (fx/merge cofx
            (toggle-visible-token symbol true)
            ;;TODO(goranjovic): move `update-token-balance-success` function to wallet models
            (update-token-balance symbol balance)))

(defn set-and-validate-amount-db [db amount symbol decimals]
  (let [{:keys [value error]} (wallet.db/parse-amount amount decimals)]
    (-> db
        (assoc-in [:wallet :send-transaction :amount] (money/formatted->internal value symbol decimals))
        (assoc-in [:wallet :send-transaction :amount-text] amount)
        (assoc-in [:wallet :send-transaction :amount-error] error))))

(fx/defn set-and-validate-amount
  {:events [:wallet.send/set-and-validate-amount]}
  [{:keys [db]} amount symbol decimals]
  {:db (set-and-validate-amount-db db amount symbol decimals)})

(fx/defn set-symbol
  {:events [:wallet.send/set-symbol]}
  [{:keys [db]} symbol]
  {:db (-> db
           (assoc-in [:wallet :send-transaction :symbol] symbol)
           (assoc-in [:wallet :send-transaction :amount] nil)
           (assoc-in [:wallet :send-transaction :amount-text] nil)
           (assoc-in [:wallet :send-transaction :asset-error] nil))})

(fx/defn sign-transaction-button-clicked
  {:events  [:wallet.ui/sign-transaction-button-clicked]}
  [{:keys [db] :as cofx}]
  (let [{:keys [to symbol amount]} (get-in cofx [:db :wallet :send-transaction])
        {:keys [symbol address]} (tokens/asset-for (:wallet/all-tokens db) (keyword (:chain db)) symbol)
        amount-hex (str "0x" (abi-spec/number-to-hex amount))
        to-norm (ethereum/normalized-address to)]
    (signing/sign cofx {:tx-obj    (if (= symbol :ETH)
                                     {:to   to-norm
                                      :value amount-hex}
                                     {:to   (ethereum/normalized-address address)
                                      :data (abi-spec/encode "transfer(address,uint256)" [to-norm amount-hex])})
                        :on-result [:navigate-back]})))

(fx/defn set-and-validate-amount-request
  {:events [:wallet.request/set-and-validate-amount]}
  [{:keys [db]} amount symbol decimals]
  (let [{:keys [value error]} (wallet.db/parse-amount amount decimals)]
    {:db (-> db
             (assoc-in [:wallet :request-transaction :amount] (money/formatted->internal value symbol decimals))
             (assoc-in [:wallet :request-transaction :amount-text] amount)
             (assoc-in [:wallet :request-transaction :amount-error] error))}))

(fx/defn set-symbol-request
  {:events [:wallet.request/set-symbol]}
  [{:keys [db]} symbol]
  {:db (-> db
           (assoc-in [:wallet :request-transaction :symbol] symbol))})

(fx/defn wallet-receive-toggle-warning
  {:events [:wallet.ui/warning-chekbox-pressed]}
  [{:keys [db] :as cofx}]
  (let [settings (get-in db [:account/account :settings])]
    (fx/merge cofx
              (accounts.update/update-settings (update-in settings [:wallet :suppress-wallet-receive-warning] not) {}))))
