(ns status-im.contexts.wallet.common.utils
  (:require [clojure.string :as string]
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

(defn- total-raw-balance-in-all-chains
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
  (let [str-representation (str value)
        decimal-part       (second (clojure.string/split str-representation #"\."))
        exponent           (extract-exponent str-representation)
        zeroes-count       (count (take-while #(= \0 %) decimal-part))
        max-decimals       (or exponent zeroes-count)]
    (let [first-non-zero-digit (first (filter #(not (= \0 %)) decimal-part))]
      (if (= \1 first-non-zero-digit)
        (inc max-decimals)
        max-decimals))))

(defn get-standard-crypto-format
  "For full details: https://github.com/status-im/status-mobile/issues/18225"
  [{:keys [market-values-per-currency]} token-units]
  (let [price          (get-in market-values-per-currency [:usd :price])
        one-cent-value (/ 0.01 price)
        count          (calc-max-crypto-decimals one-cent-value)]
    (.toFixed token-units count)))

(defn total-token-units-in-all-chains
  [{:keys [balances-per-chain decimals] :as _token}]
  (-> balances-per-chain
      (total-raw-balance-in-all-chains)
      (money/token->unit decimals)))

(defn get-account-by-address
  [accounts address]
  (some #(when (= (:address %) address) %) accounts))

(defn calculate-raw-balance
  [raw-balance decimals]
  (if-let [n (utils.number/parse-int raw-balance nil)]
    (/ n (Math/pow 10 (utils.number/parse-int decimals)))
    0))

(defn token-value-in-chain
  [{:keys [balances-per-chain decimals]} chain-id]
  (let [balance-in-chain (get balances-per-chain chain-id)]
    (when balance-in-chain
      (calculate-raw-balance (:raw-balance balance-in-chain) decimals))))

(defn total-token-fiat-value
  "Returns the total token fiat value taking into account all token's chains."
  [currency {:keys [market-values-per-currency] :as token}]
  (let [price                     (get-in market-values-per-currency
                                          [currency :price]
                                          (get-in market-values-per-currency
                                                  [constants/profile-default-currency :price]))
        total-units-in-all-chains (total-token-units-in-all-chains token)]
    (money/crypto->fiat total-units-in-all-chains price)))

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

(defn network-list
  [{:keys [balances-per-chain]} networks]
  (into #{}
        (mapv (fn [chain-id]
                (first (filter #(or (= (:chain-id %) chain-id)
                                    (= (:related-chain-id %) chain-id))
                               networks)))
              (keys balances-per-chain))))

(defn calculate-fiat-change
  [fiat-value change-pct-24hour]
  (money/bignumber (* fiat-value (/ change-pct-24hour (+ 100 change-pct-24hour)))))

(defn get-wallet-qr
  [{:keys [wallet-type selected-networks address]}]
  (if (= wallet-type :wallet-multichain)
    (as-> selected-networks $
      (map qr-codes/get-network-short-name-url $)
      (apply str $)
      (str $ address))
    address))

(def id->network
  {constants/mainnet-chain-id  :ethereum
   constants/optimism-chain-id :optimism
   constants/arbitrum-chain-id :arbitrum})
