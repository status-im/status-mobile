(ns status-im.ui.screens.wallet.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.constants :as constants]
            [status-im.utils.utils :as utils]))

(defmethod navigation/preload-data! :wallet
  [db _]
  ;;TODO(goranjovic) - get rid of this preload hook completely
  ;;TODO(andrey) - temporary "improvement" with timeout, to fix screen rendering, wallet update should be optimized
  (utils/set-timeout (fn []
                       (re-frame/dispatch [:wallet.ui/pull-to-refresh])
                       (re-frame/dispatch [:update-wallet]))
                     500)
  (assoc-in db [:wallet :current-tab] 0))

(defmethod navigation/preload-data! :wallet-stack
  [db _]
  ;;TODO(goranjovic) - get rid of this preload hook completely
  ;;TODO(andrey) - temporary "improvement" with timeout, to fix screen rendering, wallet update should be optimized
  (utils/set-timeout (fn []
                       (re-frame/dispatch [:wallet.ui/pull-to-refresh])
                       (re-frame/dispatch [:update-wallet]))
                     500)
  (assoc-in db [:wallet :current-tab] 0))

(defmethod navigation/preload-data! :transactions-history
  [db _]
  (re-frame/dispatch [:update-transactions])
  db)

(defn transaction-send-default []
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
        (assoc-in [:wallet :send-transaction] (transaction-send-default)))))

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