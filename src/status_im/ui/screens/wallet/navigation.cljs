(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]))

(defmethod navigation/preload-data! :wallet
  [db _]
  (status-im.thread/dispatch [:update-wallet])
  (assoc-in db [:wallet :current-tab] 0))

(defmethod navigation/preload-data! :transactions-history
  [db _]
  (status-im.thread/dispatch [:update-transactions])
  db)

(def transaction-send-default
  (let [symbol :ETH]
    {:gas    (ethereum/estimate-gas symbol)
     :symbol symbol}))

(def transaction-request-default
  {:symbol :ETH})

(defmethod navigation/preload-data! :wallet-request-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (-> db
        (assoc-in [:wallet :request-transaction] transaction-request-default)
        (assoc-in [:wallet :send-transaction] transaction-send-default))))

(defmethod navigation/preload-data! :wallet-send-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (do
      (status-im.thread/dispatch [:wallet/update-gas-price])
      (assoc-in db [:wallet :send-transaction] transaction-send-default))))
