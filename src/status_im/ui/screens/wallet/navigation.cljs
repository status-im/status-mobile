(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]))

(defmethod navigation/preload-data! :wallet
  [db _]
  (re-frame/dispatch [:update-wallet])
  (assoc-in db [:wallet :current-tab] 0))

(defmethod navigation/preload-data! :transactions-history
  [db _]
  (re-frame/dispatch [:update-transactions])
  db)

(def transaction-send-default
  (let [symbol :ETH]
    {:gas    (ethereum/estimate-gas symbol)
     :symbol symbol}))


(defmethod navigation/preload-data! :wallet-request-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (-> db
        (update :wallet dissoc :request-transaction)
        (assoc-in [:wallet :send-transaction] transaction-send-default))))

(defmethod navigation/preload-data! :wallet-send-transaction
  [db [event]]
  (re-frame/dispatch [:wallet/update-gas-price])
  (if (= event :navigate-back)
    db
    (assoc-in db [:wallet :send-transaction] transaction-send-default)))
