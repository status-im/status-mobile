(ns status-im.subs.wallet.swap
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.send.utils :as send-utils]
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
 :wallet/swap-asset-to-pay-decimals
 :<- [:wallet/swap-asset-to-pay]
 :-> :decimals)

(rf/reg-sub
 :wallet/swap-asset-to-pay-symbol
 :<- [:wallet/swap-asset-to-pay]
 :-> :symbol)

(rf/reg-sub
 :wallet/swap-asset-to-receive
 :<- [:wallet/swap]
 :-> :asset-to-receive)

(rf/reg-sub
 :wallet/swap-network
 :<- [:wallet/swap]
 :-> :network)

(rf/reg-sub
 :wallet/swap-start-point
 :<- [:wallet/swap]
 :-> :start-point)

(rf/reg-sub
 :wallet/swap-error-response
 :<- [:wallet/swap]
 :-> :error-response)

(rf/reg-sub
 :wallet/swap-error-response-code
 :<- [:wallet/swap-error-response]
 :-> :code)

(rf/reg-sub
 :wallet/swap-error-response-details
 :<- [:wallet/swap-error-response]
 :-> :details)

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
         fiat-formatted     (utils/fiat-formatted-for-ui currency-symbol
                                                         fiat-value)]
     {:crypto (str crypto-formatted " " token-symbol)
      :fiat   fiat-formatted})))

(rf/reg-sub
 :wallet/swap-max-slippage
 :<- [:wallet/swap]
 :-> :max-slippage)

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
 :wallet/swap-proposal-without-fees
 :<- [:wallet/swap]
 (fn [swap]
   (let [swap-proposal (:swap-proposal swap)]
     (reduce dissoc
             swap-proposal
             [:gas-fees :gas-amount :token-fees :bonder-fees :approval-gas-fees]))))

(rf/reg-sub
 :wallet/swap-amount
 :<- [:wallet/swap]
 :-> :amount)

(rf/reg-sub
 :wallet/swap-approved-amount
 :<- [:wallet/swap]
 :-> :approved-amount)

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
         amount-out-whole       (when amount-out
                                  (number/hex->whole amount-out receive-token-decimals))
         amount-out-num         (when amount-out-whole
                                  (number/to-fixed amount-out-whole
                                                   receive-token-decimals))
         display-decimals       (min receive-token-decimals
                                     constants/min-token-decimals-to-display)
         receive-amount         (when amount-out-num
                                  (utils/sanitized-token-amount-to-display amount-out-num
                                                                           display-decimals))]
     (or receive-amount 0))))

(rf/reg-sub
 :wallet/swap-receive-amount-raw
 :<- [:wallet/swap-proposal-amount-out]
 :<- [:wallet/swap-asset-to-receive]
 (fn [[amount-out asset-to-receive]]
   (let [receive-token-decimals (:decimals asset-to-receive)
         amount-out-whole       (when amount-out
                                  (number/hex->whole amount-out receive-token-decimals))
         amount-out-num         (when amount-out-whole
                                  (number/to-fixed amount-out-whole
                                                   receive-token-decimals))]
     (or amount-out-num 0))))

(rf/reg-sub
 :wallet/swap-pay-amount
 :<- [:wallet/swap-proposal-amount-in]
 :<- [:wallet/swap-asset-to-pay]
 (fn [[amount-in asset-to-pay]]
   (let [pay-token-decimals (:decimals asset-to-pay)
         amount-in-whole    (when amount-in
                              (number/hex->whole amount-in pay-token-decimals))
         amount-in-num      (when amount-in-whole
                              (number/to-fixed amount-in-whole
                                               pay-token-decimals))
         display-decimals   (min pay-token-decimals
                                 constants/min-token-decimals-to-display)
         pay-amount         (when amount-in-num
                              (utils/sanitized-token-amount-to-display amount-in-num
                                                                       display-decimals))]
     (or pay-amount 0))))

(rf/reg-sub
 :wallet/swap-pay-amount-raw
 :<- [:wallet/swap-proposal-amount-in]
 :<- [:wallet/swap-asset-to-pay]
 (fn [[amount-in asset-to-pay]]
   (let [pay-token-decimals (:decimals asset-to-pay)
         amount-in-whole    (when amount-in
                              (number/hex->whole amount-in pay-token-decimals))
         amount-in-num      (when amount-in-whole
                              (number/to-fixed amount-in-whole
                                               pay-token-decimals))]
     (or amount-in-num 0))))

