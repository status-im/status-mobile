(ns status-im.ui.screens.wallet.request.events
  (:require
    [status-im.utils.handlers :as handlers]
    [status-im.ui.screens.wallet.db :as wallet.db]
    [re-frame.core :as re-frame]))

(handlers/register-handler-fx
  ::wallet-send-chat-request
  (fn [_ [_ amount]] 
    {:dispatch       [:select-chat-input-command {:name "request" :prefill [amount]}]
     ;; TODO get rid of the timeout
     :dispatch-later [{:ms 100 :dispatch [:send-current-message]}]}))

(handlers/register-handler-fx
  :wallet-send-request
  (fn [{{:wallet/keys [request-transaction]} :db} [_ {:keys [whisper-identity]}]]
    {:dispatch-n [[:navigate-back]
                  [:navigate-to-clean :chat-list]
                  [:add-chat-loaded-event whisper-identity [::wallet-send-chat-request (:amount request-transaction)]]
                  [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
  :wallet-validate-request-amount
  (fn [{{:wallet/keys [request-transaction] :as db} :db} _]
    (let [amount (:amount request-transaction)
          error (wallet.db/get-amount-validation-error amount)]
      {:db (assoc-in db [:wallet/request-transaction :amount-error] error)})))
