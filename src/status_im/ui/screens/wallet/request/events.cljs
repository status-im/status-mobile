(ns status-im.ui.screens.wallet.request.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.db :as wallet-db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.handlers :as handlers]
            [status-im.chat.models :as chat-model]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.contact.db :as contact.db]
            [status-im.utils.ethereum.core :as ethereum]))

(defn- find-address-name [db address]
  (:name (contact.db/find-contact-by-address (:contacts/contacts db) address)))

(defn- fill-request-details [db {:keys [address name value symbol gas gasPrice public-key from-chat?]}]
  {:pre [(not (nil? address))]}
  (let [name (or name (find-address-name db address))]
    (update-in
     db [:wallet :request-transaction]
     (fn [{old-symbol :symbol :as old-transaction}]
       (let [symbol-changed? (not= old-symbol symbol)]
         (cond-> (assoc old-transaction :to address :to-name name :public-key public-key)
           value (assoc :amount value)
           symbol (assoc :symbol symbol)
           (and gas symbol-changed?) (assoc :gas (money/bignumber gas))
           from-chat? (assoc :from-chat? from-chat?)
           (and gasPrice symbol-changed?)
           (assoc :gas-price (money/bignumber gasPrice))
           (and symbol (not gasPrice) symbol-changed?)
           (assoc :gas-price (ethereum/estimate-gas symbol))))))))

(handlers/register-handler-fx
 :wallet/fill-request-from-contact
 (fn [{db :db} [_ {:keys [address name public-key]}]]
   {:db         (fill-request-details db {:address address :name name :public-key public-key})
    :dispatch   [:navigate-back]}))

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
