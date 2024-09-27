(ns status-im.contexts.wallet.common.utils
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [quo.foundations.resources :as resources]
            [status-im.common.qr-codes.view :as qr-codes]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils.networks :as network-utils]
            [utils.hex :as utils.hex]
            [utils.money :as money]
            [utils.number :as number]
            [utils.string]))

(def missing-price-decimals 6) ; if we don't have the monetary value of the token, we default to 6 decimals
(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))

(defn cut-fiat-balance-to-two-decimals
  [balance]
  (let [valid-balance? (and balance
                            (or (number? balance) (.-toFixed balance)))]
    (as-> balance $
      (if valid-balance? $ 0)
      (.toFixed $ 2))))

(defn prettify-balance
  [currency-symbol balance]
  (let [formatted-symbol (if (> (count currency-symbol) 1)
                           (str currency-symbol " ")
                           currency-symbol)]
    (str formatted-symbol (cut-fiat-balance-to-two-decimals balance))))

(defn get-derivation-path
  [number-of-accounts]
  (str constants/path-wallet-root "/" number-of-accounts))

(defn format-derivation-path
  [path]
  (string/replace path "/" " / "))

(defn get-formatted-derivation-path
  [number-of-accounts]
  (let [path (get-derivation-path number-of-accounts)]
    (format-derivation-path path)))

(defn total-raw-balance-in-all-chains
  [balances-per-chain]
  (->> balances-per-chain
       (map (comp :raw-balance val))
       (reduce money/add)))

(defn extract-exponent
  [s]
  (if-let [index (string/index-of s "e")]
    (subs s (+ index 2))
    nil))

