(ns status-im.contexts.wallet.send.input-amount.controller
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.money :as money]
    [utils.number :as number]
    [utils.re-frame :as rf]))

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
    :token               token}))

(rf/reg-sub :send-input-amount-screen/max-decimals
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:wallet/wallet-send-receiver-preferred-networks]
 (fn [[{:keys [crypto-currency?]}
       {:keys [token-decimals]}]]
   (if crypto-currency? token-decimals 2)))

(rf/reg-sub :send-input-amount-screen/enabled-from-chain-ids
 :<- [:wallet/wallet-send-tx-type]
 :<- [:wallet/wallet-send-enabled-from-chain-ids]
 :<- [:wallet/bridge-from-chain-ids]
 (fn [[tx-type send-chain-ids bridge-chain-ids]]
   (if (= tx-type :tx/bridge)
     bridge-chain-ids
     send-chain-ids)))

(rf/reg-sub :send-input-amount-screen/from-enabled-networks
 :<- [:wallet/wallet-send-tx-type]
 :<- [:wallet/wallet-send-enabled-networks]
 :<- [:wallet/bridge-from-networks]
 (fn [[tx-type send-enabled-networks bridge-enabled-networks]]
   (if (= tx-type :tx/bridge)
     bridge-enabled-networks
     send-enabled-networks)))


#_(comment
    (inc 1)
    (rf/sub [:wallet/wallet-send])
    (rf/sub [:send-input-amount-screen/from-enabled-networks])
    (rf/sub [:wallet/wallet-send-tx-type])
    (rf/sub [:wallet/wallet-send-enabled-networks])
    (rf/sub [:wallet/bridge-from-networks]))

(defn- every-network-value-is-zero?
  [sender-network-values]
  (every? (fn [{:keys [total-amount]}]
            (and
             total-amount
             (money/bignumber? total-amount)
             (money/equal-to total-amount
                             (money/bignumber "0"))))
          sender-network-values))

(rf/reg-sub :send-input-amount-screen/routes-information
 :<- [:wallet/wallet-send-route]
 :<- [:wallet/wallet-send-sender-network-values]
 :<- [:wallet/wallet-send-receiver-network-values]
 :<- [:wallet/wallet-send-suggested-routes]
 :<- [:wallet/wallet-send-loading-suggested-routes?]
 :<- [:wallet/wallet-send-tx-type]
 (fn [[route
       sender-network-values
       receiver-network-values
       suggested-routes
       loading-routes?
       tx-type]]
   (let [token-not-supported-in-receiver-networks? (and (not= tx-type :tx/bridge)
                                                        (->> receiver-network-values
                                                             (remove #(= (:type %) :add))
                                                             (every? #(= (:type %) :not-available))))
         routes                                    (when suggested-routes
                                                     (or (:best suggested-routes) []))]
     {:route                                     route
      :sender-network-values                     sender-network-values
      :receiver-network-values                   receiver-network-values
      :suggested-routes                          suggested-routes
      :loading-routes?                           loading-routes?
      :routes                                    routes
      :token-not-supported-in-receiver-networks? token-not-supported-in-receiver-networks?
      :no-routes-found?                          (and
                                                  (every-network-value-is-zero?
                                                   sender-network-values)
                                                  (not (nil? routes))
                                                  (not loading-routes?)
                                                  (not token-not-supported-in-receiver-networks?))})))

(rf/reg-sub :send-input-amount-screen/networks-information
 :<- [:wallet/wallet-send-token]
 :<- [:wallet/wallet-send-receiver-networks]
 :<- [:wallet/wallet-send-receiver-preferred-networks]
 (fn [[{token-networks :networks}
       receiver-networks
       receiver-preferred-networks]]
   {:token-networks                   token-networks
    :receiver-networks                receiver-networks
    :sending-to-unpreferred-networks? (not (every? (fn [receiver-selected-network]
                                                     (contains?
                                                      (set receiver-preferred-networks)
                                                      receiver-selected-network))
                                                   receiver-networks))}))

(rf/reg-sub :send-input-amount-screen/token-by-symbol
 :<- [:send-input-amount-screen/enabled-from-chain-ids]
 :<- [:send-input-amount-screen/currency-information]
 (fn [[enabled-from-chain-ids {:keys [token-symbol]}]]
   (rf/sub [:wallet/token-by-symbol
            (str token-symbol)
            enabled-from-chain-ids])))

(rf/reg-sub :send-input-amount-screen/total-balance
 :<- [:send-input-amount-screen/token-by-symbol]
 (fn [{:keys [total-balance]}]
   total-balance))

(rf/reg-sub :send-input-amount-screen/upper-limit
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/total-balance]
 (fn [[{:keys [crypto-currency?]}
       {:keys [usd-conversion-rate conversion-rate]}
       total-balance]]
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

(rf/reg-sub :send-input-amount-screen/recipient-gets-amount
 :<- [:send-input-amount-screen/currency-information]
 :<- [:wallet/total-amount-in-to-chains]
 (fn [[{:keys [conversion-rate token-symbol]}
       total-amount-receiver]]
   (utils/prettify-crypto-balance
    token-symbol
    total-amount-receiver
    conversion-rate)))

(rf/reg-sub :send-input-amount-screen/owned-eth-balance-is-zero?
 :<- [:send-input-amount-screen/enabled-from-chain-ids]
 (fn [enabled-from-chain-ids]
   (let [owned-eth-token (rf/sub [:wallet/token-by-symbol
                                  (string/upper-case
                                   constants/mainnet-short-name)
                                  enabled-from-chain-ids])]
     (money/equal-to (:total-balance owned-eth-token) 0))))

(rf/reg-sub :send-input-amount-screen/fee-formatted
 :<- [:send-input-amount-screen/routes-information]
 (fn [{:keys [route]}]
   (let [native-currency-symbol (when-not (or (nil? route) (empty? route))
                                  (get-in (first route)
                                          [:from :native-currency-symbol]))]
     (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol]))))

(rf/reg-sub :send-input-amount-screen/upper-limit-exceeded?
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [token-input-value]}
       upper-limit]]
   (controlled-input-logic/upper-limit-exceeded?
    token-input-value
    upper-limit)))

