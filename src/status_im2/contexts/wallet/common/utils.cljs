(ns status-im2.contexts.wallet.common.utils
  (:require
    [clojure.string :as string]))

(defn get-first-name
  [full-name]
  (first (string/split full-name #" ")))

(defn get-balance-by-address
  [balances address]
  (->> balances
       (filter #(= (:address %) address))
       first
       :balance))

(defn get-account-by-address
  [accounts address]
  (some #(when (= (:address %) address) %) accounts))

(defn prettify-balance
  [balance]
  (str "$" (.toFixed balance 2)))
