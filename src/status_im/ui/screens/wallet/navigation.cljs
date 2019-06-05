(ns status-im.ui.screens.wallet.navigation
  (:require [status-im.constants :as constants]
            [status-im.ui.screens.navigation :as navigation]))

(def transaction-send-default
  {:method constants/web3-send-transaction
   :symbol :ETH})

(defmethod navigation/preload-data! :wallet-request-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (-> db
        (assoc-in [:wallet :request-transaction] {:symbol :ETH})
        (assoc-in [:wallet :send-transaction] transaction-send-default))))

(defmethod navigation/preload-data! :wallet-send-transaction
  [db [event]]
  (if (= event :navigate-back)
    db
    (assoc-in db [:wallet :send-transaction] transaction-send-default)))

(defmethod navigation/preload-data! :wallet-add-custom-token
  [db [event]]
  (dissoc db :wallet/custom-token-screen))
