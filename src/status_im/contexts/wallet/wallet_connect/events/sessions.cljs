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
   (let [session      (data-store/get-session-by-topic db topic)
         account-name (-> (data-store/get-account-by-session db session)
                          :name)]
     (when (networks/event-should-be-handled? db event)
       (log/info "Received Wallet Connect session delete from the SDK: " event)
       {:fx [[:json-rpc/call
              [{:method     "wallet_disconnectWalletConnectSession"
                :params     [topic]
                :on-success [:wallet-connect/delete-session topic]
                :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]
             [:dispatch
              [:toasts/upsert
               {:id   :dapp-disconnect-success
                :type :positive
                :text (i18n/label :t/disconnect-dapp-success
                                  {:dapp    (:name session)
                                   :account account-name})}]]
             [:dispatch
              [:centralized-metrics/track
               :metric/dapp-session-disconnected]]]}))))

(rf/reg-event-fx
 :wallet-connect/disconnect-dapp
 (fn [{:keys [db]} [{:keys [topic on-success on-fail]}]]
   (let [web3-wallet    (get db :wallet-connect/web3-wallet)
         network-status (:network/status db)]
     (log/info "Disconnecting dApp session" topic)
     (if (= network-status :online)
       {:fx [[:effects.wallet-connect/disconnect
              {:web3-wallet web3-wallet
               :topic       topic
               :on-fail     (fn []
                              (when on-fail
                                (on-fail)))
               :on-success  (fn []
                              (log/info "Successfully disconnected dApp session" topic)
                              (rf/dispatch [:wallet-connect/delete-session topic])
                              (rf/dispatch [:centralized-metrics/track
                                            :metric/dapp-session-disconnected])
                              (when on-success
                                (on-success)))}]]}
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
   {:db (update db
                :wallet-connect/sessions
                (fn [sessions]
                  (->> new-session
                       sessions/sdk-session->db-session
                       (conj sessions))))}))

(rf/reg-event-fx
 :wallet-connect/delete-session
 (fn [{:keys [db]} [topic]]
   {:db (update db
                :wallet-connect/sessions
                (fn [sessions]
                  (->> sessions
                       (remove #(= (:topic %) topic))
                       (into []))))}))
