(ns status-im.subs.wallet.dapps.transactions
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.common.utils :as wallet-utils]
            [status-im.contexts.wallet.wallet-connect.utils.transactions :as transactions]
            [utils.money :as money]
            [utils.string]))

(rf/reg-sub
 :wallet-connect/transaction-args
 :<- [:wallet-connect/current-request]
 (fn [{:keys [event transaction]}]
   (when (transactions/transaction-request? event)
     transaction)))

(rf/reg-sub
 :wallet-connect/transaction-suggested-fees
 :<- [:wallet-connect/current-request]
 (fn [{:keys [event raw-data]}]
   (when (transactions/transaction-request? event)
     (:suggested-fees raw-data))))

(rf/reg-sub
 :wallet-connect/transaction-max-fees-wei
 :<- [:wallet-connect/transaction-args]
 :<- [:wallet-connect/transaction-suggested-fees]
 (fn [[transaction suggested-fees]]
   (when transaction
     (let [{:keys [gasPrice gas gasLimit maxFeePerGas]} transaction
           eip-1559-chain?                              (:eip1559Enabled suggested-fees)
           gas-limit                                    (or gasLimit gas)
           max-gas-fee                                  (if eip-1559-chain? maxFeePerGas gasPrice)]
       (money/bignumber (* max-gas-fee gas-limit))))))

(rf/reg-sub
 :wallet-connect/account-eth-token
 :<- [:wallet-connect/current-request-address]
 :<- [:wallet/accounts]
 (fn [[address accounts]]
   (let [fee-token    "ETH"
         find-account #(when (= (:address %) address) %)
         find-token   #(when (= (:symbol %) fee-token) %)]
     (->> accounts
          (some find-account)
          :tokens
          (some find-token)))))

(rf/reg-sub
 :wallet-connect/current-request-transaction-information
 :<- [:wallet-connect/chain-id]
 :<- [:wallet-connect/transaction-max-fees-wei]
 :<- [:wallet-connect/transaction-args]
 :<- [:wallet-connect/account-eth-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[chain-id max-fees-wei transaction eth-token currency currency-symbol]]
   (when transaction
     (let [max-fees-ether           (money/wei->ether max-fees-wei)
           max-fees-fiat            (wallet-utils/calculate-token-fiat-value {:currency currency
                                                                              :balance  max-fees-ether
                                                                              :token    eth-token})
           max-fees-fiat-formatted  (-> (wallet-utils/get-standard-crypto-format eth-token
                                                                                 max-fees-ether)
                                        (wallet-utils/get-standard-fiat-format currency-symbol
                                                                               max-fees-fiat))
           balance                  (-> eth-token
                                        (get-in [:balances-per-chain chain-id :raw-balance])
                                        money/bignumber)
           tx-value                 (money/bignumber (:value transaction))
           total-transaction-value  (money/add max-fees-wei tx-value)
           estimated-time-formatted (wallet-utils/estimated-time-format (:estimated-time transaction))]
       {:total-transaction-value total-transaction-value
        :balance                 balance
        :estimated-time          estimated-time-formatted
        :max-fees                max-fees-wei
        :max-fees-fiat-value     max-fees-fiat
        :max-fees-fiat-formatted max-fees-fiat-formatted
        :error-state             (cond
                                   (not (money/sufficient-funds? tx-value balance))
                                   :not-enough-assets

                                   (not (money/sufficient-funds? total-transaction-value
                                                                 balance))
                                   :not-enough-assets-to-pay-gas-fees)}))))
