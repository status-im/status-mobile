(ns status-im.chat.events.shortcuts
  (:require [status-im.ui.screens.wallet.send.events :as send.events]
            [status-im.ui.screens.wallet.choose-recipient.events :as choose-recipient.events]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [taoensso.timbre :as log]))

;; TODO(goranjovic) - update to include tokens in https://github.com/status-im/status-react/issues/3233
(defn- transaction-details [contact symbol]
  (-> contact
      (select-keys [:name :address :whisper-identity])
      (assoc :symbol symbol
             :gas (ethereum/estimate-gas symbol)
             :from-chat? true)))

(defn send-shortcut-fx [{:account/keys [account] :as db} contact params]
  (let [chain              (keyword (:chain db))
        symbol             (-> params :asset keyword)
        {:keys [decimals]} (tokens/asset-for chain symbol)]
    (merge {:db (-> db
                    (send.events/set-and-validate-amount-db (:amount params) symbol decimals)
                    (choose-recipient.events/fill-request-details (transaction-details contact symbol))
                    (update-in [:wallet :send-transaction] dissoc :id :password :wrong-password?)
                    (navigation/navigate-to
                     (if (:wallet-set-up-passed? account)
                       :wallet-send-transaction-chat
                       :wallet-onboarding-setup)))}
           (send.events/update-gas-price db false))))

(def shortcuts
  {"send" send-shortcut-fx})

(defn shortcut-override? [message]
  (get shortcuts (get-in message [:content :command])))

(defn shortcut-override-fx [db {:keys [chat-id content]}]
  (let [command              (:command content)
        contact              (get-in db [:contacts/contacts chat-id])
        shortcut-specific-fx (get shortcuts command)]
    (-> db
        (shortcut-specific-fx contact (:params content))
        ;; TODO(goranjovic) - replace this dispatch with a function call
        ;; Need to refactor chat events namespaces for this to avoid circular dependecy
        (assoc :dispatch [:cleanup-chat-command]))))
