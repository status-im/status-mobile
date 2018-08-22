(ns status-im.models.wallet
  (:require [clojure.set :as set]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.transport.utils :as transport.utils]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.money :as money]))

(def min-gas-price-wei (money/bignumber 1))

(defmulti invalid-send-parameter? (fn [type _] type))

(defmethod invalid-send-parameter? :gas-price [_ value]
  (cond
    (not value) :invalid-number
    (< (money/->wei :gwei value) min-gas-price-wei) :not-enough-wei))

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
(defn prepare-dapp-transaction [{{:keys [id method params]} :payload :as queued-transaction} contacts]
  (let [{:keys [to value data gas gasPrice nonce]} (first params)
        contact (get contacts (utils.hex/normalize-hex to))]
    (cond-> {:id               (str id)
             :to-name          (or (when (nil? to)
                                     (i18n/label :t/new-contract))
                                   contact)
             :symbol           :ETH
             :method           method
             :dapp-transaction queued-transaction
             :to               to
             :amount           (money/bignumber (or value 0))
             :gas              (cond
                                 gas
                                 (money/bignumber gas)
                                 (and value (empty? data))
                                 (money/bignumber 21000))
             :gas-price        (when gasPrice
                                 (money/bignumber gasPrice))
             :data             data}
      nonce
      (assoc :nonce nonce))))

;; SEND TRANSACTION -> RPC TRANSACTION
(defn prepare-send-transaction [from {:keys [amount to gas gas-price data nonce]}]
  (cond-> {:from     (ethereum/normalized-address from)
           :to       (ethereum/normalized-address to)
           :value    (ethereum/int->hex amount)
           :gas      (ethereum/int->hex gas)
           :gasPrice (ethereum/int->hex gas-price)}
    data
    (assoc :data data)
    nonce
    (assoc :nonce nonce)))

;; NOTE (andrey) we need this function, because params may be mixed up, so we need to figure out which one is address
;; and which message
(defn normalize-sign-message-params [params]
  (let [first_param           (first params)
        second_param          (second params)
        first-param-address?  (ethereum/address? first_param)
        second-param-address? (ethereum/address? second_param)]
    (when (or first-param-address? second-param-address?)
      (if first-param-address?
        [first_param second_param]
        [second_param first_param]))))

(defn web3-error-callback [fx {:keys [webview-bridge]} {:keys [message-id]} message]
  (assoc fx :send-to-bridge-fx [{:type      constants/web3-send-async-callback
                                 :messageId message-id
                                 :error     message}
                                webview-bridge]))

(defn dapp-complete-transaction [id result method message-id webview]
  (cond-> {:send-to-bridge-fx [{:type      constants/web3-send-async-callback
                                :messageId message-id
                                :result    {:jsonrpc "2.0"
                                            :id      (int id)
                                            :result  result}}
                               webview]
           :dispatch          [:navigate-back]}

    (= method constants/web3-send-transaction)
    (assoc :dispatch-later [{:ms 400 :dispatch [:navigate-to-modal :wallet-transaction-sent-modal]}])))

(defn discard-transaction
  [{:keys [db]}]
  (let [{:keys [dapp-transaction]} (get-in db [:wallet :send-transaction])]
    (cond-> {:db (update db :wallet
                         assoc
                         :send-transaction {}
                         :transactions-queue nil)}
      dapp-transaction
      (web3-error-callback db dapp-transaction "discarded"))))

(defn prepare-unconfirmed-transaction [db now hash]
  (let [transaction (get-in db [:wallet :send-transaction])]
    (let [chain (:chain db)
          token (tokens/symbol->token (keyword chain) (:symbol transaction))]
      (-> transaction
          (assoc :confirmations "0"
                 :timestamp (str now)
                 :type :outbound
                 :hash hash
                 :value (:amount transaction)
                 :token token
                 :gas-limit (str (:gas transaction)))
          (update :gas-price str)
          (dissoc :message-id :id :gas)))))

(defn handle-transaction-error [db {:keys [code message]}]
  (let [{:keys [dapp-transaction]} (get-in db [:wallet :send-transaction])]
    (case code

      ;;WRONG PASSWORD
      constants/send-transaction-err-decrypt
      {:db (-> db
               (assoc-in [:wallet :send-transaction :wrong-password?] true))}

      (cond-> {:db (-> db
                       navigation/navigate-back
                       (assoc-in [:wallet :transactions-queue] nil)
                       (assoc-in [:wallet :send-transaction] {}))
               :wallet/show-transaction-error message}

        dapp-transaction
        (web3-error-callback db dapp-transaction message)))))

(defn transform-data-for-message [{:keys [method] :as transaction}]
  (cond-> transaction
    (= method constants/web3-personal-sign)
    (update :data transport.utils/to-utf8)))

(defn clear-error-message [db error-type]
  (update-in db [:wallet :errors] dissoc error-type))

(defn tokens-symbols [v chain]
  (set/difference (set v) (set (map :symbol (tokens/nfts-for chain)))))

(defn update-wallet
  [{{:keys [web3 network network-status] {:keys [address settings]} :account/account :as db} :db}]
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
