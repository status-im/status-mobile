(ns status-im.contexts.wallet.wallet-connect.events.sessions
  (:require [re-frame.core :as rf]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.sessions :as sessions]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

(rf/reg-event-fx
 :wallet-connect/on-session-delete
 (fn [{:keys [db]} [{:keys [topic] :as event}]]
   (when (networks/event-should-be-handled? db event)
     (log/info "Received Wallet Connect session delete from the SDK: " event)
     {:fx [[:json-rpc/call
            [{:method     "wallet_disconnectWalletConnectSession"
              :params     [topic]
              :on-success [:wallet-connect/delete-session topic]
              :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]})))

(rf/reg-event-fx :wallet-connect/disconnect-dapp-success
 (fn [{:keys [db]} [wallet-account topic dapp-name]]
   (log/info "Wallet Connect dApp session disconnected successfully")
   {:db (assoc db
               :centralized-metrics/event-data
               {:dapp_name dapp-name})
    :fx [[:dispatch
          [:toasts/upsert
           {:id   :dapp-disconnect-success
            :type :positive
            :text (i18n/label :t/disconnect-dapp-success
                              {:dapp    dapp-name
                               :account (:name wallet-account)})}]]
         [:dispatch [:wallet-connect/delete-session topic]]]}))

(rf/reg-event-fx :wallet-connect/disconnect-dapp-fail
 (fn [{:keys [db]} [wallet-account topic dapp-name]]
   (log/info "Wallet Connect dApp session disconnected failed")
   {:db (assoc db
               :centralized-metrics/event-data
               {:dapp_name dapp-name})
    :fx [[:dispatch
          [:toasts/upsert
           {:id   :dapp-disconnect-failure
            :type :negative
            :text (i18n/label :t/disconnect-dapp-fail
                              {:dapp    dapp-name
                               :account (:name wallet-account)})}]]
         [:dispatch [:wallet-connect/disconnect-persisted-session topic]]]}))

(rf/reg-event-fx
 :wallet-connect/disconnect-dapp
 (fn [{:keys [db]} [{:keys [wallet-account topic name]}]]
   (let [web3-wallet    (get db :wallet-connect/web3-wallet)
         network-status (:network/status db)]
     (log/info "Disconnecting dApp session" topic)
     (if (= network-status :online)
       {:fx [[:effects.wallet-connect/disconnect
              {:web3-wallet web3-wallet
               :topic       topic
               :on-fail     [:wallet-connect/disconnect-dapp-fail wallet-account topic name]
               :on-success  [:wallet-connect/disconnect-dapp-success wallet-account topic name]}]]}
       {:fx [[:dispatch [:wallet-connect/no-internet-toast]]]}))))

(rf/reg-event-fx
 :wallet-connect/get-sessions
 (fn [{:keys [db]}]
   (let [addresses (->> (get-in db [:wallet :accounts])
                        vals
                        sessions/filter-operable-accounts
                        (map :address))]
     (if (not (seq addresses))
       ;; NOTE: Re-trying to get active sessions if accounts weren't loaded yet during
       ;; initialization
       ((log/info "Re-trying to fetch active WalletConnect sessions")
        {:fx [[:dispatch-later [{:ms 500 :dispatch [:wallet-connect/get-sessions]}]]]})
       {:fx [[:effects.wallet-connect/get-sessions
              {:online?     (-> db :network/status (= :online))
               :web3-wallet (get db :wallet-connect/web3-wallet)
               :addresses   addresses
               :on-success  #(rf/dispatch [:wallet-connect/get-sessions-success %])
               :on-error    #(rf/dispatch [:wallet-connect/get-sessions-error %])}]]}))))

(rf/reg-event-fx
 :wallet-connect/get-sessions-success
 (fn [{:keys [db]} [sessions]]
   (log/info "WalletConnect sessions loaded successfully")
   {:db (assoc db :wallet-connect/sessions sessions)}))

(rf/reg-event-fx
 :wallet-connect/get-sessions-error
 (fn [_ [error]]
   (log/error "WalletConnect sessions failed to load" error)
   {:fx [[:dispatch
          [:toasts/upsert
           {:type :negative
            :text (i18n/label :t/wallet-connect-connections-error)}]]]}))

(rf/reg-event-fx
 :wallet-connect/on-new-session
 (fn [{:keys [db]} [new-session]]
   (let [[dapp-name networks]  (data-store/get-dapp-name-and-networks db new-session)
         total-connected-dapps (-> db
                                   :wallet-connect/sessions
                                   count
                                   inc)]
     {:db (-> db
              (update
               :wallet-connect/sessions
               (fn [sessions]
                 (->> new-session
                      sessions/sdk-session->db-session
                      (conj sessions))))
              (assoc :centralized-metrics/event-data
                     {:dapp_name             dapp-name
                      :networks              networks
                      :total_connected_dapps total-connected-dapps}))})))

(rf/reg-event-fx
 :wallet-connect/delete-session
 (fn [{:keys [db]} [topic]]
   {:db (update db
                :wallet-connect/sessions
                (fn [sessions]
                  (->> sessions
                       (remove #(= (:topic %) topic))
                       (into []))))}))
