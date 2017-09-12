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
