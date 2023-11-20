(ns status-im2.contexts.wallet.common.utils
  (:require [clojure.string :as string]
            [status-im2.constants :as constants]
            [utils.money :as money]
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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
(defn total-token-value-in-all-chains
=======
=======
>>>>>>> e41fe5426 (review)
=======
>>>>>>> af0e5cc43 (review)
<<<<<<< HEAD
=======
>>>>>>> 6acd5d275 (rebase)
(defn- total-token-value-in-all-chains
>>>>>>> 70e4a2a12 (updates)
  [{:keys [balances-per-chain decimals]}]
  (->> balances-per-chain
       (vals)
       (map #(calculate-raw-balance (:raw-balance %) decimals))
       (reduce +)))

(defn- token-value-in-chain
  [{:keys [balances-per-chain decimals]} chain-id]
  (let [balance-in-chain (get balances-per-chain chain-id)]
    (calculate-raw-balance (:raw-balance balance-in-chain) decimals)))

(defn calculate-balance
  [tokens-in-account]
  (->> tokens-in-account
       (map (fn [token]
              (* (total-token-value-in-all-chains token)
                 (-> token :market-values-per-currency :usd :price))))
       (reduce +)))
<<<<<<< HEAD
<<<<<<< HEAD

(defn network-list
  [{:keys [balances-per-chain]} networks]
  (into #{}
        (mapv (fn [chain-id]
                (first (filter #(or (= (:chain-id %) chain-id)
                                    (= (:related-chain-id %) chain-id))
                               networks)))
              (keys balances-per-chain))))
=======
=======
(defn total-per-token
=======
=======
=======

>>>>>>> 6acd5d275 (rebase)
(defn calculate-fiat-change
  [fiat-value change-pct-24hour]
<<<<<<< HEAD
  (* fiat-value (/ change-pct-24hour (+ 100 change-pct-24hour))))

<<<<<<< HEAD
>>>>>>> aeda1e4a7 (review)
(defn sum-token-chains
>>>>>>> 25ec47428 (review)
  [item]
  (reduce (fn [acc balances]
            (+ (calculate-raw-balance (:rawBalance balances)
                                      (:decimals item))
               acc))
          0
          (vals (:balancesPerChain item))))
>>>>>>> c8bb0a581 (updates)
>>>>>>> 70e4a2a12 (updates)
=======
(defn get-balance-by-address
  [balances address]
  (->> balances
       (filter #(= (:address %) address))
       first
       :balance))

>>>>>>> 6acd5d275 (rebase)
=======
  (money/bignumber (* fiat-value (/ change-pct-24hour (+ 100 change-pct-24hour)))))
>>>>>>> f4f0deecf (review)
