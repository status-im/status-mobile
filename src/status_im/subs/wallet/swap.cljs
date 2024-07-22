(ns status-im.subs.wallet.swap
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [utils.money :as money]))

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
