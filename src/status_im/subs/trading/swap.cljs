(ns status-im.subs.trading.swap
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.trading.swap.routes :as routes]
            [status-im.contexts.wallet.trading.swap.utils :as swap-utils]
            [utils.money :as money]
            [utils.number :as number]))

;; == root keys ==

(rf/reg-sub
 :trading.swap/ui-data
 :<- [:wallet/ui]
 :-> :trading-swap)

(rf/reg-sub
 :trading.swap/focused?
 :<- [:trading.swap/ui-data]
 (fn [{:keys [focused?]}]
   focused?))

(rf/reg-sub
 :trading.swap/pay-token-symbol
 :<- [:trading.swap/ui-data]
 :-> :pay-token-symbol)

(rf/reg-sub
 :trading.swap/receive-token-symbol
 :<- [:trading.swap/ui-data]
 :-> :receive-token-symbol)

(rf/reg-sub
 :trading.swap/chain-id
 :<- [:trading.swap/ui-data]
 :-> :chain-id)

(rf/reg-sub
 :trading.swap/max-slippage
 :<- [:trading.swap/ui-data]
 :-> :max-slippage)

(rf/reg-sub
 :trading.swap/account-address
 :<- [:trading.swap/ui-data]
 :-> :account-address)

(rf/reg-sub
 :trading.swap/pay-amount
 :<- [:trading.swap/ui-data]
 :-> :pay-amount)

(rf/reg-sub
 :trading.swap/initialized?
 :<- [:trading.swap/ui-data]
 (fn [{:keys [initialized?]}]
   (or initialized? false)))

(rf/reg-sub
 :trading.swap/requested-route-uuid
 :<- [:trading.swap/ui-data]
 :-> :requested-route-uuid)

(rf/reg-sub
 :trading.swap/approvals
 :<- [:trading.swap/ui-data]
 :-> :approvals)

(rf/reg-sub
 :trading.swap/route
 :<- [:wallet.routes/uuid]
 :<- [:wallet.routes/best-route]
 :<- [:trading.swap/requested-route-uuid]
 (fn [[route-uuid best-route requested-route-uuid]]
   (when (= requested-route-uuid route-uuid)
     best-route)))

(rf/reg-sub
 :trading.swap/route-error
 :<- [:wallet.routes/error]
 :<- [:wallet.routes/uuid]
 :<- [:trading.swap/requested-route-uuid]
 (fn [[error route-uuid requested-route-uuid]]
   (when (= requested-route-uuid route-uuid)
     error)))

(rf/reg-sub
 :trading.swap/processing-route?
 :<- [:wallet.routes/processing-routes?]
 (fn [processing?]
   processing?))

;; =====

(rf/reg-sub
 :trading.swap/loading?
 :<- [:trading.swap/initialized?]
 :<- [:trading.swap/route]
 (fn [[initialized? route]]
   (or (not initialized?) (nil? route))))

