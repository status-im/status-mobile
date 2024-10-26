(ns status-im.subs.wallet.screens.input-amount
  (:require
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(rf/reg-sub :send-input-amount-screen/state
 :<- [:wallet/wallet-screens]
 :-> :input-amount-screen)

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

(rf/reg-sub :send-input-amount-screen/token-by-symbol
 :<- [:send-input-amount-screen/enabled-from-chain-ids]
 :<- [:wallet/wallet-send-token]
 (fn [[enabled-from-chain-ids
       {token-symbol :symbol}]]
   (rf/sub [:wallet/token-by-symbol
            (str token-symbol)
            enabled-from-chain-ids])))

(rf/reg-sub :send-input-amount-screen/total-balance
 :<- [:send-input-amount-screen/token-by-symbol]
 (fn [{:keys [total-balance]}]
   total-balance))

(rf/reg-sub :send-input-amount-screen/conversion-rate
 :<- [:wallet/wallet-send-token]
 :<- [:profile/currency]
 (fn [[token
       currency]]
   (-> token
       :market-values-per-currency
       currency
       :price)))

(rf/reg-sub :send-input-amount-screen/usd-conversion-rate
 :<- [:wallet/wallet-send-token]
 (fn [token]
   (utils/token-usd-price token)))

(rf/reg-sub :send-input-amount-screen/token-decimals
 :<- [:wallet/wallet-send-token]
 (fn [token]
   (-> token
       utils/token-usd-price
       utils/one-cent-value
       utils/calc-max-crypto-decimals)))

(rf/reg-sub :send-input-amount-screen/max-decimals
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/token-decimals]
 (fn [[{:keys [crypto-currency?]}
       token-decimals]]
   (if crypto-currency? token-decimals 2)))

(rf/reg-sub :send-input-amount-screen/upper-limit
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/conversion-rate]
 :<- [:send-input-amount-screen/usd-conversion-rate]
 :<- [:send-input-amount-screen/total-balance]
 (fn [[{:keys [crypto-currency?]}
       conversion-rate
       usd-conversion-rate
       total-balance]]
   (if crypto-currency?
     (utils/cut-crypto-decimals-to-fit-usd-cents
      total-balance
      usd-conversion-rate)
     (utils/cut-fiat-balance-to-two-decimals
      (money/crypto->fiat total-balance conversion-rate)))))

(rf/reg-sub :send-input-amount-screen/upper-limit-prettified
 :<- [:send-input-amount-screen/state]
 :<- [:profile/currency-symbol]
 :<- [:wallet/wallet-send-token]
 :<- [:send-input-amount-screen/conversion-rate]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [crypto-currency?]}
       currency-symbol
       {token-symbol :symbol}
       conversion-rate
       upper-limit]]
   (if crypto-currency?
     (utils/prettify-crypto-balance
      (or (clj->js token-symbol) "")
      (money/bignumber upper-limit)
      conversion-rate)
     (utils/prettify-balance currency-symbol
                             (money/bignumber upper-limit)))
 ))

(rf/reg-sub :send-input-amount-screen/upper-limit-exceeded?
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [input-value]}
       upper-limit]]
   (controlled-input-logic/upper-limit-exceeded?
    input-value
    upper-limit)))

(rf/reg-sub :send-input-amount-screen/value-out-of-limits?
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [input-value]}
       upper-limit]]
   (controlled-input-logic/value-out-of-limits? input-value upper-limit 0)))

(rf/reg-sub :send-input-amount-screen/valid-input?
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/value-out-of-limits?]
 (fn [[{:keys [input-value]}
       value-out-of-limits?]]
   (not (or (controlled-input-logic/empty-value? input-value) value-out-of-limits?))))

(rf/reg-sub :send-input-amount-screen/upper-limit-equals-input-value?
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/upper-limit]
 (fn [[{:keys [input-value]}
       upper-limit]]
   (money/equal-to
    (money/bignumber input-value)
    (money/bignumber upper-limit))))


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
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/conversion-rate]
 (fn [[{:keys [crypto-currency? input-value]}
       conversion-rate]]
   (if crypto-currency?
     (crypto->fiat input-value conversion-rate)
     (fiat->crypto input-value conversion-rate))))



(comment
  (inc 1)
  (rf/sub [:send-input-amount-screen/state])
  (rf/sub [:send-input-amount-screen/max-decimals])
  (rf/sub [:send-input-amount-screen/enabled-from-chain-ids])
  (rf/sub [:send-input-amount-screen/from-enabled-networks])
  (rf/sub [:send-input-amount-screen/token-by-symbol])
  (rf/sub [:view-id])
)
