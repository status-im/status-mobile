(ns status-im2.contexts.wallet.common.utils
  (:require [clojure.string :as string]
            [status-im2.constants :as constants]
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