(rf/reg-sub :send-input-amount-screen/upper-limit-equals-input-value?
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [token-input-value]}
       upper-limit]]
   (money/equal-to
    (money/bignumber token-input-value)
    (money/bignumber upper-limit))))

(rf/reg-sub :send-input-amount-screen/should-try-again?
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/routes-information]
 (fn [[upper-limit-exceeded?
       {:keys [no-routes-found?]}]]
   (and (not upper-limit-exceeded?) no-routes-found?)))

(rf/reg-sub :send-input-amount-screen/not-enough-asset?
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/upper-limit-equals-input-value?]
 :<- [:send-input-amount-screen/routes-information]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/owned-eth-balance-is-zero?]
 (fn [[upper-limit-exceeded?
       upper-limit-equals-input-value?
       {:keys [sender-network-values no-routes-found?]}
       {:keys [token-symbol]}
       owned-eth-balance-is-zero?]]
   (and
    (or no-routes-found? upper-limit-exceeded?)
    (not-empty sender-network-values)
    (if (= token-symbol
           (string/upper-case
            constants/mainnet-short-name))
      upper-limit-equals-input-value?
      owned-eth-balance-is-zero?))))

(rf/reg-sub :send-input-amount-screen/show-no-routes?
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/routes-information]
 :<- [:send-input-amount-screen/not-enough-asset?]
 (fn [[upper-limit-exceeded?
       {:keys [sender-network-values no-routes-found?]}
       not-enough-asset?]]
   (and
    (or no-routes-found? upper-limit-exceeded?)
    (not-empty sender-network-values)
    (not not-enough-asset?))))

(comment
  (rf/sub [:send-input-amount-screen/not-enough-asset?])
  (rf/sub [:send-input-amount-screen/upper-limit-equals-input-value?])
  (:no-routes-found? (rf/sub [:send-input-amount-screen/routes-information]))
  (:sender-network-values (rf/sub [:send-input-amount-screen/currency-information]))
  (rf/sub [:send-input-amount-screen/not-enough-asset?]))


