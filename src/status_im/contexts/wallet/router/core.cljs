(ns status-im.contexts.wallet.router.core
  (:require [schema.core :as schema]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.router.schema :as router.schema]
            [status-im.contexts.wallet.send.transaction-settings.core :as transaction-settings]
            [utils.money :as money]))

(defn- to-gwei
  [amount]
  (-> amount
      money/wei->gwei
      (money/with-precision constants/min-token-decimals-to-display)
      str))

(defn transaction-fees-by-mode
  [route]
  (let [{:keys [low medium high low-estimated-time
                medium-estimated-time high-estimated-time]}
        (:suggested-levels-for-max-fees-per-gas route)]
    {:tx-fee-mode/normal
     {:max-fees       (to-gwei low)
      :estimated-time low-estimated-time}

     :tx-fee-mode/fast
     {:max-fees       (to-gwei medium)
      :estimated-time medium-estimated-time}

     :tx-fee-mode/urgent
     {:max-fees       (to-gwei high)
      :estimated-time high-estimated-time}}))

(schema/=> transaction-fees-by-mode
  [:=>
   [:cat router.schema/?route]
   [:map-of
    router.schema/?fee-modes
    [:map {:closed? true}
     [:max-fees :string]
     [:estimated-time :int]]]])

(defn transaction-fee-mode
  [route]
  (-> route
      :tx-gas-fee-mode
      transaction-settings/gas-rate->tx-fee-mode))

(schema/=> transaction-fee-mode
  [:=>
   [:cat router.schema/?route]
   router.schema/?fee-modes])

(defn transaction-estimated-time
  "Get the transaction estimated time. If the time is unknown (0),
  fallback to the current fee-mode's estimated time."
  [route]
  (let [estimated-time (:tx-estimated-time route)]
    (if (zero? estimated-time)
      (-> route
          transaction-fees-by-mode
          (get (transaction-fee-mode route))
          :estimated-time)
      estimated-time)))

(schema/=> transaction-estimated-time
  [:=>
   [:cat router.schema/?route]
   :int])

(defn approval-estimated-time
  "Get the transaction approval estimated time."
  [route]
  (:approval-estimated-time route))

(schema/=> approval-estimated-time
  [:=>
   [:cat router.schema/?route]
   [:maybe :int]])

(defn transaction-gas-fees
  [route]
  (let [{:keys [tx-base-fee tx-priority-fee
                tx-l-1-fee tx-max-fees-per-gas]} route]
    {:gas-price                "0"
     :eip-1559-enabled         true
     :base-fee                 (to-gwei tx-base-fee)
     :max-priority-fee-per-gas (to-gwei tx-priority-fee)
     :l-1-gas-fee              (to-gwei tx-l-1-fee)
     :tx-max-fees-per-gas      (to-gwei tx-max-fees-per-gas)}))

(schema/=> transaction-gas-fees
  [:=>
   [:cat router.schema/?route]
   [:map {:closed? true}
    [:gas-price :string]
    [:eip-1559-enabled :boolean]
    [:base-fee :string]
    [:max-priority-fee-per-gas :string]
    [:l-1-gas-fee :string]
    [:tx-max-fees-per-gas :string]]])
