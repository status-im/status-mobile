(ns status-im.contexts.wallet.wallet-connect.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            status-im.contexts.wallet.wallet-connect.effects
            [status-im.contexts.wallet.wallet-connect.utils :as wallet-connect.utils]
            [taoensso.timbre :as log]))

(rf/reg-event-fx
 :wallet-connect/init
 (fn []
   {:fx [[:effects.wallet-connect/init
          {:on-success #(rf/dispatch [:wallet-connect/on-init-success %])
           :on-fail    #(rf/dispatch [:wallet-connect/on-init-fail %])}]]}))

(rf/reg-event-fx
 :wallet-connect/on-init-success
 (fn [{:keys [db]} [web3-wallet]]
   {:db (assoc db :wallet-connect/web3-wallet web3-wallet)
    :fx [[:dispatch [:wallet-connect/register-event-listeners]]]}))

(rf/reg-event-fx
 :wallet-connect/register-event-listeners
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/register-event-listener
            web3-wallet
            constants/wallet-connect-session-proposal-event
            #(rf/dispatch [:wallet-connect/on-session-proposal %])]]})))

(rf/reg-event-fx
 :wallet-connect/on-init-fail
 (fn [error]
   (log/error "Failed to initialize Wallet Connect"
              {:error error
               :event :wallet-connect/on-init-fail})))

(rf/reg-event-fx
 :wallet-connect/on-session-proposal
 (fn [{:keys [db]} [proposal]]
   {:db (assoc db :wallet-connect/current-proposal proposal)}))

(rf/reg-event-fx
 :wallet-connect/pair
 (fn [{:keys [db]} [url]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/pair
            {:web3-wallet web3-wallet
             :url         url
             :on-fail     #(log/error "Failed to pair with dApp")
             :on-success  #(log/info "dApp paired successfully")}]]})))

(rf/reg-event-fx
 :wallet-connect/approve-session
 (fn [{:keys [db]}]
   ;; NOTE: hardcoding optimism for the base implementation
   (let [crosschain-ids       [constants/optimism-crosschain-id]
         web3-wallet          (get db :wallet-connect/web3-wallet)
         current-proposal     (get db :wallet-connect/current-proposal)
         accounts             (get-in db [:wallet :accounts])
         address              (-> accounts keys first)
         formatted-address    (wallet-connect.utils/format-address (first crosschain-ids)
                                                                   address)
         supported-namespaces (clj->js {:eip155
                                        {:chains   crosschain-ids
                                         :methods  constants/wallet-connect-supported-methods
                                         :events   constants/wallet-connect-supported-events
                                         :accounts [formatted-address]}})]
     {:fx [[:effects.wallet-connect/approve-session
            {:web3-wallet          web3-wallet
             :proposal             current-proposal
             :supported-namespaces supported-namespaces}]]})))
