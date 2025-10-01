(ns status-im.contexts.wallet.router.core
  (:require [schema.core :as schema]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.transaction-settings.core :as transaction-settings]
            [status-im.contexts.wallet.router.schema :as router.schema]
            [utils.money :as money]))

(defn- to-gwei
  [amount]
  (-> amount
      money/wei->gwei
      (money/with-precision constants/min-token-decimals-to-display)
      str))

(defn transaction-fee-mode
  [route]
  (-> route
      :tx-gas-fee-mode
      transaction-settings/gas-rate->tx-fee-mode))

(schema/=> transaction-fee-mode
  [:=>
   [:cat router.schema/?route]
   router.schema/?fee-modes])

(defn transaction-fees-by-mode
  [{:keys [from-chain suggested-levels-for-max-fees-per-gas suggested-non-eip-1559-fees]}]
  (let [eip-1559-enabled? (:eip-1559-enabled from-chain)
        {:keys [low medium high low-estimated-time
                medium-estimated-time high-estimated-time]}
        suggested-levels-for-max-fees-per-gas
        {:keys [gas-price estimated-time]} suggested-non-eip-1559-fees]
    (if eip-1559-enabled?
      {:tx-fee-mode/normal
       {:max-fees       (to-gwei low)
        :estimated-time low-estimated-time}

       :tx-fee-mode/fast
       {:max-fees       (to-gwei medium)
        :estimated-time medium-estimated-time}

       :tx-fee-mode/urgent
       {:max-fees       (to-gwei high)
        :estimated-time high-estimated-time}}
      ;; For non EIP-1559 transactions, we show only the fast (medium) mode
      {:tx-fee-mode/fast
       {:max-fees       (to-gwei gas-price)
        :estimated-time estimated-time}})))

(schema/=> transaction-fees-by-mode
  [:=>
   [:cat router.schema/?route]
   [:map-of
    router.schema/?fee-modes
    [:map {:closed? true}
     [:max-fees :string]
     [:estimated-time :int]]]])

(defn suggested-gas-price
  [{:keys [suggested-non-eip-1559-fees]}]
  (some-> suggested-non-eip-1559-fees
          :gas-price
          to-gwei))

(schema/=> suggested-gas-price
  [:=>
   [:cat router.schema/?route]
   [:maybe :string]])

(defn transaction-estimated-time
  "Get the transaction estimated time. If the time is unknown (0),
  fallback to the current fee-mode's estimated time."
  [route]
  (let [estimated-time (:tx-estimated-time route)]
    (if (= 0 estimated-time)
      (-> route
          transaction-fees-by-mode
          (get (transaction-fee-mode route))
          :estimated-time)
      estimated-time)))

(schema/=> transaction-estimated-time
  [:=>
   [:cat router.schema/?route]
   [:maybe :int]])

(defn approval-estimated-time
  "Get the transaction approval estimated time."
  [route]
  (:approval-estimated-time route))

(schema/=> approval-estimated-time
  [:=>
   [:cat router.schema/?route]
   [:maybe :int]])

(defn nonce-field
  "Extract nonce field encoded in hex to integer representation"
  [route field]
  (-> route
      field
      money/from-hex
      money/to-string
      js/parseInt))

(schema/=> nonce-field
  [:=>
   [:cat router.schema/?route :keyword]
   [:maybe :int]])

(defn transaction-gas-fees
  [route]
  (let [{:keys [tx-base-fee tx-priority-fee
                tx-l-1-fee tx-max-fees-per-gas
                suggested-min-priority-fee
                suggested-max-priority-fee
                current-base-fee tx-gas-price from-chain]} route]
    {:gas-price                  (to-gwei tx-gas-price)
     :eip-1559-enabled           (:eip-1559-enabled from-chain)
     :base-fee                   (to-gwei tx-base-fee)
     :network-base-fee           (to-gwei current-base-fee)
     :tx-priority-fee            (to-gwei tx-priority-fee)
     :l-1-gas-fee                (to-gwei tx-l-1-fee)
     :tx-max-fees-per-gas        (to-gwei tx-max-fees-per-gas)
     :suggested-min-priority-fee (to-gwei suggested-min-priority-fee)
     :suggested-max-priority-fee (to-gwei suggested-max-priority-fee)}))

(schema/=> transaction-gas-fees
  [:=>
   [:cat router.schema/?route]
   [:map {:closed? true}
    [:gas-price :string]
    [:eip-1559-enabled :boolean]
    [:base-fee :string]
    [:network-base-fee :string]
    [:tx-priority-fee :string]
    [:l-1-gas-fee :string]
    [:tx-max-fees-per-gas :string]
    [:suggested-min-priority-fee :string]
    [:suggested-max-priority-fee :string]]])
