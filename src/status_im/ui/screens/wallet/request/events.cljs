(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]))

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
                  [:add-chat-loaded-event whisper-identity [::wallet-send-chat-request (str (:amount request-transaction))]]
                  [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
  :wallet.request/set-and-validate-amount
  (fn [{:keys [db]} [_ amount]]
    (let [{:keys [value error]} (wallet.db/parse-amount amount)]
      {:db (-> db
               (assoc-in [:wallet/request-transaction :amount] (money/ether->wei value))
               (assoc-in [:wallet/request-transaction :amount-error] error))})))
