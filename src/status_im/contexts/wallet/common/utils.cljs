(ns status-im.contexts.wallet.common.utils
  (:require [clojure.string :as string]
            [quo.foundations.resources :as resources]
            [status-im.common.qr-codes.view :as qr-codes]
            [status-im.constants :as constants]
            [utils.money :as money]
            [utils.number]))

(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))

(defn prettify-balance
  [currency-symbol balance]
  (let [valid-balance? (and balance
                            (or (number? balance) (.-toFixed balance)))]
    (as-> balance $
      (if valid-balance? $ 0)
      (.toFixed $ 2)
      (str currency-symbol $))))

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
  [value]
  (let [str-representation   (str value)
        decimal-part         (second (clojure.string/split str-representation #"\."))
        exponent             (extract-exponent str-representation)
        zeroes-count         (count (take-while #(= \0 %) decimal-part))
        max-decimals         (or exponent zeroes-count)
        first-non-zero-digit (first (filter #(not (= \0 %)) decimal-part))]
    (if (= \1 first-non-zero-digit)
      (inc max-decimals)
      max-decimals)))

(defn remove-trailing-zeroes
  [num]
  (let [parts (clojure.string/split (str num) #"\.")]
    (str (first parts)
         (if-let [decimals (second parts)]
           (if (seq (clojure.string/replace decimals #"0+$" ""))
             (str "." (clojure.string/replace decimals #"0+$" ""))
             "")
           ""))))

(defn get-crypto-decimals-count
  [{:keys [market-values-per-currency]}]
  (let [price          (get-in market-values-per-currency [:usd :price])
        one-cent-value (if (pos? price) (/ 0.01 price) 0)]
    (calc-max-crypto-decimals one-cent-value)))

(defn get-standard-crypto-format
  "For full details: https://github.com/status-im/status-mobile/issues/18225"
  [{:keys [market-values-per-currency]} token-units]
  (if (or (nil? token-units)
          (nil? market-values-per-currency)
          (money/equal-to token-units 0))
    "0"
    (let [price          (-> market-values-per-currency :usd :price)
          one-cent-value (if (pos? price) (/ 0.01 price) 0)
          decimals-count (calc-max-crypto-decimals one-cent-value)]
      (if (< token-units one-cent-value)
        (str "<" (remove-trailing-zeroes (.toFixed one-cent-value decimals-count)))
        (remove-trailing-zeroes (.toFixed token-units decimals-count))))))

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

(defn network-list
  [{:keys [balances-per-chain]} networks]
  (into #{}
        (mapv (fn [chain-id]
                (first (filter #(or (= (:chain-id %) chain-id)
                                    (= (:related-chain-id %) chain-id))
                               networks)))
              (keys balances-per-chain))))

(defn get-wallet-qr
  [{:keys [wallet-type selected-networks address]}]
  (if (= wallet-type :multichain)
    (as-> selected-networks $
      (map qr-codes/get-network-short-name-url $)
      (apply str $)
      (str $ address))
    address))

(def id->network
  {constants/ethereum-mainnet-chain-id constants/mainnet-network-name
   constants/ethereum-goerli-chain-id  constants/mainnet-network-name
   constants/ethereum-sepolia-chain-id constants/mainnet-network-name
   constants/optimism-mainnet-chain-id constants/optimism-network-name
   constants/optimism-goerli-chain-id  constants/optimism-network-name
   constants/optimism-sepolia-chain-id constants/optimism-network-name
   constants/arbitrum-mainnet-chain-id constants/arbitrum-network-name
   constants/arbitrum-goerli-chain-id  constants/arbitrum-network-name
   constants/arbitrum-sepolia-chain-id constants/arbitrum-network-name})

(def network->short-name
  {constants/mainnet-network-name  constants/mainnet-short-name
   constants/optimism-network-name constants/optimism-short-name
   constants/arbitrum-network-name constants/arbitrum-short-name})

(def short-name->network
  {constants/mainnet-short-name  constants/mainnet-network-name
   constants/optimism-short-name constants/optimism-network-name
   constants/arbitrum-short-name constants/arbitrum-network-name})

(defn short-names->network-preference-prefix
  [short-names]
  (str (string/join ":" short-names) ":"))

(defn network-preference-prefix->network-names
  [prefix]
  (as-> prefix $
    (string/split $ ":")
    (map short-name->network $)))

(defn- get-chain-id
  [{:keys [mainnet-chain-id sepolia-chain-id goerli-chain-id testnet-enabled? goerli-enabled?]}]
  (cond
    (and testnet-enabled? goerli-enabled?)
    goerli-chain-id

    testnet-enabled?
    sepolia-chain-id

    :else
    mainnet-chain-id))

(defn network->chain-id
  ([db network]
   (let [{:keys [test-networks-enabled? is-goerli-enabled?]} (:profile/profile db)]
     (network->chain-id {:network          network
                         :testnet-enabled? test-networks-enabled?
                         :goerli-enabled?  is-goerli-enabled?})))
  ([{:keys [network testnet-enabled? goerli-enabled?]}]
   (condp contains? (keyword network)
     #{constants/mainnet-network-name (keyword constants/mainnet-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/ethereum-mainnet-chain-id
       :sepolia-chain-id constants/ethereum-sepolia-chain-id
       :goerli-chain-id  constants/ethereum-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?})

     #{constants/optimism-network-name (keyword constants/optimism-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/optimism-mainnet-chain-id
       :sepolia-chain-id constants/optimism-sepolia-chain-id
       :goerli-chain-id  constants/optimism-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?})

     #{constants/arbitrum-network-name (keyword constants/arbitrum-short-name)}
     (get-chain-id
      {:mainnet-chain-id constants/arbitrum-mainnet-chain-id
       :sepolia-chain-id constants/arbitrum-sepolia-chain-id
       :goerli-chain-id  constants/arbitrum-goerli-chain-id
       :testnet-enabled? testnet-enabled?
       :goerli-enabled?  goerli-enabled?}))))

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
        fiat-value                        (calculate-token-fiat-value
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
                                                                    fiat-value)]
    {:token               (:symbol token)
     :token-name          (:name token)
     :state               :default
     :metrics?            true
     :status              (cond
                            (pos? change-pct-24hour) :positive
                            (neg? change-pct-24hour) :negative
                            :else                    :empty)
     :customization-color color
     :values              {:crypto-value      crypto-value
                           :fiat-value        fiat-value
                           :fiat-change       formatted-token-price
                           :percentage-change percentage-change}}))

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
  [{:keys [network-name color on-change networks state label-props]}]
  (cond-> {:title        (string/capitalize (name network-name))
           :image        :icon-avatar
           :image-props  {:icon (resources/get-network network-name)
                          :size :size-20}
           :action       :selector
           :action-props {:type                (if (= :default state)
                                                 :filled-checkbox
                                                 :checkbox)
                          :customization-color color
                          :checked?            (some #(= network-name %) networks)
                          :on-change           on-change}}

    label-props
    (assoc :label       :text
           :label-props label-props)))

(defn get-default-chain-ids-by-mode
  [{:keys [test-networks-enabled? is-goerli-enabled?]}]
  (cond
    (and test-networks-enabled? is-goerli-enabled?)
    constants/goerli-chain-ids

    test-networks-enabled?
    constants/sepolia-chain-ids

    :else
    constants/mainnet-chain-ids))

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
