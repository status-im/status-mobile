(ns status-im.contexts.wallet.trading.swap.utils
  (:require [clojure.string :as string]
            [utils.money :as money]))

(defn default-account
  [db]
  (let [accounts (-> db :wallet :accounts vals)]
    (->> accounts
         (filter :operable?)
         first)))

(defn- testnet?
  [db]
  (-> db :profile/profile :test-networks-enabled?))

(defn token-from-symbol
  ([db token-symbol]
   (token-from-symbol db token-symbol 1))
  ([db token-symbol chain-id]
   (get-in db
           [:wallet :tokens :normalized-tokens
            token-symbol chain-id])))

(defn router-disabled-chain-ids
  [db chain-id]
  (let [testnet-key (if (testnet? db) :test :prod)]
    (->> (get-in db [:wallet :networks testnet-key])
         (map :chain-id)
         (filter #(not= % chain-id)))))

(defn input-empty?
  [amount]
  (string/blank? amount))

(defn pay-input-error?
  [pay-amount pay-balance]
  (and (not (input-empty? pay-amount))
       (money/greater-than
        (money/bignumber pay-amount)
        (money/bignumber pay-balance))))

(defn pay-input-valid?
  [pay-amount pay-balance]
  (and (not (input-empty? pay-amount))
       (money/greater-than (money/bignumber pay-amount)
                           (money/bignumber 0))
       (not (pay-input-error? pay-amount pay-balance))))

(defn approval-for-inputs
  "Finds the relevant approval based on the current inputs"
  [approvals {:keys [account-address chain-id pay-token-symbol pay-amount]}]
  (some (fn [approval]
          (let [same-address?              (= account-address (:account-address approval))
                same-chain?                (= chain-id (:chain-id approval))
                same-pay-token?            (= pay-token-symbol (:pay-token-symbol approval))
                equal-to-approved-amount?  (money/equal-to pay-amount (:pay-amount approval))
                less-than-approved-amount? (money/less-than pay-amount (:pay-amount approval))]
            (when (and same-address?
                       same-chain?
                       same-pay-token?
                       (or equal-to-approved-amount? less-than-approved-amount?))
              approval)))
        approvals))
