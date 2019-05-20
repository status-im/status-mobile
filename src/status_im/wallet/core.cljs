(ns status-im.wallet.core
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.utils :as wallet.utils]
            [status-im.utils.config :as config]
            [status-im.utils.core :as utils.core]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.fx :as fx]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.money :as money]
            [status-im.utils.prices :as prices]
            [status-im.utils.utils :as utils.utils]
            [taoensso.timbre :as log]))

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

(fx/defn open-send-transaction-modal
  [{:keys [db] :as cofx} id method from-chat?]
  (fx/merge cofx
            {:db (assoc-in db [:wallet :send-transaction] {:id         id
                                                           :method     method
                                                           :from-chat? from-chat?})}
            (navigation/navigate-to-clean :wallet-send-transaction-modal nil)))

;; FX

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

(re-frame/reg-fx
 :wallet/update-gas-price
 (fn [{:keys [success-event edit?]}]
   (json-rpc/call
    {:method "eth_gasPrice"
     :on-success
     #(re-frame/dispatch [success-event % edit?])})))

(re-frame/reg-fx
 :wallet/update-estimated-gas
 (fn [{:keys [obj success-event]}]
   (json-rpc/call
    {:method "eth_estimateGas"
     :params [obj]
     :on-success
     #(re-frame/dispatch [success-event %])})))

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

(def min-gas-price-wei (money/bignumber 1))

(defmulti invalid-send-parameter? (fn [type _] type))

(defmethod invalid-send-parameter? :gas-price [_ value]
  (cond
    (not value) :invalid-number
    (.lt (money/->wei :gwei value) min-gas-price-wei) :not-enough-wei
    (-> (money/->wei :gwei value) .decimalPlaces pos?) :invalid-number))

(defmethod invalid-send-parameter? :default [_ value]
  (when (or (not value)
            (<= value 0))
    :invalid-number))

(defn- calculate-max-fee
  [gas gas-price]
  (if (and gas gas-price)
    (money/to-fixed (money/wei->ether (.times gas gas-price)))
    "0"))

(defn- edit-max-fee [edit]
  (let [gas       (get-in edit [:gas-price :value-number])
        gas-price (get-in edit [:gas :value-number])]
    (assoc edit :max-fee (calculate-max-fee gas gas-price))))

(defn add-max-fee [{:keys [gas gas-price] :as transaction}]
  (assoc transaction :max-fee (calculate-max-fee gas gas-price)))

(defn build-edit [edit-value key value]
  "Takes the previous edit, either :gas or :gas-price and a value as string.
  Wei for gas, and gwei for gas price.
  Validates them and sets max fee"
  (let [bn-value (money/bignumber value)
        invalid? (invalid-send-parameter? key bn-value)
        data     (if invalid?
                   {:value    value
                    :max-fee  0
                    :invalid? invalid?}
                   {:value        value
                    :value-number (if (= :gas-price key)
                                    (money/->wei :gwei bn-value)
                                    bn-value)
                    :invalid?     false})]
    (-> edit-value
        (assoc key data)
        edit-max-fee)))

(defn edit-value
  [key value {:keys [db]}]
  {:db (update-in db [:wallet :edit] build-edit key value)})

;; DAPP TRANSACTION -> SEND TRANSACTION
(defn prepare-dapp-transaction [{{:keys [id method params]} :payload message-id :message-id} contacts]
  (let [{:keys [to value data gas gasPrice nonce]} (first params)
        contact (get contacts (utils.hex/normalize-hex to))]
    (cond-> {:id               (str id)
             :to-name          (or (when (nil? to)
                                     (i18n/label :t/new-contract))
                                   contact)
             :symbol           :ETH
             :method           method
             :to               to
             :amount           (money/bignumber (or value 0))
             :gas              (cond
                                 gas
                                 (money/bignumber gas)
                                 (and value (empty? data))
                                 (money/bignumber 21000))
             :gas-price        (when gasPrice
                                 (money/bignumber gasPrice))
             :data             data
             :on-result        [:wallet.dapp/transaction-on-result message-id]
             :on-error         [:wallet.dapp/transaction-on-error message-id]}
      nonce
      (assoc :nonce nonce))))

;; SEND TRANSACTION -> RPC TRANSACTION
(defn prepare-send-transaction
  [from {:keys [amount to gas gas-price data nonce]}]
  (cond-> {:from     (ethereum/normalized-address from)
           :to       (ethereum/normalized-address to)
           :value    (str "0x" (abi-spec/number-to-hex amount))
           :gas      (str "0x" (abi-spec/number-to-hex gas))
           :gasPrice (str "0x" (abi-spec/number-to-hex gas-price))}
    data
    (assoc :data data)
    nonce
    (assoc :nonce nonce)))

(defn normalize-sign-message-params
  "NOTE (andrey) we need this function, because params may be mixed up,
  so we need to figure out which one is address and which message"
  [params]
  (let [first_param           (first params)
        second_param          (second params)
        first-param-address?  (ethereum/address? first_param)
        second-param-address? (ethereum/address? second_param)]
    (when (or first-param-address? second-param-address?)
      (if first-param-address?
        [first_param second_param]
        [second_param first_param]))))

(defn web3-error-callback
  [fx {:keys [webview-bridge]} message-id message]
  (assoc fx :browser/send-to-bridge
         {:message {:type      constants/web3-send-async-callback
                    :messageId message-id
                    :error     message}
          :webview webview-bridge}))

(defn dapp-complete-transaction
  [id result method message-id webview keycard?]
  (cond-> {:browser/send-to-bridge
           {:message {:type      constants/web3-send-async-callback
                      :messageId message-id
                      :result    {:jsonrpc "2.0"
                                  :id      (int id)
                                  :result  result}}
            :webview webview}
           :dispatch [:navigate-back]}

    (constants/web3-sign-message? method)
    (assoc :dispatch (if keycard?
                       [:navigate-to :browser]
                       [:navigate-back]))

    (= method constants/web3-send-transaction)
    (assoc :dispatch [:navigate-to-clean :wallet-transaction-sent-modal])))

(fx/defn discard-transaction
  [{:keys [db]}]
  (let [{:keys [on-error]} (get-in db [:wallet :send-transaction])]
    (merge {:db (update db :wallet
                        assoc
                        :send-transaction {}
                        :transactions-queue nil)}
           (when on-error
             {:dispatch (conj on-error "transaction was cancelled by user")}))))

(defn prepare-unconfirmed-transaction
  [db now hash]
  (let [transaction (get-in db [:wallet :send-transaction])
        all-tokens  (:wallet/all-tokens db)]
    (let [chain (:chain db)
          token (tokens/symbol->token all-tokens (keyword chain) (:symbol transaction))]
      (-> transaction
          (assoc :timestamp (str now)
                 :type :pending
                 :hash hash
                 :value (:amount transaction)
                 :token token
                 :gas-limit (str (:gas transaction)))
          (update :gas-price str)
          (dissoc :message-id :id :gas)))))

(fx/defn handle-transaction-error
  [{:keys [db] :as cofx} {:keys [code message]}]
  (let [{:keys [on-error]} (get-in db [:wallet :send-transaction])]
    (log/warn :wallet/transaction-error
              :code code
              :message message)
    (case code
      ;;WRONG PASSWORD
      constants/send-transaction-err-decrypt
      {:db (-> db
               (assoc-in [:wallet :send-transaction :wrong-password?] true))}

      (fx/merge cofx
                (merge {:db (-> db
                                (assoc-in [:wallet :transactions-queue] nil)
                                (assoc-in [:wallet :send-transaction] {}))
                        :wallet/show-transaction-error message}
                       (when on-error
                         {:dispatch (conj on-error message)}))
                navigation/navigate-back))))

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
     {:keys [address settings]} :account/account :as db} :db :as cofx}]
  (let [normalized-address (ethereum/normalized-address address)
        chain  (ethereum/chain-keyword db)
        assets (get-in settings [:wallet :visible-tokens chain])
        tokens (->> (tokens/tokens-for all-tokens chain)
                    (remove #(or (:hidden? %)
                                 (:nft? %)))
                    (filter #((or assets identity) (:symbol %))))]
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
           (if assets
             (re-frame/dispatch
              [:wallet.callback/update-token-balance-success symbol balance])
             ;; NOTE: when there is no visible assets set,
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
  [{{:keys [network network-status :wallet/all-tokens]
     {:keys [address settings networks]} :account/account :as db} :db}]
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

(defn open-modal-wallet-for-transaction
  [db transaction tx-object]
  (let [{:keys [gas gas-price]} transaction
        {:keys [wallet-set-up-passed?]} (:account/account db)]
    {:db         (-> db
                     (assoc-in [:navigation/screen-params :wallet-send-modal-stack :modal?] true)
                     (assoc-in [:wallet :send-transaction] transaction)
                     (assoc-in [:wallet :send-transaction :original-gas] gas))
     :dispatch-n [(when-not gas
                    [:TODO.remove/update-estimated-gas tx-object])
                  (when-not gas-price
                    [:wallet/update-gas-price])
                  [:navigate-to
                   (if wallet-set-up-passed?
                     :wallet-send-modal-stack
                     :wallet-send-modal-stack-with-onboarding)]]}))

(defn send-transaction-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db
                 [:navigation/screen-params :wallet-send-modal-stack :modal?]
                 false)})

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

(fx/defn update-gas-price
  [{:keys [db] :as cofx} price edit?]
  (if edit?
    (edit-value
     :gas-price
     (money/to-fixed
      (money/wei-> :gwei price))
     cofx)
    {:db (assoc-in db [:wallet :send-transaction :gas-price] price)}))

(fx/defn update-estimated-gas-price
  [{:keys [db]} gas]
  (when gas
    (let [adjusted-gas (money/bignumber (int (* gas 1.2)))
          db-with-adjusted-gas (assoc-in db
                                         [:wallet :send-transaction :gas]
                                         adjusted-gas)]
      {:db (if (some? (-> db :wallet :send-transaction :original-gas))
             db-with-adjusted-gas
             (assoc-in db-with-adjusted-gas
                       [:wallet :send-transaction :original-gas]
                       adjusted-gas))})))

(defn update-toggle-in-settings
  [{{:account/keys [account]} :db} symbol checked?]
  (let [network      (get (:networks account) (:network account))
        chain        (ethereum/network->chain-keyword network)
        settings     (get account :settings)]
    (update-in settings [:wallet :visible-tokens chain] #(set-checked % symbol checked?))))

(fx/defn toggle-visible-token
  [cofx symbol checked?]
  (let [new-settings (update-toggle-in-settings cofx symbol checked?)]
    (accounts.update/update-settings cofx new-settings {})))

(fx/defn add-custom-token
  [{{:account/keys [account]} :db :as cofx} {:keys [symbol address] :as token}]
  (let [network      (get (:networks account) (:network account))
        chain        (ethereum/network->chain-keyword network)
        settings     (update-toggle-in-settings cofx symbol true)
        new-settings (assoc-in settings [:wallet :custom-tokens chain address] token)]
    (accounts.update/update-settings cofx new-settings {})))

(fx/defn configure-token-balance-and-visibility
  [cofx symbol balance]
  (fx/merge cofx
            (toggle-visible-token symbol true)
            ;;TODO(goranjovic): move `update-token-balance-success` function to wallet models
            (update-token-balance symbol balance)))

(fx/defn eth-transaction-call
  [{:keys [db] :as cofx}
   {:keys [contract method params on-success on-error details] :as transaction}]
  (let [current-address (ethereum/current-address db)
        transaction (merge {:to        contract
                            :from      current-address
                            :data      (abi-spec/encode method params)
                            :id        "approve"
                            :symbol    :ETH
                            :method    "eth_sendTransaction"
                            :amount    (money/bignumber 0)
                            :on-success on-success
                            :on-error  on-error}
                           details)
        go-to-view-id (if (get-in db [:account/account :wallet-set-up-passed?])
                        :wallet-send-modal-stack
                        :wallet-send-modal-stack-with-onboarding)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:navigation/screen-params :wallet-send-modal-stack :modal?] true)
                       (assoc-in [:wallet :send-transaction]
                                 transaction))
               :wallet/update-estimated-gas
               {:obj           (select-keys transaction [:to :from :data])
                :success-event :wallet/update-estimated-gas-success}

               :wallet/update-gas-price
               {:success-event :wallet/update-gas-price-success
                :edit?         false}}
              (navigation/navigate-to-cofx go-to-view-id {}))))
