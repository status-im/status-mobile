(ns legacy.status-im.subs.wallet.signing
  (:require
    [clojure.string :as string]
    [legacy.status-im.ethereum.tokens :as tokens]
    [legacy.status-im.signing.gas :as signing.gas]
    [legacy.status-im.wallet.db :as wallet.db]
    [re-frame.core :as re-frame]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.money :as money]))

(re-frame/reg-sub
 ::send-transaction
 :<- [:wallet-legacy]
 (fn [wallet]
   (:send-transaction wallet)))

(re-frame/reg-sub
 :wallet-legacy.send/symbol
 :<- [::send-transaction]
 (fn [send-transaction]
   (:symbol send-transaction)))

(re-frame/reg-sub
 :wallet-legacy.send/camera-flashlight
 :<- [::send-transaction]
 (fn [send-transaction]
   (:camera-flashlight send-transaction)))

(re-frame/reg-sub
 :wallet-legacy/settings
 :<- [:wallet-legacy]
 (fn [{:keys [settings]}]
   (reduce-kv #(conj %1 %3) [] settings)))

(re-frame/reg-sub
 :wallet-legacy.request/transaction
 :<- [:wallet-legacy]
 :request-transaction)

(re-frame/reg-sub
 :wallet-legacy/binance-chain?
 :<- [:current-network]
 (fn [network]
   (chain/binance-chain-id? (get-in network [:config :NetworkId]))))

(re-frame/reg-sub
 :signing/fee
 :<- [:signing/tx]
 (fn [{:keys [gas gasPrice maxFeePerGas]}]
   (signing.gas/calculate-max-fee gas (or maxFeePerGas gasPrice))))

(re-frame/reg-sub
 :signing/currencies
 :<- [:prices]
 :<- [:wallet-legacy/currency]
 :<- [:ethereum/native-currency]
 (fn [[prices {:keys [code]} {sym :symbol}]]
   [(name sym)
    code
    (get-in prices [sym (keyword code)])]))

(re-frame/reg-sub
 :signing/priority-fee-suggestions-range
 :<- [:wallet-legacy/current-priority-fee]
 :<- [:wallet-legacy/slow-base-fee]
 :<- [:wallet-legacy/normal-base-fee]
 :<- [:wallet-legacy/fast-base-fee]
 (fn [[latest-tip slow normal fast]]
   (reduce
    (fn [acc [k fees]]
      (assoc acc
             k
             (reduce
              (fn [acc [k fee]]
                (assoc acc
                       k
                       (-> fee
                           money/wei->gwei
                           (money/to-fixed 2))))
              {}
              fees)))
    {}
    (signing.gas/get-fee-options latest-tip slow normal fast))))

(re-frame/reg-sub
 :signing/phrase
 :<- [:profile/profile]
 (fn [{:keys [signing-phrase]}]
   signing-phrase))

(re-frame/reg-sub
 :signing/sign-message
 :<- [:signing/sign]
 :<- [:profile/wallet-accounts]
 :<- [:prices]
 (fn [[sign wallet-accounts prices]]
   (if (= :pinless (:type sign))
     (let [message    (get-in sign [:formatted-data :message])
           wallet-acc (some #(when (= (:address %) (:receiver message)) %) wallet-accounts)]
       (cond-> sign
         (and (:amount message) (:currency message))
         (assoc :fiat-amount
                (money/fiat-amount-value (:amount message)
                                         (:currency message)
                                         :USD
                                         prices)
                :fiat-currency "USD")
         (and (:receiver message) wallet-acc)
         (assoc :account wallet-acc)))
     sign)))

(defn- too-precise-amount?
  "Checks if number has any extra digit beyond the allowed number of decimals.
  It does so by checking the number against its rounded value."
  [amount decimals]
  (let [^js bn (money/bignumber amount)]
    (not (.eq bn (.round bn decimals)))))

(defn get-amount-error
  [amount decimals]
  (when (and (seq amount) decimals)
    (let [normalized-amount (money/normalize amount)
          value             (money/bignumber normalized-amount)]
      (cond
        (not (money/valid? value))
        {:amount-error (i18n/label :t/validation-amount-invalid-number)}

        (too-precise-amount? normalized-amount decimals)
        {:amount-error (i18n/label :t/validation-amount-is-too-precise {:decimals decimals})}

        :else nil))))

(defn get-sufficient-funds-error
  [balance sym amount]
  (when-not (money/sufficient-funds? amount (get balance sym))
    {:amount-error (i18n/label :t/wallet-insufficient-funds)}))

(defn gas-required-exceeds-allowance?
  [gas-error-message]
  (and gas-error-message
       (string/starts-with?
        gas-error-message
        "gas required exceeds allowance")))

(defn get-sufficient-gas-error
  [gas-error-message balance sym amount ^js gas ^js gasPrice]
  (if (and gas gasPrice)
    (let [^js fee               (.times gas gasPrice)
          ^js available-ether   (money/bignumber (get balance :ETH 0))
          ^js available-for-gas (if (= :ETH sym)
                                  (.minus available-ether (money/bignumber amount))
                                  available-ether)]
      (merge {:gas-error-state (when gas-error-message :gas-is-set)}
             (when-not (money/sufficient-funds? fee (money/bignumber available-for-gas))
               {:gas-error (i18n/label :t/wallet-insufficient-gas)})))
    (let [insufficient-balance? (gas-required-exceeds-allowance? gas-error-message)]
      {:gas-error-state       (when gas-error-message :gas-isnt-set)
       :insufficient-balalce? insufficient-balance?
       :gas-error             (if insufficient-balance?
                                (i18n/label :t/insufficient-balance-to-cover-fee)
                                (or gas-error-message
                                    (i18n/label :t/invalid-number)))})))

(re-frame/reg-sub
 :signing/amount-errors
 (fn [[_ address] _]
   [(re-frame/subscribe [:signing/tx])
    (re-frame/subscribe [:balance address])])
 (fn [[{:keys [amount token gas gasPrice maxFeePerGas approve? gas-error-message]} balance]]
   (let [gas-price (or maxFeePerGas gasPrice)]
     (if (and amount token (not approve?))
       (let [amount-bn    (money/formatted->internal (money/bignumber amount)
                                                     (:symbol token)
                                                     (:decimals token))
             amount-error (or (get-amount-error amount (:decimals token))
                              (get-sufficient-funds-error balance (:symbol token) amount-bn))]
         (merge
          amount-error
          (get-sufficient-gas-error gas-error-message balance (:symbol token) amount-bn gas gas-price)))
       (get-sufficient-gas-error gas-error-message balance nil nil gas gas-price)))))

(re-frame/reg-sub
 :wallet-legacy.send/prepare-transaction-with-balance
 :<- [:wallet-legacy/prepare-transaction]
 :<- [:wallet-legacy]
 :<- [:offline?]
 :<- [:wallet-legacy/all-tokens]
 :<- [:current-network]
 (fn [[{:keys [from to amount-text] :as transaction}
       wallet offline? all-tokens current-network]]
   (let [sym (:symbol transaction)
         balance (get-in wallet [:accounts (:address from) :balance])
         {:keys [decimals] :as token} (tokens/asset-for all-tokens current-network sym)
         {:keys [value error]} (wallet.db/parse-amount amount-text decimals)
         amount (money/formatted->internal value sym decimals)
         {:keys [amount-error] :as transaction-new}
         (merge transaction
                {:amount-error error}
                (when amount
                  (get-sufficient-funds-error balance sym amount)))]
     (assoc transaction-new
            :amount        amount
            :balance       balance
            :token         (assoc token :amount (get balance (:symbol token)))
            :sign-enabled? (and to
                                (nil? amount-error)
                                (not (nil? amount))
                                (not offline?))))))

(re-frame/reg-sub
 :wallet-legacy.request/prepare-transaction-with-balance
 :<- [:wallet-legacy/prepare-transaction]
 :<- [:wallet-legacy]
 :<- [:offline?]
 :<- [:wallet-legacy/all-tokens]
 :<- [:current-network]
 (fn [[{:keys [from to amount-text] :as transaction}
       wallet offline? all-tokens current-network]]
   (let [sym (:symbol transaction)
         balance (get-in wallet [:accounts (:address from) :balance])
         {:keys [decimals] :as token} (tokens/asset-for all-tokens current-network sym)
         {:keys [value error]} (wallet.db/parse-amount amount-text decimals)
         amount (money/formatted->internal value sym decimals)
         {:keys [amount-error] :as transaction-new}
         (assoc transaction :amount-error error)]
     (assoc transaction-new
            :amount        amount
            :balance       balance
            :token         (assoc token :amount (get balance (:symbol token)))
            :sign-enabled? (and to
                                from
                                (nil? amount-error)
                                (not (nil? amount))
                                (not offline?))))))
