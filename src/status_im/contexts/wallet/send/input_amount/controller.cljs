(ns status-im.contexts.wallet.send.input-amount.controller
  (:require
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]
    [status-im.common.router :as router]))

;; notes
;; token-by-symbol and token looks very similar but they have difference in market values data
;; inside token structure :total-balance and :available-balance are same, not sure if they have
;; different
;; meaning

;; subs

(rf/reg-sub :layers/ui
 :<- [:layers]
 :-> :ui)

(rf/reg-sub :ui/send
 :<- [:layers/ui]
 :-> :send)

(rf/reg-sub :ui/send-input-amount-screen
 :<- [:ui/send]
 :-> :input-amount-screen)

(rf/reg-sub :send-input-amount-screen/controller
 :<- [:ui/send-input-amount-screen]
 :-> :controller)

(rf/reg-sub :send-input-amount-screen/currency-information
 :<- [:wallet/wallet-send-token]
 :<- [:profile/currency]
 :<- [:profile/currency-symbol]
 :<- [:profile/profile]
 (fn [[{token-symbol :symbol
        total-balance :total-balance
        :as
        token}
       currency
       currency-symbol
       {fiat-currency :currency}]]
   {:usd-conversion-rate (utils/token-usd-price token)
    :currency            currency
    :fiat-currency       fiat-currency
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

(rf/reg-sub :send-input-amount-screen/routes-information
 :<- [:wallet/wallet-send-route]
 :<- [:wallet/wallet-send-sender-network-values]
 :<- [:wallet/wallet-send-receiver-network-values]
 :<- [:wallet/wallet-send-suggested-routes]
 :<- [:wallet/wallet-send-loading-suggested-routes?]
 (fn [[route
       sender-network-values
       receiver-network-values
       suggested-routes
       loading-routes?]]
   {:route                   route
    :sender-network-values   sender-network-values
    :receiver-network-values receiver-network-values
    :suggested-routes        suggested-routes
    :loading-routes?         loading-routes?
    :routes                  (when suggested-routes
                               (or (:best suggested-routes) []))}))

(rf/reg-sub :send-input-amount-screen/networks-information
 :<- [:wallet/wallet-send-token]
 :<- [:wallet/wallet-send-receiver-networks]
 :<- [:wallet/wallet-send-receiver-preferred-networks]
 (fn [[{token-networks :networks}
       receiver-networks
       receiver-preferred-networks
      ]]
   {:token-networks              token-networks
    :receiver-networks           receiver-networks
    :receiver-preferred-networks receiver-preferred-networks}))

(rf/reg-sub :send-input-amount-screen/token-by-symbol
 :<- [:wallet/wallet-send-enabled-from-chain-ids]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[enabled-from-chain-ids {:keys [token-symbol]}]]
   (rf/sub [:wallet/token-by-symbol
            (str token-symbol)
            enabled-from-chain-ids])))

(rf/reg-sub :send-input-amount-screen/upper-limit
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
 (fn [[{:keys [crypto-currency? token-input-value]}
       {:keys [conversion-rate token-decimals]}]]
   (if crypto-currency?
     token-input-value
     (number/remove-trailing-zeroes
      (.toFixed (/ token-input-value conversion-rate)
                token-decimals)))))

(rf/reg-sub
 :send-input-amount-screen/token-not-supported-in-receiver-networks?
 :<- [:wallet/wallet-send-tx-type]
 :<- [:send-input-amount-screen/routes-information]
 (fn [[tx-type
       {:keys [receiver-network-values]}]]
   (and (not= tx-type :tx/bridge)
        (->> receiver-network-values
             (remove #(= (:type %) :add))
             (every? #(= (:type %) :not-available))))))

(defn- fiat->crypto
  [value conversion-rate]
  (-> value
      (money/fiat->crypto conversion-rate)
      (utils/cut-crypto-decimals-to-fit-usd-cents conversion-rate)))

(defn- crypto->fiat
  [value conversion-rate]
  (-> value
      (money/crypto->fiat conversion-rate)
      (utils/cut-fiat-balance-to-two-decimals)))

(rf/reg-sub :send-input-amount-screen/token-input-converted-value
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[{:keys [crypto-currency? token-input-value]}
       {:keys [conversion-rate]}]]
   (if crypto-currency?
     (crypto->fiat token-input-value conversion-rate)
     (fiat->crypto token-input-value conversion-rate))))

(rf/reg-sub :send-input-amount-screen/token-input-converted-value-prettified
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/token-input-converted-value]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[{:keys [crypto-currency?]}
       token-input-converted-value
       {:keys [currency-symbol token-symbol]}]]
   (if crypto-currency?
     (utils/prepend-curency-symbol-to-fiat-balance token-input-converted-value currency-symbol)
     (utils/add-token-symbol-to-crypto-balance token-input-converted-value
                                               (or (clj->js token-symbol) "")))))

(rf/reg-sub :send-input-amount-screen/upper-limit-prettified
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [crypto-currency?]}
       {:keys [currency-symbol token-symbol conversion-rate]}
       upper-limit]]
   (if crypto-currency?
     (utils/prettify-crypto-balance
      (or (clj->js token-symbol) "")
      (money/bignumber upper-limit)
      conversion-rate)
     (utils/prettify-balance currency-symbol
                             (money/bignumber upper-limit)))
 ))

