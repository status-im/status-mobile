(ns status-im.ui.screens.offline-messaging-settings.events
  (:require [re-frame.core :refer [reg-fx dispatch]]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.erc20 :as erc20]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]))

(handlers/register-handler-fx
 ::save-wnode
 (fn [{:keys [db now] :as cofx} [_ chain wnode]]
   (let [settings (get-in db [:account/account :settings])]
     (handlers-macro/merge-fx cofx
                              {:dispatch-n [[:load-accounts]
                                            [:navigate-to-clean :accounts]]}
                              (accounts-events/update-settings (assoc-in settings [:wnode chain] wnode))))))

(handlers/register-handler-fx
 ::save-wnode-transaction
 (fn [{:keys [db] :as cofx} [_ chain wnode hash]]
   (let [settings    (get-in db [:account/account :settings])
         transaction (get-in db [:wallet :transactions hash])]
     (when (seq transaction)
       (handlers-macro/merge-fx cofx
                                {:dispatch [::save-wnode chain wnode]}
                                (accounts-events/update-settings
                                 (assoc-in settings [:wnode-payment chain wnode] hash)))))))

(reg-fx
 ::send-token-transaction
 (fn [{:keys [web3 contract from address amount gas gas-price on-sent] :as haha}]
   (erc20/transfer web3
                   contract
                   from
                   address
                   amount
                   {:gas gas :gasPrice gas-price}
                   on-sent)))

(handlers/register-handler-fx
 ::pay-wnode
 (fn [{{:keys [web3] :as db} :db} [_ chain wnode]]
   (let [{:keys [address amount symbol]} (get-in db [:inbox/wnodes chain wnode :payment])]
     {::send-token-transaction {:web3      web3
                                :address   address
                                :amount    amount
                                :contract  (:address (tokens/symbol->token chain symbol))
                                :from      (get-in db [:account/account :address])
                                :gas       (ethereum/estimate-gas symbol)
                                :gas-price (money/->wei :gwei 5)
                                :on-sent   #(dispatch [::save-wnode-transaction chain wnode %])}})))

(handlers/register-handler-fx
 ::select-wnode
 (fn [{:keys [db]} [_ chain wnode]]
   (let [payment?    (contains? (get-in db [:inbox/wnodes chain wnode]) :payment)
         payment-tx? (contains? (get-in db [:account/account :settings :wnode-payment chain]) wnode)]
     {:dispatch (if (and payment? (not payment-tx?))
                  [::pay-wnode chain wnode]
                  [::save-wnode chain wnode])})))

(handlers/register-handler-fx
 :connect-wnode
 (fn [{:keys [db]} [_ wnode]]
   (let [network (get (:networks (:account/account db)) (:network db))
         chain   (ethereum/network->chain-keyword network)]
     {:show-confirmation {:title               (i18n/label :t/close-app-title)
                          :content             (i18n/label :t/connect-wnode-content
                                                           {:name (get-in db [:inbox/wnodes chain wnode :name])})
                          :confirm-button-text (i18n/label :t/close-app-button)
                          :on-accept           #(dispatch [::select-wnode chain wnode])
                          :on-cancel           nil}})))
