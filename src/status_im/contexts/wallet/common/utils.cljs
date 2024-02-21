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

(defn total-token-units-in-all-chains
  [{:keys [balances-per-chain decimals] :as _token}]
  (-> balances-per-chain
      (total-raw-balance-in-all-chains)
      (money/token->unit decimals)))

(defn get-account-by-address
  [accounts address]
  (some #(when (= (:address %) address) %) accounts))

(defn total-token-fiat-value
  "Returns the total token fiat value taking into account all token's chains."
  [currency {:keys [market-values-per-currency] :as token}]
  (let [price                     (or (get-in market-values-per-currency
                                              [currency :price])
                                      (get-in market-values-per-currency
                                              [constants/profile-default-currency :price])
                                      ;; NOTE: adding fallback value (zero) in case prices are
                                      ;; unavailable and to prevent crash on calculating fiat value
                                      0)
        total-units-in-all-chains (total-token-units-in-all-chains token)]
    (money/crypto->fiat total-units-in-all-chains price)))

(defn token-fiat-value
  "Returns the fiat value for a single token on a given network."
  [currency raw-balance {:keys [market-values-per-currency]}]
  (let [price (or (get-in market-values-per-currency
                          [currency :price])
                  (get-in market-values-per-currency
                          [constants/profile-default-currency :price])
                  0)]
    (money/crypto->fiat raw-balance price)))

(defn calculate-balance-for-account
  [currency {:keys [tokens] :as _account}]
  (->> tokens
       (map #(total-token-fiat-value currency %))
       (reduce money/add)))

(defn calculate-balance-for-token
  [token]
  (money/bignumber
   (money/mul (total-token-units-in-all-chains token)
              (-> token :market-values-per-currency :usd :price))))

(defn calculate-balance
  [tokens-in-account]
  (->> tokens-in-account
       (map #(calculate-balance-for-token %))
       (reduce +)))

(defn calculate-balance-from-tokens
  [{:keys [currency tokens]}]
  (->> tokens
       (map #(total-token-fiat-value currency %))
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
  {constants/ethereum-chain-id         :ethereum
   constants/goerli-chain-id           :ethereum
   constants/ethereum-sepolia-chain-id :ethereum
   constants/optimism-chain-id         :optimism
   constants/optimism-testnet-chain-id :optimism
   constants/optimism-sepolia-chain-id :optimism
   constants/arbitrum-chain-id         :arbitrum
   constants/arbitrum-testnet-chain-id :arbitrum
   constants/arbitrum-sepolia-chain-id :arbitrum})

(defn- get-chain-id
  [test-net?]
  (if test-net?
    {:eth  constants/goerli-chain-id
     :opt  constants/optimism-testnet-chain-id
     :arb1 constants/arbitrum-testnet-chain-id}
    {:eth  constants/ethereum-chain-id
     :opt  constants/optimism-chain-id
     :arb1 constants/arbitrum-chain-id}))

(defn short-name->id
  [short-name test-net?]
  (let [chain-id-map (get-chain-id test-net?)]
    (get chain-id-map short-name)))

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
  (let [token-units                       (total-token-units-in-all-chains token)
        fiat-value                        (total-token-fiat-value currency token)
        market-values                     (or (get-in token [:market-values-per-currency currency])
                                              (get-in token
                                                      [:market-values-per-currency
                                                       constants/profile-default-currency]))
        {:keys [price change-pct-24hour]} market-values
        formatted-token-price             (prettify-balance currency-symbol price)
        percentage-change                 (prettify-percentage-change change-pct-24hour)
        crypto-value                      (get-standard-crypto-format token token-units)
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

(defn get-balance-for-chain
  [data chain-id]
  (some #(when (= chain-id (:chain-id %)) %) (vals data)))
<<<<<<< HEAD

<<<<<<< HEAD
<<<<<<< HEAD
(defn make-network-item
  "This function generates props for quo/category component item"
  [{:keys [network-name] :as _network}
   {:keys [title color on-change networks state label-props] :as _options}]
  (cond-> {:title        (or title (string/capitalize (name network-name)))
           :image        :icon-avatar
           :image-props  {:icon (resources/get-network network-name)
                          :size :size-20}
           :action       :selector
           :action-props {:type                (if (= :default state)
                                                 :filled-checkbox
                                                 :checkbox)
                          :customization-color color
                          :checked?            (contains? networks network-name)
                          :on-change           on-change}}

    label-props
    (assoc :label       :text
           :label-props label-props)))
=======
(defn string-to-vector [str]
=======
(defn string-to-vector
  [str]
>>>>>>> 86465d80f (lint)
  (->> (clojure.string/split str #"\s")
       (vec)))
>>>>>>> 2d6aa482f (feat: new keypair)
=======
>>>>>>> 8db6aedb6 (review)