(rf/reg-sub
 :trading.swap/network
 :<- [:wallet/network-details]
 :<- [:trading.swap/chain-id]
 (fn [[networks chain-id]]
   (some #(when (= chain-id (:chain-id %)) %) networks)))

(rf/reg-sub
 :trading.swap/account
 :<- [:wallet/accounts-by-address]
 :<- [:trading.swap/account-address]
 (fn [[accounts address]]
   (get accounts address)))

(rf/reg-sub
 :trading.swap/customization-color
 :<- [:trading.swap/account]
 (fn [account]
   (:color account)))

(rf/reg-sub
 :trading.swap/pay-token
 :<- [:wallet/normalized-tokens]
 :<- [:trading.swap/pay-token-symbol]
 :<- [:trading.swap/chain-id]
 (fn [[normalized-tokens token-symbol chain-id]]
   (get-in normalized-tokens [token-symbol chain-id])))

(rf/reg-sub
 :trading.swap/receive-token
 :<- [:wallet/normalized-tokens]
 :<- [:trading.swap/receive-token-symbol]
 :<- [:trading.swap/chain-id]
 (fn [[normalized-tokens token-symbol chain-id]]
   (get-in normalized-tokens [token-symbol chain-id])))

(rf/reg-sub
 :trading.swap/pay-token-balance
 :<- [:wallet/balances]
 :<- [:trading.swap/pay-token]
 :<- [:trading.swap/chain-id]
 :<- [:trading.swap/account-address]
 (fn [[balances pay-token chain-id address]]
   (let [token-symbol (:symbol pay-token)
         decimals     (:decimals pay-token)]
     (-> balances
         (get-in [address token-symbol chain-id])
         :raw-balance
         (number/convert-to-whole-number decimals)
         (or "0")))))

(rf/reg-sub
 :trading.swap/pay-amount-valid?
 :<- [:trading.swap/pay-amount]
 :<- [:trading.swap/pay-token-balance]
 (fn [[pay-amount pay-balance]]
   (swap-utils/pay-input-valid? pay-amount pay-balance)))

(rf/reg-sub
 :trading.swap/pay-amount-error?
 :<- [:trading.swap/pay-amount]
 :<- [:trading.swap/pay-token-balance]
 (fn [[pay-amount pay-balance]]
   (swap-utils/pay-input-error? pay-amount pay-balance)))

(rf/reg-sub
 :trading.swap/error?
 :<- [:trading.swap/route-error]
 :<- [:trading.swap/pay-amount-error?]
 (fn [[route-error pay-amount-error]]
   (boolean (or route-error pay-amount-error))))

(rf/reg-sub
 :trading.swap/pay-fiat-amount
 :<- [:trading.swap/pay-token]
 :<- [:trading.swap/pay-amount]
 :<- [:wallet/prices-per-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[pay-token pay-amount prices-per-token currency currency-symbol]]
   (utils/formatted-token-fiat-value
    {:currency         currency
     :currency-symbol  currency-symbol
     :balance          (or pay-amount 0)
     :token            pay-token
     :prices-per-token prices-per-token})))

(rf/reg-sub
 :trading.swap/pay-fiat-amount
 :<- [:trading.swap/pay-token]
 :<- [:trading.swap/pay-amount]
 :<- [:wallet/prices-per-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[pay-token pay-amount prices-per-token currency currency-symbol]]
   (utils/formatted-token-fiat-value
    {:currency         currency
     :currency-symbol  currency-symbol
     :balance          (or pay-amount 0)
     :token            pay-token
     :prices-per-token prices-per-token})))

(rf/reg-sub
 :trading.swap/receive-amount
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (-> route
         routes/amount-out
         :whole))))

(rf/reg-sub
 :trading.swap/receive-fiat-amount
 :<- [:trading.swap/receive-token]
 :<- [:trading.swap/receive-amount]
 :<- [:wallet/prices-per-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[receive-token receive-amount prices-per-token currency currency-symbol]]
   (utils/formatted-token-fiat-value
    {:currency         currency
     :currency-symbol  currency-symbol
     :balance          (or receive-amount 0)
     :token            receive-token
     :prices-per-token prices-per-token})))

(rf/reg-sub
 :trading.swap/exchange-rate
 :<- [:trading.swap/route]
 :<- [:trading.swap/pay-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:wallet/prices-per-token]
 (fn [[route pay-token currency currency-symbol prices-per-token]]
   (when route
     (let [pay-amount         (-> route routes/amount-in :bn)
           receive-amount     (-> route routes/amount-out :bn)
           exchange-rate      (money/div pay-amount receive-amount)
           display-decimals   (min (:decimals pay-token)
                                   constants/min-token-decimals-to-display)
           exchange-rate      (number/to-fixed exchange-rate display-decimals)
           fiat-exchange-rate (utils/formatted-token-fiat-value
                               {:currency         currency
                                :currency-symbol  currency-symbol
                                :balance          exchange-rate
                                :token            pay-token
                                :prices-per-token prices-per-token})]
       {:crypto exchange-rate
        :fiat   fiat-exchange-rate}))))

;; === fees ===

