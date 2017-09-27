(ns status-im.ui.screens.wallet.request.events
  (:require
    [status-im.utils.handlers :as handlers]
    [status-im.ui.screens.wallet.db :as wallet.db]))

(handlers/register-handler-fx
  :wallet-send-request
  (fn [{{:wallet/keys [request-transaction]} :db} [_ {:keys [whisper-identity] :as contact}]]
    {:dispatch-n [[:navigate-back]
                  [:navigate-to-clean :chat-list]
                  [:chat-with-command whisper-identity :request
                   {:contact contact
                    :amount (:amount request-transaction)}]]}))

(handlers/register-handler-fx
  :wallet-validate-request-amount
  (fn [{{:keys [web3] :wallet/keys [request-transaction] :as db} :db} _]
    (let [amount (:amount request-transaction)
          error (wallet.db/get-amount-validation-error amount web3)]
      {:db (assoc-in db [:wallet/request-transaction :amount-error] error)})))