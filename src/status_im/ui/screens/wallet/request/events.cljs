(ns status-im.ui.screens.wallet.request.events
  (:require
    [status-im.utils.handlers :as handlers]
    [status-im.ui.screens.wallet.db :as wallet.db]
    [re-frame.core :as re-frame]))

(defn chat-loaded-callback [amount]
  (fn []
    (re-frame/dispatch [:select-chat-input-command {:name "request" :prefill [amount]}])
    ;;TODO get rid of timeout
    (js/setTimeout #(re-frame/dispatch [:send-current-message]) 100)))

(handlers/register-handler-fx
  :wallet-send-request
  (fn [{{:wallet/keys [request-transaction]} :db} [_ {:keys [whisper-identity]}]]
    {:dispatch-n [[:navigate-back]
                  [:navigate-to-clean :chat-list]
                  [:add-chat-loaded-callback whisper-identity (chat-loaded-callback (:amount request-transaction))]
                  [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
  :wallet-validate-request-amount
  (fn [{{:wallet/keys [request-transaction] :as db} :db} _]
    (let [amount (:amount request-transaction)
          error (wallet.db/get-amount-validation-error amount)]
      {:db (assoc-in db [:wallet/request-transaction :amount-error] error)})))