(rf/reg-sub
 :wallet/swap-proposal-provider
 :<- [:wallet/swap-proposal]
 (fn [swap-proposal]
   (when swap-proposal
     (let [bridge-name  (:bridge-name swap-proposal)
           provider-key (keyword (string/lower-case bridge-name))]
       (get constants/swap-providers provider-key)))))

(rf/reg-sub
 :wallet/swap-proposal-approval-required
 :<- [:wallet/swap-proposal]
 :-> :approval-required)

(rf/reg-sub
 :wallet/swap-proposal-approval-contract-address
 :<- [:wallet/swap-proposal]
 :-> :approval-contract-address)

(rf/reg-sub
 :wallet/swap-proposal-approval-amount-required
 :<- [:wallet/swap-proposal]
 :-> :approval-amount-required)

(rf/reg-sub
 :wallet/swap-proposal-estimated-time
 :<- [:wallet/swap-proposal]
 :-> :estimated-time)

(rf/reg-sub
 :wallet/wallet-swap-proposal-fee-fiat
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/swap-proposal]
 :<- [:profile/currency]
 (fn [[account swap-proposal currency] [_ token-symbol-for-fees]]
   (when token-symbol-for-fees
     (let [tokens              (:tokens account)
           token-for-fees      (first (filter #(= (string/lower-case (:symbol %))
                                                  (string/lower-case token-symbol-for-fees))
                                              tokens))
           fee-in-native-token (send-utils/calculate-full-route-gas-fee [swap-proposal])
           fee-in-fiat         (utils/calculate-token-fiat-value
                                {:currency currency
                                 :balance  fee-in-native-token
                                 :token    token-for-fees})]
       fee-in-fiat))))

(rf/reg-sub
 :wallet/swap-asset-to-pay-balance-for-chain-data
 :<- [:wallet/swap-asset-to-pay]
 (fn [asset-to-pay]
   (let [token-symbol (or (:symbol asset-to-pay) constants/token-for-fees-symbol)]
     @(rf/subscribe [:wallet/token-by-symbol token-symbol]))))

(rf/reg-sub
 :wallet/swap-asset-to-pay-balance-for-chain
 :<- [:wallet/swap-asset-to-pay-balance-for-chain-data]
 (fn [asset-to-pay-with-current-account-balance [_ chain-id]]
   (let [pay-token-decimals               (:decimals asset-to-pay-with-current-account-balance)
         pay-token-balance-selected-chain (-> (get-in asset-to-pay-with-current-account-balance
                                                      [:balances-per-chain chain-id :raw-balance]
                                                      0)
                                              (number/convert-to-whole-number pay-token-decimals))]
     pay-token-balance-selected-chain)))

(rf/reg-sub
 :wallet/swap-asset-to-pay-balance-for-chain-ui
 :<- [:wallet/swap-asset-to-pay-balance-for-chain-data]
 (fn [asset-to-pay-with-current-account-balance [_ chain-id]]
   (utils/token-balance-display-for-network
    asset-to-pay-with-current-account-balance
    chain-id
    constants/min-token-decimals-to-display)))

(rf/reg-sub
 :wallet/swap-asset-to-pay-amount-in-fiat
 :<- [:wallet/swap-asset-to-pay-balance-for-chain-data]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[asset-to-pay-with-current-account-balance currency currency-symbol] [_ amount]]
   (utils/formatted-token-fiat-value
    {:currency        currency
     :currency-symbol currency-symbol
     :balance         (or amount 0)
     :token           asset-to-pay-with-current-account-balance})))

(rf/reg-sub
 :wallet/approval-gas-fees
 :<- [:wallet/current-viewing-account]
 :<- [:wallet/swap-proposal]
 :<- [:profile/currency]
 (fn [[account {:keys [approval-gas-fees]} currency]]
   (let [tokens         (:tokens account)
         token-for-fees (first (filter #(= (string/lower-case (:symbol %))
                                           (string/lower-case constants/token-for-fees-symbol))
                                       tokens))
         fee-in-fiat    (utils/calculate-token-fiat-value
                         {:currency currency
                          :balance  approval-gas-fees
                          :token    token-for-fees})]
     fee-in-fiat)))

