(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.ui.screens.wallet.main.views :as main]))

(defmethod navigation/preload-data! :wallet
  [db _]
  (re-frame/dispatch [:update-wallet (map :symbol (main/tokens-for (:network db)))])
  (assoc-in db [:wallet :current-tab] 0))

(defmethod navigation/preload-data! :transactions-history
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
    (update db :wallet dissoc :send-transaction)))
