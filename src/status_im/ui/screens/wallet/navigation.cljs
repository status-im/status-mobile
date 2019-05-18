(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]))

(def transaction-send-default
  (let [symbol :ETH]
    {:gas    (ethereum/estimate-gas symbol)
     :method constants/web3-send-transaction
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
      (re-frame/dispatch [:wallet/update-gas-price])
      (assoc-in db [:wallet :send-transaction] transaction-send-default))))

(defmethod navigation/preload-data! :wallet-add-custom-token
  [db [event]]
  (dissoc db :wallet/custom-token-screen))