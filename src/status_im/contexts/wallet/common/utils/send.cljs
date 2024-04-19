(ns status-im.contexts.wallet.common.utils.send
  (:require [clojure.string :as string]
            [utils.money :as money]))

(defn calculate-gas-fee
  [data]
  (let [gas-amount         (money/bignumber (get data :gas-amount))
        gas-fees           (get data :gas-fees)
        eip1559-enabled?   (get gas-fees :eip-1559-enabled)
        optimal-price-gwei (money/bignumber (if eip1559-enabled?
                                              (get gas-fees :max-fee-per-gas-medium)
                                              (get gas-fees :gas-price)))
        total-gas-fee-wei  (money/mul (money/->wei :gwei optimal-price-gwei) gas-amount)
        l1-fee-wei         (money/->wei :gwei (get gas-fees :l-1-gas-fee))]
    (money/add total-gas-fee-wei l1-fee-wei)))

(defn calculate-full-route-gas-fee
  "Sums all the routes fees in wei and then convert the total value to ether"
  [route]
  (money/wei->ether (reduce money/add (map calculate-gas-fee route))))

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