(defn calc-max-crypto-decimals
  [one-cent-value]
  (let [str-representation   (str one-cent-value)
        decimal-part         (second (clojure.string/split str-representation #"\."))
        exponent             (extract-exponent str-representation)
        zeroes-count         (count (take-while #(= \0 %) decimal-part))
        max-decimals         (or exponent zeroes-count)
        first-non-zero-digit (first (filter #(not (= \0 %)) decimal-part))]
    (if (= \1 first-non-zero-digit)
      (inc max-decimals)
      max-decimals)))

(defn token-usd-price
  [token]
  (get-in token [:market-values-per-currency :usd :price]))

(defn one-cent-value
  [token-price-in-usd]
  (if (pos? token-price-in-usd)
    (/ 0.01 token-price-in-usd)
    0))

(defn analyze-token-amount-for-price
  "For full details: https://github.com/status-im/status-mobile/issues/18225"
  [token-price-in-usd token-units]
  (if (or (nil? token-units)
          (not (money/bignumber? token-units))
          (money/equal-to token-units 0))
    {:zero-value? true}
    (let [cent-value (one-cent-value token-price-in-usd)]
      {:usd-cent-value              cent-value
       :standardized-decimals-count (if (nil? token-price-in-usd)
                                      missing-price-decimals
                                      (calc-max-crypto-decimals cent-value))})))

(defn cut-crypto-decimals-to-fit-usd-cents
  [token-units token-price-in-usd]
  (let [{:keys [zero-value? usd-cent-value standardized-decimals-count]}
        (analyze-token-amount-for-price token-price-in-usd token-units)]
    (cond
      zero-value?                    "0"
      (< token-units usd-cent-value) "0"
      :else                          (number/remove-trailing-zeroes
                                      (.toFixed token-units standardized-decimals-count)))))

(defn prettify-crypto-balance
  [token-symbol crypto-balance conversion-rate]
  (str (cut-crypto-decimals-to-fit-usd-cents crypto-balance conversion-rate)
       " "
       (string/upper-case token-symbol)))

(defn get-standard-crypto-format
  "For full details: https://github.com/status-im/status-mobile/issues/18225"
  [token token-units]
  (let [token-price-in-usd (token-usd-price token)
        {:keys [zero-value? usd-cent-value standardized-decimals-count]}
        (analyze-token-amount-for-price token-price-in-usd token-units)]
    (cond
      zero-value?
      "0"

      (< token-units usd-cent-value)
      (str "<" (number/remove-trailing-zeroes (.toFixed usd-cent-value standardized-decimals-count)))

      :else
      (number/remove-trailing-zeroes (.toFixed token-units standardized-decimals-count)))))

(defn get-market-value
  [currency {:keys [market-values-per-currency]}]
  (or (get-in market-values-per-currency
              [currency :price])
      (get-in market-values-per-currency
              [constants/profile-default-currency :price])
      ;; NOTE: adding fallback value (zero) in case prices are
      ;; unavailable and to prevent crash on calculating fiat value
      0))

(defn- filter-chains
  [balances-per-chain chain-ids]
  (if chain-ids
    (select-keys balances-per-chain chain-ids)
    balances-per-chain))

(defn calculate-total-token-balance
  ([token]
   (calculate-total-token-balance token nil))
  ([{:keys [balances-per-chain decimals]} chain-ids]
   (-> balances-per-chain
       (filter-chains chain-ids)
       (total-raw-balance-in-all-chains)
       (money/token->unit decimals))))

(defn get-account-by-address
  [accounts address]
  (some #(when (= (:address %) address) %) accounts))

(defn calculate-token-fiat-value
  "Returns the token fiat value for provided raw balance"
  [{:keys [currency balance token]}]
  (let [price (get-market-value currency token)]
    (money/crypto->fiat balance price)))

(defn calculate-balance-from-tokens
  [{:keys [currency tokens chain-ids]}]
  (->> tokens
       (map #(calculate-token-fiat-value
              {:currency currency
               :balance  (calculate-total-token-balance % chain-ids)
               :token    %}))
       (reduce money/add)))

(defn- add-balances-per-chain
  [b1 b2]
  {:raw-balance (money/add (:raw-balance b1) (:raw-balance b2))
   :chain-id    (:chain-id b2)})

(defn- merge-token
  [existing-token token]
  (assoc token
         :balances-per-chain
         (merge-with add-balances-per-chain
                     (:balances-per-chain existing-token)
                     (:balances-per-chain token))))

(defn aggregate-tokens-for-all-accounts
  "Receives accounts (seq) and returns aggregated tokens in all accounts
   NOTE: We use double reduce for faster performance (faster than mapcat and flatten)"
  [accounts]
  (->> accounts
       (map :tokens)
       (reduce
        (fn [result-map tokens-per-account]
          (reduce
           (fn [acc token]
             (update acc (:symbol token) merge-token token))
           result-map
           tokens-per-account))
        {})
       vals))

(defn get-wallet-qr
  [{:keys [wallet-type selected-networks address]}]
  (if (= wallet-type :multichain)
    (as-> selected-networks $
      (map qr-codes/get-network-short-name-url $)
      (apply str $)
      (str $ address))
    address))

(defn get-standard-fiat-format
  [crypto-value currency-symbol fiat-value]
  (if (string/includes? crypto-value "<")
    (str "<" currency-symbol "0.01")
    (prettify-balance currency-symbol fiat-value)))

(defn prettify-percentage-change
  "Returns unsigned precentage"
  [percentage]
  (-> (if (number? percentage) percentage 0)
      money/bignumber
      money/absolute-value
      (money/to-fixed 2)))

(defn calculate-token-value
  "This function returns token values in the props of token-value (quo) component"
  [{:keys [token color currency currency-symbol]}]
  (let [balance                           (calculate-total-token-balance token)
        fiat-unformatted-value            (calculate-token-fiat-value
                                           {:currency currency
                                            :balance  balance
                                            :token    token})
        market-values                     (or (get-in token [:market-values-per-currency currency])
                                              (get-in token
                                                      [:market-values-per-currency
                                                       constants/profile-default-currency]))
        {:keys [price change-pct-24hour]} market-values
        formatted-token-price             (prettify-balance currency-symbol price)
        percentage-change                 (prettify-percentage-change change-pct-24hour)
        crypto-value                      (get-standard-crypto-format token balance)
        fiat-value                        (get-standard-fiat-format crypto-value
                                                                    currency-symbol
                                                                    fiat-unformatted-value)]
    {:token               (:symbol token)
     :token-name          (:name token)
     :state               :default
     :metrics?            true
     :status              (cond
                            (pos? change-pct-24hour) :positive
                            (neg? change-pct-24hour) :negative
                            :else                    :empty)
     :customization-color color
     :values              {:crypto-value           crypto-value
                           :fiat-value             fiat-value
                           :fiat-unformatted-value fiat-unformatted-value
                           :fiat-change            formatted-token-price
                           :percentage-change      percentage-change}}))

(defn get-multichain-address
  [networks address]
  (str (->> networks
            (map #(str (:short-name %) ":"))
            (clojure.string/join ""))
       address))

(defn split-prefix-and-address
  [input-string]
  (let [split-result (string/split input-string #"0x")]
    [(first split-result) (str "0x" (second split-result))]))

(defn make-network-item
  "This function generates props for quo/category component item"
  [{:keys [network-name color on-change networks state label-props type blur?]}]
  (cond-> {:title        (string/capitalize (name network-name))
           :image        :icon-avatar
           :image-props  {:icon (resources/get-network network-name)
                          :size :size-20}
           :action       :selector
           :action-props {:type                (or type
                                                   (if (= :default state)
                                                     :filled-checkbox
                                                     :checkbox))
                          :blur?               blur?
                          :customization-color color
                          :checked?            (contains? networks network-name)
                          :on-change           on-change}}

    label-props
    (assoc :label       :text
           :label-props label-props)))

(defn filter-tokens-in-chains
  [tokens chain-ids]
  (map #(update % :balances-per-chain select-keys chain-ids) tokens))

(defn calculate-balances-per-chain
  [{:keys [tokens currency currency-symbol]}]
  (->
    (reduce (fn [acc {:keys [balances-per-chain decimals] :as token}]
              (let [currency-value         (get-market-value currency token)
                    fiat-balance-per-chain (update-vals balances-per-chain
                                                        #(-> (money/token->unit (:raw-balance %)
                                                                                decimals)
                                                             (money/crypto->fiat currency-value)))]
                (merge-with money/add acc fiat-balance-per-chain)))
            {}
            tokens)
    (update-vals #(prettify-balance currency-symbol %))))

(defn format-token-id
  [token collectible]
  (if token
    (:symbol token)
    (str (get-in collectible [:id :contract-id :address])
         ":"
         (get-in collectible [:id :token-id]))))

(defn get-token-from-account
  [db token-symbol address]
  (let [address-tokens (-> db :wallet :accounts (get address) :tokens)]
    (some #(when (= token-symbol (:symbol %))
             %)
          address-tokens)))

(defn get-shortened-address
  "Takes first and last 4 digits from address including leading 0x
  and adds unicode ellipsis in between"
  [address]
  (when address
    (let [counter (count address)]
      (str (subs address 0 6) "\u2026" (subs address (- counter 3) counter)))))

(defn get-account-name-error
  [s existing-account-names]
  (cond
    (utils.string/contains-emoji? s)             :emoji
    (existing-account-names s)                   :existing-name
    (utils.string/contains-special-character? s) :special-character))

(defn calculate-and-sort-tokens
  [{:keys [tokens color currency currency-symbol]}]
  (let [calculate-token   (fn [token]
                            (calculate-token-value {:token           token
                                                    :color           color
                                                    :currency        currency
                                                    :currency-symbol currency-symbol}))
        calculated-tokens (map calculate-token tokens)]
    (sort-by (fn [token]
               (let [fiat-value (get-in token [:values :fiat-unformatted-value])
                     priority   (get constants/token-sort-priority (:token token) ##Inf)]
                 [(- fiat-value) priority]))
             calculated-tokens)))

(defn sort-tokens
  [tokens]
  (let [priority #(get constants/token-sort-priority (:symbol %) ##Inf)]
    (sort-by (juxt (comp - :balance) priority) tokens)))

(defn- transaction-data
  [{:keys [from-address to-address token-address route data eth-transfer?]}]
  (let [{:keys [amount-in gas-amount gas-fees]} route
        eip-1559-enabled?                       (:eip-1559-enabled gas-fees)
        {:keys [gas-price max-fee-per-gas-medium
                max-priority-fee-per-gas]}      gas-fees]
    (cond-> {:From  from-address
             :To    (or token-address to-address)
             :Gas   (money/to-hex gas-amount)
             :Value (when eth-transfer? amount-in)
             :Nonce nil
             :Input ""
             :Data  (or data "0x")}
      eip-1559-enabled?       (assoc
                               :TxType "0x02"
                               :MaxFeePerGas
                               (money/to-hex
                                (money/->wei
                                 :gwei
                                 max-fee-per-gas-medium))
                               :MaxPriorityFeePerGas
                               (money/to-hex
                                (money/->wei
                                 :gwei
                                 max-priority-fee-per-gas)))
      (not eip-1559-enabled?) (assoc :TxType "0x00"
                                     :GasPrice
                                     (money/to-hex
                                      (money/->wei
                                       :gwei
                                       gas-price))))))

(defn approval-path
  [{:keys [route from-address to-address token-address]}]
  (let [{:keys [from]}                     route
        from-chain-id                      (:chain-id from)
        approval-amount-required           (:approval-amount-required route)
        approval-amount-required-sanitized (-> approval-amount-required
                                               (utils.hex/normalize-hex)
                                               (native-module/hex-to-number))
        approval-contract-address          (:approval-contract-address route)
        data                               (native-module/encode-function-call
                                            constants/contract-function-signature-erc20-approve
                                            [approval-contract-address
                                             approval-amount-required-sanitized])
        tx-data                            (transaction-data {:from-address  from-address
                                                              :to-address    to-address
                                                              :token-address token-address
                                                              :route         route
                                                              :data          data
                                                              :eth-transfer? false})]
    {:BridgeName constants/bridge-name-transfer
     :ChainID    from-chain-id
     :TransferTx tx-data}))

(defn transaction-path
  [{:keys [from-address to-address token-id-from token-address token-id-to route data
           slippage-percentage eth-transfer?]}]
  (let [{:keys [bridge-name amount-in bonder-fees from
                to]}  route
        tx-data       (transaction-data {:from-address  from-address
                                         :to-address    to-address
                                         :token-address token-address
                                         :route         route
                                         :data          data
                                         :eth-transfer? eth-transfer?})
        to-chain-id   (:chain-id to)
        from-chain-id (:chain-id from)]
    (cond-> {:BridgeName bridge-name
             :ChainID    from-chain-id}

      (= bridge-name constants/bridge-name-erc-721-transfer)
      (assoc :ERC721TransferTx
             (assoc tx-data
                    :Recipient to-address
                    :TokenID   token-id-from
                    :ChainID   to-chain-id))

      (= bridge-name constants/bridge-name-erc-1155-transfer)
      (assoc :ERC1155TransferTx
             (assoc tx-data
                    :Recipient to-address
                    :TokenID   token-id-from
                    :ChainID   to-chain-id
                    :Amount    amount-in))

      (= bridge-name constants/bridge-name-transfer)
      (assoc :TransferTx tx-data)

      (= bridge-name constants/bridge-name-hop)
      (assoc :HopTx
             (assoc tx-data
                    :ChainID   from-chain-id
                    :ChainIDTo to-chain-id
                    :Symbol    token-id-from
                    :Recipient to-address
                    :Amount    amount-in
                    :BonderFee bonder-fees))

      (= bridge-name constants/bridge-name-paraswap)
      (assoc :SwapTx
             (assoc tx-data
                    :ChainID            from-chain-id
                    :ChainIDTo          to-chain-id
                    :TokenIDFrom        token-id-from
                    :TokenIDTo          token-id-to
                    :SlippagePercentage slippage-percentage))

      (not (or (= bridge-name constants/bridge-name-erc-721-transfer)
               (= bridge-name constants/bridge-name-transfer)
               (= bridge-name constants/bridge-name-hop)))
      (assoc :CbridgeTx
             (assoc tx-data
                    :ChainID   to-chain-id
                    :Symbol    token-id-from
                    :Recipient to-address
                    :Amount    amount-in)))))

(defn multi-transaction-command
  [{:keys [from-address to-address from-asset to-asset amount-out multi-transaction-type]
    :or   {multi-transaction-type constants/multi-transaction-type-unknown}}]
  {:fromAddress from-address
   :toAddress   to-address
   :fromAsset   from-asset
   :toAsset     to-asset
   :fromAmount  amount-out
   :type        multi-transaction-type})

(defn sort-tokens-by-name
  [tokens]
  (let [priority #(get constants/token-sort-priority (:symbol %) ##Inf)]
    (sort-by (juxt :symbol priority) tokens)))

(defn tokens-with-balance
  [tokens networks chain-ids]
  (map (fn [token]
         (assoc token
                :networks          (network-utils/network-list token networks)
                :available-balance (calculate-total-token-balance token)
                :total-balance     (calculate-total-token-balance token chain-ids)))
       tokens))
