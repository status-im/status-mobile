(ns status-im2.contexts.wallet.common.utils
  (:require [clojure.string :as string]
            [status-im2.constants :as constants]
            [utils.number]))

(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))

(defn prettify-balance
  [balance]
  (str "$" (.toFixed (if (number? balance) balance 0) 2)))

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

(defn- calculate-raw-balance
  [raw-balance decimals]
  (if-let [n (utils.number/parse-int raw-balance nil)]
    (/ n (Math/pow 10 (utils.number/parse-int decimals)))
    0))

(defn- total-token-value-in-all-chains
  [{:keys [balances-per-chain decimals]}]
  (->> balances-per-chain
       (vals)
       (map #(calculate-raw-balance (:raw-balance %) decimals))
       (reduce +)))

(defn calculate-balance
  [tokens-in-account]
  (->> tokens-in-account
       (map (fn [token]
              (* (total-token-value-in-all-chains token)
                 (-> token :market-values-per-currency :usd :price))))
       (reduce +)))
