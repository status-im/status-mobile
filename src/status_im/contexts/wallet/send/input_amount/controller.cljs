(ns status-im.contexts.wallet.send.input-amount.controller
  (:require
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.money :as money]
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
 :send-input-amount-screen/data
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/max-limit]
 (fn [[{:keys [crypto-currency? input-state] :as controller} max-limit]]
   {:crypto-currency? crypto-currency?
    :max-limit        max-limit
    :input-state      input-state
   }))



;; events

(rf/reg-event-fx
 :send-input-amount-screen/swap-between-fiat-and-crypto
 (fn [{:keys [db]}]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :crypto-currency?] not)}))

(rf/reg-event-fx
 :send-input-amount-screen/set-input-state
 (fn [{:keys [db]} [f]]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :input-state] f)}))




(comment
  (rf/dispatch [:send-input-amount-screen/swap-between-fiat-and-crypto])
  (rf/sub [:send-input-amount-screen/max-limit])
  (rf/sub [:send-input-amount-screen/data])
  (rf/sub [:send-input-amount-screen/controller])

  (rf/dispatch [:send-input-amount-screen/set-input-state #(controlled-input/add-character % "1")])
  (rf/dispatch [:send-input-amount-screen/set-input-state])

  (tap> {:token-by-symbol (rf/sub [:send-input-amount-screen/token-by-symbol])
         :token           (:token (rf/sub [:send-input-amount-screen/currency-information]))})

)

#_(rf/reg-sub
   :wallet/wallet-send-enough-assets?
   :<- [:wallet/wallet-send]
   :-> :enough-assets?)

#_(rf/reg-sub
   :wallet/wallet-send-token
   :<- [:wallet/wallet-send]
   :<- [:wallet/network-details]
   :<- [:wallet/wallet-send-disabled-from-chain-ids]
   (fn [[wallet-send networks disabled-from-chain-ids]]
     (let [token                    (:token wallet-send)
           disabled-from-chain-ids? (set disabled-from-chain-ids)
           enabled-from-chain-ids   (->> networks
                                         (map :chain-id)
                                         (remove disabled-from-chain-ids?)
                                         set)]
       (some-> token
               (assoc :networks          (network-utils/network-list token networks)
                      :available-balance (utils/calculate-total-token-balance token)
                      :total-balance     (utils/calculate-total-token-balance
                                          token
                                          enabled-from-chain-ids))))))
