(ns status-im.contexts.wallet.routes.hooks
  (:require [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn- use-previous
  [value]
  (let [ref-value (rn/use-ref-atom value)]
    (rn/use-effect (fn [] (reset! ref-value value)) [value])
    @ref-value))

(defn use-transactions-built
  [cb]
  (let [route-state              (rf/sub [:wallet.routes/state])
        previous-route-state     (use-previous route-state)
        transactions-for-signing (rf/sub [:wallet.routes/transactions-for-signing])]
    (rn/use-effect (fn []
                     (when (and (= :transactions-built route-state)
                                (not= route-state previous-route-state))
                       (apply cb [transactions-for-signing])))
                   [route-state])))

(defn use-transactions-sent
  [cb]
  (let [route-state          (rf/sub [:wallet.routes/state])
        previous-route-state (use-previous route-state)
        transactions         (rf/sub [:wallet.routes/transactions])]
    (rn/use-effect (fn []
                     (when (and (= :transactions-sent route-state)
                                (not= route-state previous-route-state))
                       (apply cb [transactions])))
                   [route-state])))