(rf/reg-sub
 :trading.swap/total-fee-fiat
 :<- [:trading.swap/route]
 :<- [:profile/currency]
 :<- [:wallet/prices-per-token]
 (fn [[route currency prices-per-token]]
   (let [native-fee (-> route routes/fees :tx-total-fee money/wei->ether)]
     (utils/calculate-token-fiat-value
      {:currency         currency
       :balance          native-fee
       :token            {:symbol constants/token-for-fees-symbol}
       :prices-per-token prices-per-token}))))

(rf/reg-sub
 :trading.swap/approval-fee-fiat
 :<- [:trading.swap/route]
 :<- [:profile/currency]
 :<- [:wallet/prices-per-token]
 (fn [[route currency prices-per-token]]
   (let [native-fee (-> route routes/fees :approval-fee money/wei->ether)]
     (utils/calculate-token-fiat-value
      {:currency         currency
       :balance          native-fee
       :token            {:symbol constants/token-for-fees-symbol}
       :prices-per-token prices-per-token}))))

(rf/reg-sub
 :trading.swap/estimated-time
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (routes/estimated-time route))))

;; === formatted values ===

(rf/reg-sub
 :trading.swap/formatted-pay-token-balance
 :<- [:trading.swap/pay-token]
 :<- [:trading.swap/pay-token-balance]
 (fn [[{:keys [decimals]} balance]]
   (utils/sanitized-token-amount-to-display (money/bignumber balance)
                                            (min constants/min-token-decimals-to-display decimals))))

(rf/reg-sub
 :trading.swap/formatted-receive-amount
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (let [{:keys [whole decimals]} (routes/amount-out route)
           display-decimals         (min decimals constants/min-token-decimals-to-display)]
       (utils/sanitized-token-amount-to-display whole display-decimals)))))


;; === approvals ===

;; TODO
(rf/reg-sub
 :trading.swap/current-approval
 :<- [:trading.swap/ui-data]
 (fn [{:keys [approvals] :as swap-data}]
   (swap-utils/approval-for-inputs approvals swap-data)))

(rf/reg-sub
 :trading.swap/approval-required?
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (-> route routes/approval-data :required?))))

(rf/reg-sub
 :trading.swap/approval-transaction-id
 :<- [:trading.swap/current-approval]
 (fn [approval]
   (:transaction-hash approval)))

(rf/reg-sub
 :trading.swap/approval-status
 :<- [:wallet.routes/transactions]
 :<- [:trading.swap/approval-transaction-id]
 (fn [[transactions approval-transaction-id]]
   (get-in transactions [approval-transaction-id :status])))

(rf/reg-sub
 :trading.swap/last-approval-status
 :<- [:wallet.routes/transactions]
 :<- [:trading.swap/approvals]
 (fn [[transactions approvals]]
   (->> approvals
        last
        :transaction-hash
        (get transactions)
        :status)))

(rf/reg-sub
 :trading.swap/approval-amount
 :<- [:trading.swap/route]
 :<- [:trading.swap/pay-token]
 :<- [:trading.swap/approval-required?]
 :<- [:trading.swap/current-approval]
 (fn [[route {:keys [decimals]} approval-required? approval]]
   (if approval-required?
     (-> (routes/approval-data route)
         :amount-required
         (number/convert-to-whole-number decimals)
         (money/bignumber)
         (number/to-fixed decimals)
         (utils/sanitized-token-amount-to-display decimals))
     (:pay-amount approval))))

(rf/reg-sub
 :trading.swap/provider
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (->> route
          routes/processor-name
          string/lower-case
          keyword
          (get constants/swap-providers)))))

(rf/reg-sub
 :trading.swap/approval-contract-address
 :<- [:trading.swap/route]
 :<- [:trading.swap/provider]
 (fn [[route provider]]
   (when route
     (-> route
         routes/approval-data
         :contract-address
         (or (:contract-address provider))))))

(rf/reg-sub
 :trading.swap/approval-estimated-time
 :<- [:trading.swap/route]
 (fn [route]
   (when route
     (-> route
         routes/approval-data
         :estimated-time))))
