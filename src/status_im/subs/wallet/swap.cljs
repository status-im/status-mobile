(ns status-im.subs.wallet.swap
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.send.utils :as send-utils]
            [utils.hex :as hex]
            [utils.money :as money]
            [utils.number :as number]))

(rf/reg-sub
 :wallet/swap
 :<- [:wallet/ui]
 :-> :swap)

(rf/reg-sub
 :wallet/swap-asset-to-pay
 :<- [:wallet/swap]
 :-> :asset-to-pay)

(rf/reg-sub
 :wallet/swap-asset-to-receive
 :<- [:wallet/swap]
 :-> :asset-to-receive)

(rf/reg-sub
 :wallet/swap-network
 :<- [:wallet/swap]
 :-> :network)

(rf/reg-sub
 :wallet/swap-error-response
 :<- [:wallet/swap]
 :-> :error-response)

(rf/reg-sub
 :wallet/swap-asset-to-pay-token-symbol
 :<- [:wallet/swap-asset-to-pay]
 :-> :symbol)

(rf/reg-sub
 :wallet/swap-asset-to-pay-networks
 :<- [:wallet/swap-asset-to-pay]
 (fn [token]
   (let [{token-networks :networks} token
         grouped-networks           (group-by :layer
                                              token-networks)
         mainnet-network            (first (get grouped-networks constants/layer-1-network))
         layer-2-networks           (get grouped-networks constants/layer-2-network)]
     {:mainnet-network  mainnet-network
      :layer-2-networks layer-2-networks})))

(rf/reg-sub
 :wallet/swap-asset-to-pay-network-balance
 :<- [:wallet/swap-asset-to-pay]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/swap-asset-to-pay-token-symbol]
 (fn [[token currency currency-symbol token-symbol] [_ chain-id]]
   (let [{:keys [balances-per-chain
                 decimals]} token
         balance-for-chain  (get balances-per-chain chain-id)
         total-balance      (money/token->unit (:raw-balance balance-for-chain) decimals)
         fiat-value         (utils/calculate-token-fiat-value
                             {:currency currency
                              :balance  total-balance
                              :token    token})
         crypto-formatted   (utils/get-standard-crypto-format token total-balance)
         fiat-formatted     (utils/get-standard-fiat-format crypto-formatted
                                                            currency-symbol
                                                            fiat-value)]
     {:crypto (str crypto-formatted " " token-symbol)
      :fiat   fiat-formatted})))

(rf/reg-sub
 :wallet/swap-max-slippage
 :<- [:wallet/swap]
 :-> :max-slippage)

(rf/reg-sub
 :wallet/swap-loading-fees?
 :<- [:wallet/swap]
 :-> :loading-fees?)

(rf/reg-sub
 :wallet/swap-approval-transaction-id
 :<- [:wallet/swap]
 :-> :approval-transaction-id)

(rf/reg-sub
 :wallet/swap-approval-transaction-status
 :<- [:wallet/transactions]
 :<- [:wallet/swap-approval-transaction-id]
 (fn [[transactions approval-transaction-id]]
   (get-in transactions [approval-transaction-id :status])))

(rf/reg-sub
 :wallet/swap-proposal
 :<- [:wallet/swap]
 :-> :swap-proposal)

(rf/reg-sub
 :wallet/swap-loading-swap-proposal?
 :<- [:wallet/swap]
 :-> :loading-swap-proposal?)

(rf/reg-sub
 :wallet/swap-proposal-amount-out
 :<- [:wallet/swap-proposal]
 :-> :amount-out)

(rf/reg-sub
 :wallet/swap-proposal-amount-in
 :<- [:wallet/swap-proposal]
 :-> :amount-in)

(rf/reg-sub
 :wallet/swap-receive-amount
 :<- [:wallet/swap-proposal-amount-out]
 :<- [:wallet/swap-asset-to-receive]
 (fn [[amount-out asset-to-receive]]
   (let [receive-token-decimals (:decimals asset-to-receive)
         receive-amount         (when amount-out
                                  (number/convert-to-whole-number
                                   (native-module/hex-to-number
                                    (hex/normalize-hex
                                     amount-out))
                                   receive-token-decimals))
         receive-amount         (when amount-out
                                  (number/remove-trailing-zeroes
                                   (.toFixed receive-amount
                                             (min receive-token-decimals
                                                  constants/min-token-decimals-to-display))))]
     (or receive-amount 0))))

(rf/reg-sub
 :wallet/swap-pay-amount
 :<- [:wallet/swap-proposal-amount-in]
 :<- [:wallet/swap-asset-to-pay]
 (fn [[amount-in asset-to-pay]]
   (let [pay-token-decimals (:decimals asset-to-pay)
         pay-amount         (when amount-in
                              (number/convert-to-whole-number
                               (native-module/hex-to-number
                                (hex/normalize-hex
                                 amount-in))
                               pay-token-decimals))
         pay-amount         (when amount-in
                              (number/remove-trailing-zeroes
                               (.toFixed pay-amount
                                         (min pay-token-decimals
                                              constants/min-token-decimals-to-display))))]
     (or pay-amount 0))))

(rf/reg-sub
 :wallet/swap-proposal-provider
 :<- [:wallet/swap-proposal]
 (fn [swap-proposal]
   (let [bridge-name  (:bridge-name swap-proposal)
         provider-key (keyword (string/lower-case bridge-name))]
     (get constants/swap-providers provider-key))))

(rf/reg-sub
 :wallet/swap-proposal-approval-required
 :<- [:wallet/swap-proposal]
 :-> :approval-required)

(rf/reg-sub
 :wallet/swap-proposal-approval-amount-required
 :<- [:wallet/swap-proposal]
 :-> :approval-amount-required)

(rf/reg-sub
 :wallet/swap-proposal-estimated-time
 :<- [:wallet/swap-proposal]
 :-> :estimated-time)

(rf/reg-sub
 :wallet/wallet-swap-proposal-fee-fiat-formatted
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/swap-proposal]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[account swap-proposal currency currency-symbol] [_ token-symbol-for-fees]]
   (when token-symbol-for-fees
     (let [tokens                  (:tokens account)
           token-for-fees          (first (filter #(= (string/lower-case (:symbol %))
                                                      (string/lower-case token-symbol-for-fees))
                                                  tokens))
           fee-in-native-token     (send-utils/calculate-full-route-gas-fee [swap-proposal])
           fee-in-crypto-formatted (utils/get-standard-crypto-format
                                    token-for-fees
                                    fee-in-native-token)
           fee-in-fiat             (utils/calculate-token-fiat-value
                                    {:currency currency
                                     :balance  fee-in-native-token
                                     :token    token-for-fees})
           fee-formatted           (utils/get-standard-fiat-format
                                    fee-in-crypto-formatted
                                    currency-symbol
                                    fee-in-fiat)]
       fee-formatted))))
