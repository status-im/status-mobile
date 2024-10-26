(ns status-im.subs.wallet.screens.input-amount
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.money :as money]
    [utils.number :as number]
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

(rf/reg-sub :send-input-amount-screen/amount-in-crypto
 :<- [:send-input-amount-screen/state]
 :<- [:send-input-amount-screen/conversion-rate]
 :<- [:send-input-amount-screen/token-decimals]
 (fn [[{:keys [crypto-currency? input-value]}
       conversion-rate
       token-decimals]]
   (if crypto-currency?
     input-value
     (number/remove-trailing-zeroes
      (.toFixed (/ input-value conversion-rate) token-decimals)))))

(rf/reg-sub :send-input-amount-screen/recipient-gets-amount
 :<- [:send-input-amount-screen/conversion-rate]
 :<- [:wallet/total-amount-in-to-chains]
 :<- [:wallet/wallet-send-token]
 (fn [[conversion-rate
       total-amount-receiver
       {token-symbol :symbol}]]
   (utils/prettify-crypto-balance
    token-symbol
    total-amount-receiver
    conversion-rate)))

(rf/reg-sub :send-input-amount-screen/routes
 :<- [:wallet/wallet-send-suggested-routes]
 (fn [suggested-routes]
   (when suggested-routes
     (or (:best suggested-routes) []))))

(defn- every-network-value-is-zero?
  [sender-network-values]
  (every? (fn [{:keys [total-amount]}]
            (and
             total-amount
             (money/bignumber? total-amount)
             (money/equal-to total-amount
                             (money/bignumber "0"))))
          sender-network-values))

(rf/reg-sub :send-input-amount-screen/unsupported-token-in-receiver?
 :<- [:wallet/wallet-send-receiver-network-values]
 :<- [:wallet/wallet-send-tx-type]
 (fn [[receiver-network-values
       tx-type]]
   (and (not= tx-type :tx/bridge)
        (->> receiver-network-values
             (remove #(= (:type %) :add))
             (every? #(= (:type %) :not-available))))))

(rf/reg-sub :send-input-amount-screen/no-routes-found?
 :<- [:wallet/wallet-send-sender-network-values]
 :<- [:send-input-amount-screen/routes]
 :<- [:wallet/wallet-send-loading-suggested-routes?]
 :<- [:send-input-amount-screen/unsupported-token-in-receiver?]
 (fn [[sender-network-values
       routes
       loading-routes?
       unsupported-token-in-receiver?]]
   (and
    (every-network-value-is-zero? sender-network-values)
    (some? routes)
    (not loading-routes?)
    (not unsupported-token-in-receiver?))))

(rf/reg-sub :send-input-amount-screen/sending-to-unpreferred-networks?
 :<- [:wallet/wallet-send-receiver-networks]
 :<- [:wallet/wallet-send-receiver-preferred-networks]
 (fn [[receiver-networks
       receiver-preferred-networks]]
   (some (comp not (set receiver-preferred-networks)) receiver-networks)))

(defn- insufficient-asset-amount?
  [{:keys [token-symbol owned-eth-token upper-limit-equals-input-value? no-routes-found? limit-exceeded?
           sender-network-values enough-assets?]}]
  (let [eth-selected?   (= token-symbol (string/upper-case constants/mainnet-short-name))
        zero-owned-eth? (money/equal-to (:total-balance owned-eth-token) 0)
        exceeded-input? (if eth-selected?
                          upper-limit-equals-input-value?
                          zero-owned-eth?)]
    (and (or no-routes-found? limit-exceeded?)
         (seq sender-network-values)
         (or exceeded-input? (not enough-assets?)))))

(rf/reg-sub :send-input-amount-screen/not-enough-asset?
 :<- [:wallet/wallet-send-sender-network-values]
 :<- [:wallet/wallet-send-enough-assets?]
 :<- [:wallet/wallet-send-token]
 :<- [:send-input-amount-screen/upper-limit-equals-input-value?]
 :<- [:send-input-amount-screen/no-routes-found?]
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/enabled-from-chain-ids]
 (fn [[sender-network-values
       enough-assets?
       {token-symbol :symbol}
       upper-limit-equals-input-value?
       no-routes-found?
       limit-exceeded?
       enabled-from-chain-ids]]
   (let [owned-eth-token (rf/sub [:wallet/token-by-symbol
                                  (string/upper-case constants/mainnet-short-name)
                                  enabled-from-chain-ids])]
     (insufficient-asset-amount?
      {:enough-assets?                  enough-assets?
       :token-symbol                    token-symbol
       :owned-eth-token                 owned-eth-token
       :upper-limit-equals-input-value? upper-limit-equals-input-value?
       :no-routes-found?                no-routes-found?
       :limit-exceeded?                 limit-exceeded?
       :sender-network-values           sender-network-values}))))

(rf/reg-sub :send-input-amount-screen/should-try-again?
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/no-routes-found?]
 :<- [:send-input-amount-screen/not-enough-asset?]
 (fn [[limit-exceeded?
       no-routes-found?
       not-enough-asset?]]
   (and (not limit-exceeded?)
        no-routes-found?
        (not not-enough-asset?))))

(rf/reg-sub :send-input-amount-screen/show-no-routes?
 :<- [:send-input-amount-screen/upper-limit-exceeded?]
 :<- [:send-input-amount-screen/no-routes-found?]
 :<- [:send-input-amount-screen/not-enough-asset?]
 :<- [:wallet/wallet-send-sender-network-values]
 (fn [[limit-exceeded?
       no-routes-found?
       not-enough-asset?
       sender-network-values]]
   (and (or no-routes-found? limit-exceeded?)
        (not-empty sender-network-values)
        (not not-enough-asset?))))

(rf/reg-sub :send-input-amount-screen/confirm-disabled?
 :<- [:wallet/wallet-send-route]
 :<- [:send-input-amount-screen/valid-input?]
 (fn [[route
       valid-input?]]
   (or (nil? route)
       (empty? route)
       (not valid-input?))))

(defn- get-fee-formatted
  [route]
  (when-let [native-currency-symbol (-> route first :from :native-currency-symbol)]
    (rf/sub [:wallet/wallet-send-fee-fiat-formatted native-currency-symbol])))

(rf/reg-sub :send-input-amount-screen/fee-formatted
 :<- [:wallet/wallet-send-route]
 :<- [:send-input-amount-screen/confirm-disabled?]
 :<- [:send-input-amount-screen/not-enough-asset?]
 (fn [[route
       confirm-disabled?
       not-enough-asset?]]
   (when (or (not confirm-disabled?) not-enough-asset?)
     (get-fee-formatted route))))

(comment
  (inc 1)
  (rf/sub [:send-input-amount-screen/state])
  (rf/sub [:send-input-amount-screen/max-decimals])
  (rf/sub [:send-input-amount-screen/enabled-from-chain-ids])
  (rf/sub [:send-input-amount-screen/from-enabled-networks])
  (rf/sub [:send-input-amount-screen/token-by-symbol])
  (rf/sub [:view-id])
)
