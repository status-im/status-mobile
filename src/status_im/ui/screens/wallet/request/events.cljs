(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet-db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models :as chat-model]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
 :wallet-send-request
 (fn [{:keys [db] :as cofx} [_ public-key amount symbol decimals]]
   (assert public-key)
   (let [request-command (get-in db [:id->command ["request" #{:personal-chats}]])]
     (fx/merge cofx
               (chat-model/start-chat public-key nil)
               (commands-sending/send public-key
                                      request-command
                                      {:asset  (name symbol)
                                       :amount (str (money/internal->formatted amount symbol decimals))})))))

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
