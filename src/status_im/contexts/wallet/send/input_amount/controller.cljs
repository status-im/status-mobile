(ns status-im.contexts.wallet.send.input-amount.controller
  (:require
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]))

;; notes
;; token-by-symbol and token looks very similar but they have difference in market values data
;; inside token structure :total-balance and :available-balance are same, not sure if they have
;; different
;; meaning

;; subs

(rf/reg-sub
 :layers/ui
 :<- [:layers]
 :-> :ui)

(rf/reg-sub
 :ui/send
 :<- [:layers/ui]
 :-> :send)

(rf/reg-sub
 :ui/send-input-amount-screen
 :<- [:ui/send]
 :-> :input-amount-screen)

(rf/reg-sub
 :send-input-amount-screen/controller
 :<- [:ui/send-input-amount-screen]
 :-> :controller)

(rf/reg-sub
 :send-input-amount-screen/currency-information
 :<- [:wallet/wallet-send-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 (fn [[{token-symbol :symbol
        total-balance :total-balance
        :as
        token}
       currency currency-symbol]]
   {:usd-conversion-rate (utils/token-usd-price token)
    :currency            currency
    :currency-symbol     currency-symbol
    :token-symbol        token-symbol
    :conversion-rate     (-> token
                             :market-values-per-currency
                             currency
                             :price)
    :token-decimals      (-> token
                             utils/token-usd-price
                             utils/one-cent-value
                             utils/calc-max-crypto-decimals)
    :token               token
    :total-balance       total-balance}))

(rf/reg-sub
 :send-input-amount-screen/token-by-symbol
 :<- [:wallet/wallet-send-enabled-from-chain-ids]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[enabled-from-chain-ids {:keys [token-symbol]}]]
   (rf/sub [:wallet/token-by-symbol
            (str token-symbol)
            enabled-from-chain-ids])))

(rf/reg-sub
 :send-input-amount-screen/max-limit
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[{:keys [crypto-currency?]}
       {:keys [total-balance usd-conversion-rate conversion-rate]}]]
   (if crypto-currency?
     (utils/cut-crypto-decimals-to-fit-usd-cents
      total-balance
      usd-conversion-rate)
     (-> (money/crypto->fiat
          total-balance
          conversion-rate)
         utils/cut-fiat-balance-to-two-decimals))))

(rf/reg-sub
 :send-input-amount-screen/amount-in-crypto
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[{:keys [crypto-currency? input-state]}
       {:keys [conversion-rate token-decimals]}]]
   (let [input-value (controlled-input/input-value input-state)]
     (if crypto-currency?
       input-value
       (number/remove-trailing-zeroes
        (.toFixed (/ input-value conversion-rate)
                  token-decimals))))))

(rf/reg-sub
 :send-input-amount-screen/token-input-converted-value
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[{:keys [crypto-currency? input-state]}
       {:keys [conversion-rate currency-symbol token-symbol]}]]
   (let [input-value (controlled-input/input-value input-state)]
     (if crypto-currency?
       (utils/prettify-balance
        currency-symbol
        (money/crypto->fiat input-value
                            conversion-rate))
       (utils/prettify-crypto-balance
        (or (clj->js token-symbol) "")
        (money/fiat->crypto input-value
                            conversion-rate)
        conversion-rate)))))



(rf/reg-sub
 :send-input-amount-screen/data
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/max-limit]
 :<- [:send-input-amount-screen/amount-in-crypto]
 :<- [:send-input-amount-screen/token-input-converted-value]
 (fn [[{:keys [crypto-currency? input-state] :as controller}
       {:keys [conversion-rate]}
       max-limit
       amount-in-crypto
       token-input-converted-value]]
   {:crypto-currency?            crypto-currency?
    :max-limit                   max-limit
    :input-state                 input-state
    :input-value                 (controlled-input/input-value input-state)
    :input-error                 (controlled-input/input-error input-state)
    :valid-input?                (not (or (controlled-input/empty-value? input-state)
                                          (controlled-input/input-error input-state)))
    :limit-exceeded?             (controlled-input/upper-limit-exceeded? input-state)
    :amount-in-crypto            amount-in-crypto
    :token-input-converted-value token-input-converted-value
    :conversion-rate             conversion-rate
   }))



;; events

(rf/reg-event-fx
 :send-input-amount-screen/set-input-state
 (fn [{:keys [db]} [f]]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :input-state] f)}))

(rf/reg-event-fx
 :send-input-amount-screen/swap-between-fiat-and-crypto
 (fn [{:keys [db]} [crypto-currency? conversion-rate]]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :crypto-currency?] not)
    :fx (if crypto-currency?
          [[:dispatch
            [:send-input-amount-screen/set-input-state #(controlled-input/->fiat % conversion-rate)]]]
          [[:dispatch
            [:send-input-amount-screen/set-input-state
             #(controlled-input/->crypto % conversion-rate)]]])}))




(comment
  (rf/dispatch [:send-input-amount-screen/swap-between-fiat-and-crypto])
  (rf/sub [:send-input-amount-screen/max-limit])
  (rf/sub [:send-input-amount-screen/amount-in-crypto])
  (rf/sub [:send-input-amount-screen/data])
  (rf/sub [:send-input-amount-screen/controller])

  (rf/dispatch [:send-input-amount-screen/set-input-state #(controlled-input/add-character % "1")])
  (rf/dispatch [:send-input-amount-screen/set-input-state])

  (tap> {:token-by-symbol (rf/sub [:send-input-amount-screen/token-by-symbol])
         :token           (:token (rf/sub [:send-input-amount-screen/currency-information]))})

)
