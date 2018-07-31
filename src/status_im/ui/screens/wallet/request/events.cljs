(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet-db]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.chat.commands.core :as commands]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
 ::wallet-send-chat-request
 [re-frame/trim-v]
 (fn [{:keys [db] :as cofx} [asset amount]]
   (handlers-macro/merge-fx cofx
                            {:dispatch [:send-current-message]}
                            (commands/select-chat-input-command
                             (get-in db [:id->command ["request" #{:personal-chats}]]) [asset amount]))))

(handlers/register-handler-fx
 :wallet-send-request
 [re-frame/trim-v]
 (fn [_ [whisper-identity amount symbol decimals]]
   (assert whisper-identity)
   ;; TODO(janherich) remove this dispatch sequence, there is absolutely no need for that :/
   {:dispatch-n [[:navigate-back]
                 [:navigate-to-clean :home]
                 [:add-chat-loaded-event whisper-identity
                  [::wallet-send-chat-request (name symbol) (str (money/internal->formatted amount symbol decimals))]]
                 [:start-chat whisper-identity]]}))

(handlers/register-handler-fx
 :wallet.request/set-recipient
 (fn [{:keys [db]} [_ s]]
   {:db (assoc-in db [:wallet :request-transaction :to] s)}))

(handlers/register-handler-fx
 :wallet.request/set-and-validate-amount
 (fn [{:keys [db]} [_ amount symbol decimals]]
   (let [{:keys [value error]} (wallet-db/parse-amount amount decimals)]
     {:db (-> db
              (assoc-in [:wallet :request-transaction :amount] (money/formatted->internal value symbol decimals))
              (assoc-in [:wallet :request-transaction :amount-text] amount)
              (assoc-in [:wallet :request-transaction :amount-error] error))})))

(handlers/register-handler-fx
 :wallet.request/set-symbol
 (fn [{:keys [db]} [_ symbol]]
   {:db (-> db
            (assoc-in [:wallet :request-transaction :symbol] symbol))}))
