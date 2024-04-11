(ns status-im.contexts.wallet.common.utils.send
  (:require [clojure.string :as string]
            [utils.money :as money]))

(defn calculate-gas-fee
  [data]
  (let [gas-amount       (money/bignumber (get data :gas-amount))
        gas-fees         (get data :gas-fees)
        eip1559-enabled? (get gas-fees :eip1559-enabled)
        billion          (money/bignumber "1000000000")]
    (if eip1559-enabled?
      (let [base-fee      (money/bignumber (get gas-fees :base-fee))
            priority-fee  (money/bignumber (get gas-fees :max-priority-fee-per-gas))
            fee-with-tip  (money/bignumber (money/add base-fee priority-fee))
            total-gas-fee (money/mul gas-amount fee-with-tip)]
        (money/with-precision (money/div total-gas-fee billion) 10))
      (let [gas-price     (money/bignumber (get gas-fees :gas-price))
            total-gas-fee (money/mul gas-amount gas-price)]
        (money/with-precision (money/div total-gas-fee billion) 10)))))

(defn calculate-full-route-gas-fee
  [route]
  (reduce money/add (map calculate-gas-fee route)))

(defn find-affordable-networks
  [{:keys [balances-per-chain input-value selected-networks disabled-chain-ids]}]
  (let [input-value (if (string/blank? input-value) 0 input-value)]
    (->> balances-per-chain
         (filter (fn [[_
                       {:keys [balance chain-id]
                        :or   {balance 0}}]]
                   (and
                    (money/greater-than-or-equals (money/bignumber balance)
                                                  (money/bignumber input-value))
                    (some #(= % chain-id) selected-networks)
                    (not-any? #(= % chain-id) disabled-chain-ids))))
         (map first))))
