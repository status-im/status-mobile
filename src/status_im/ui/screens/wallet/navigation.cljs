(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]))

(defmethod navigation/preload-data! :wallet
  [db _]
  (re-frame/dispatch [:update-wallet])
  db)

(defmethod navigation/preload-data! :wallet-transactions
  [db _]
  (re-frame/dispatch [:update-transactions])
  db)

(defmethod navigation/preload-data! :wallet-request-transaction
  [db _]
  (dissoc db :wallet/request-transaction))

(defmethod navigation/preload-data! :wallet-send-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (dissoc db :wallet/send-transaction)))

(defmethod navigation/preload-data! :wallet-send-transaction-modal
  [db [_ _ value]]
  (assoc db :wallet/send-transaction value))