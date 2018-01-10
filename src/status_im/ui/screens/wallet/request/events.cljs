(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet-db]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.constants :as chat-const]
            [status-im.chat.events.input :as input-events]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
  ::wallet-send-chat-request
  [re-frame/trim-v]
  (fn [{{:contacts/keys [contacts] :as db} :db} [amount]]
    (-> db
        (input-events/select-chat-input-command
         (assoc (get-in contacts chat-const/request-command-ref) :prefill [amount]) nil true)
        (assoc :dispatch [:send-current-message]))))

(handlers/register-handler-fx
  :wallet-send-request
  [re-frame/trim-v]
  (fn [{{:keys [wallet]} :db} [{:keys [whisper-identity]}]]
    {:dispatch-n [[:navigate-back]
                  [:navigate-to-clean :home]
                  [:add-chat-loaded-event whisper-identity
                   [::wallet-send-chat-request (some-> wallet :request-transaction :amount money/wei->ether str)]]
                  [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
  :wallet.request/set-and-validate-amount
  (fn [{:keys [db]} [_ amount]]
    (let [{:keys [value error]} (wallet-db/parse-amount amount)]
      {:db (-> db
               (assoc-in [:wallet :request-transaction :amount] (money/ether->wei value))
               (assoc-in [:wallet :request-transaction :amount-error] error))})))

(handlers/register-handler-fx
  :wallet.request/set-symbol
  (fn [{:keys [db]} [_ s]]
    {:db (assoc-in db [:wallet :request-transaction :symbol] s)}))