(rf/reg-sub :send-input-amount-screen/value-out-of-limits?
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [token-input-value]}
       upper-limit]]
   (controlled-input-logic/value-out-of-limits? token-input-value upper-limit 0)))

(rf/reg-sub :send-input-amount-screen/data
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/upper-limit]
 :<- [:send-input-amount-screen/amount-in-crypto]
 :<- [:send-input-amount-screen/token-input-converted-value]
 :<- [:send-input-amount-screen/token-input-converted-value-prettified]
 :<- [:send-input-amount-screen/value-out-of-limits?]
 :<- [:send-input-amount-screen/upper-limit-prettified]
 :<- [:send-input-amount-screen/routes-information]
 :<- [:send-input-amount-screen/token-not-supported-in-receiver-networks?]
 :<- [:send-input-amount-screen/networks-information]
 (fn
   [[{:keys [crypto-currency? token-input-value] :as controller}
     {:keys [fiat-currency token-symbol token] :as currency-information}
     upper-limit
     amount-in-crypto
     token-input-converted-value
     token-input-converted-value-prettified
     value-out-of-limits?
     upper-limit-prettified
     {:keys [route
             routes
             sender-network-values
             loading-routes?]
      :as   routes-information}
     token-not-supported-in-receiver-networks?
     {:keys [token-networks
             receiver-networks
             receiver-preferred-networks]}]]
   {:crypto-currency?                          crypto-currency?
    :fiat-currency                             fiat-currency
    :token                                     token
    :token-symbol                              token-symbol
    :upper-limit                               upper-limit
    :upper-limit-prettified                    upper-limit-prettified
    :input-value                               token-input-value
    :value-out-of-limits?                      value-out-of-limits?
    :valid-input?                              (not (or (controlled-input-logic/empty-value?
                                                         token-input-value)
                                                        value-out-of-limits?))
    :upper-limit-exceeded?                     (controlled-input-logic/upper-limit-exceeded?
                                                token-input-value
                                                upper-limit)
    :amount-in-crypto                          amount-in-crypto
    :token-input-converted-value               token-input-converted-value
    :token-input-converted-value-prettified    token-input-converted-value-prettified
    :route                                     route
    :routes                                    routes
    :sender-network-values                     sender-network-values
    :loading-routes?                           loading-routes?
    :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
    :token-networks                            token-networks
    :receiver-networks                         receiver-networks
    :receiver-preferred-networks               receiver-preferred-networks}))



;; events
(def token-input-value-path [:layers :ui :send :input-amount-screen :controller :token-input-value])

(rf/reg-event-fx :send-input-amount-screen/set-input-state
 (fn [{:keys [db]} [f]]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :input-state] f)}))

(rf/reg-event-fx :send-input-amount-screen/set-token-input-value
 (fn [{:keys [db]} [v]]
   {:db (assoc-in db token-input-value-path v)}))


(rf/reg-event-fx :send-input-amount-screen/swap-between-fiat-and-crypto
 (fn [{:keys [db]} [token-input-converted-value]]
   {:db (update-in db [:layers :ui :send :input-amount-screen :controller :crypto-currency?] not)
    :fx [[:dispatch
          [:send-input-amount-screen/set-token-input-value token-input-converted-value]]]}))


(rf/reg-event-fx :send-input-amount-screen/token-input-add-character
 (fn [{:keys [db]} [c]]
   {:db (update-in db token-input-value-path #(controlled-input-logic/add-character % c))}))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-last
 (fn [{:keys [db]}]
   {:db (update-in db token-input-value-path controlled-input-logic/delete-last)}))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-all
 (fn [{:keys [db]}]
   {:db (assoc-in db token-input-value-path "")}))



(comment
  (rf/dispatch [:send-input-amount-screen/swap-between-fiat-and-crypto])
  (rf/sub [:send-input-amount-screen/upper-limit])
  (rf/sub [:send-input-amount-screen/amount-in-crypto])
  (rf/sub [:send-input-amount-screen/data])
  (rf/sub [:send-input-amount-screen/controller])

  (rf/dispatch [:send-input-amount-screen/set-input-state])

  (tap> {:token-by-symbol (rf/sub [:send-input-amount-screen/token-by-symbol])
         :token           (:token (rf/sub [:send-input-amount-screen/currency-information]))})
)