(rf/reg-sub :send-input-amount-screen/view-subs
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/upper-limit]
 :<- [:send-input-amount-screen/amount-in-crypto]
 :<- [:send-input-amount-screen/token-input-converted-value]
 :<- [:send-input-amount-screen/token-input-converted-value-prettified]
 :<- [:send-input-amount-screen/value-out-of-limits?]
 :<- [:send-input-amount-screen/upper-limit-prettified]
 :<- [:send-input-amount-screen/routes-information]
 :<- [:send-input-amount-screen/networks-information]
 :<- [:send-input-amount-screen/token-by-symbol]
 :<- [:send-input-amount-screen/recipient-gets-amount]
 :<- [:send-input-amount-screen/max-decimals]
 :<- [:send-input-amount-screen/fee-formatted]
 :<- [:send-input-amount-screen/from-enabled-networks]
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/should-try-again?]
 :<- [:wallet/current-viewing-account-address]
 :<- [:send-input-amount-screen/not-enough-asset?]
 :<- [:send-input-amount-screen/show-no-routes?]
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
             loading-routes?
             token-not-supported-in-receiver-networks?
             no-routes-found?]
      :as   routes-information}
     {:keys [token-networks
             receiver-networks
             sending-to-unpreferred-networks?]}
     token-by-symbol
     recipient-gets-amount
     max-decimals
     fee-formatted
     from-enabled-networks
     upper-limit-exceeded?
     should-try-again?
     current-address
     not-enough-asset?
     show-no-routes?]]
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
    :upper-limit-exceeded?                     upper-limit-exceeded?
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
    :token-by-symbol                           token-by-symbol
    :recipient-gets-amount                     recipient-gets-amount
    :max-decimals                              max-decimals
    :fee-formatted                             fee-formatted
    :sending-to-unpreferred-networks?          sending-to-unpreferred-networks?
    :no-routes-found?                          no-routes-found?
    :from-enabled-networks                     from-enabled-networks
    :should-try-again?                         should-try-again?
    :current-address                           current-address
    :not-enough-asset?                         not-enough-asset?
    :show-no-routes?                           show-no-routes?}))

(rf/reg-sub :send-input-amount-screen/token-input-subs
 :<- [:send-input-amount-screen/controller]
 :<- [:send-input-amount-screen/currency-information]
 :<- [:send-input-amount-screen/upper-limit]
 :<- [:send-input-amount-screen/amount-in-crypto]
 :<- [:send-input-amount-screen/token-input-converted-value]
 :<- [:send-input-amount-screen/token-input-converted-value-prettified]
 :<- [:send-input-amount-screen/value-out-of-limits?]
 :<- [:send-input-amount-screen/upper-limit-prettified]
 :<- [:send-input-amount-screen/routes-information]
 :<- [:send-input-amount-screen/networks-information]
 :<- [:send-input-amount-screen/token-by-symbol]
 :<- [:send-input-amount-screen/recipient-gets-amount]
 :<- [:send-input-amount-screen/max-decimals]
 :<- [:send-input-amount-screen/fee-formatted]
 :<- [:send-input-amount-screen/from-enabled-networks]
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/should-try-again?]
 :<- [:wallet/current-viewing-account-address]
 :<- [:send-input-amount-screen/not-enough-asset?]
 :<- [:send-input-amount-screen/show-no-routes?]
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
             loading-routes?
             token-not-supported-in-receiver-networks?
             no-routes-found?]
      :as   routes-information}
     {:keys [token-networks
             receiver-networks
             sending-to-unpreferred-networks?]}
     token-by-symbol
     recipient-gets-amount
     max-decimals
     fee-formatted
     from-enabled-networks
     upper-limit-exceeded?
     should-try-again?
     current-address
     not-enough-asset?
     show-no-routes?]]
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
    :upper-limit-exceeded?                     upper-limit-exceeded?
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
    :token-by-symbol                           token-by-symbol
    :recipient-gets-amount                     recipient-gets-amount
    :max-decimals                              max-decimals
    :fee-formatted                             fee-formatted
    :sending-to-unpreferred-networks?          sending-to-unpreferred-networks?
    :no-routes-found?                          no-routes-found?
    :from-enabled-networks                     from-enabled-networks
    :should-try-again?                         should-try-again?
    :current-address                           current-address
    :not-enough-asset?                         not-enough-asset?
    :show-no-routes?                           show-no-routes?}))



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
 (fn [{:keys [db]} [c max-decimals]]
   (let [input-value   (get-in db token-input-value-path)
         new-text      (str input-value c)
         regex-pattern (str "^\\d*\\.?\\d{0," max-decimals "}$")
         regex         (re-pattern regex-pattern)]
     (when (re-matches regex new-text)
       {:db (update-in db token-input-value-path #(controlled-input-logic/add-character % c))}))))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-last
 (fn [{:keys [db]}]
   {:db (update-in db token-input-value-path controlled-input-logic/delete-last)}))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-all
 (fn [{:keys [db]}]
   {:db (assoc-in db token-input-value-path "")}))


