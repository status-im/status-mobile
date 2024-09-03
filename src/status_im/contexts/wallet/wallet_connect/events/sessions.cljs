(ns status-im.contexts.wallet.wallet-connect.events.sessions
  (:require [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.sessions :as sessions]
            [taoensso.timbre :as log]
            [utils.transforms :as types]))

(rf/reg-event-fx
 :wallet-connect/on-session-delete
 (fn [{:keys [db]} [{:keys [topic] :as event}]]
   (when (networks/event-should-be-handled? db event)
     (log/info "Received Wallet Connect session delete from the SDK: " event)
     {:fx [[:dispatch [:wallet-connect/disconnect-persisted-session topic]]]})))

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
               :reason      (wallet-connect/get-sdk-error
                             constants/wallet-connect-user-disconnected-reason-key)
               :on-fail     on-fail
               :on-success  (fn []
                              (rf/dispatch [:wallet-connect/disconnect-persisted-session topic])
                              (when on-success
                                (on-success)))}]]}
       {:fx [[:dispatch [:wallet-connect/no-internet-toast]]]}))))

;; We first load sessions from database, then we initiate a call to Wallet Connect SDK and
;; then replace the list we have stored in the database with the one that came from the SDK.
;; In addition to that, we also update the backend state by marking sessions that are not
;; active anymore by calling `:wallet-connect/disconnect-session`.
(rf/reg-event-fx
 :wallet-connect/fetch-active-sessions-success
 (fn [{:keys [db now]} [sessions]]
   (let [persisted-sessions (:wallet-connect/sessions db)
         account-addresses  (->> (get-in db [:wallet :accounts])
                                 vals
                                 sessions/filter-operable-accounts
                                 (map :address))
         sessions           (->> (js->clj sessions :keywordize-keys true)
                                 vals
                                 (map sessions/sdk-session->db-session)
                                 (sessions/filter-sessions-for-account-addresses
                                  account-addresses))
         session-topics     (set (map :topic sessions))
         expired-sessions   (filter
                             (fn [{:keys [expiry topic]}]
                               (or (< expiry (/ now 1000))
                                   (not (contains? session-topics topic))))
                             persisted-sessions)]
     (when (seq expired-sessions)
       (log/info "Updating WalletConnect persisted sessions due to expired/inactive sessions"
                 {:expired expired-sessions}))
     {:fx (mapv (fn [{:keys [topic]}]
                  [:dispatch [:wallet-connect/disconnect-persisted-session topic]])
                expired-sessions)
      :db (assoc db :wallet-connect/sessions sessions)})))

(rf/reg-event-fx
 :wallet-connect/fetch-active-sessions
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/fetch-active-sessions
            {:web3-wallet web3-wallet
             :on-fail     #(log/error "Failed to get active sessions" {:error %})
             :on-success  #(rf/dispatch [:wallet-connect/fetch-active-sessions-success %])}]]})))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions-success
 (fn [{:keys [db]} [sessions]]
   (let [network-status (:network/status db)
         sessions'      (mapv (fn [{:keys [sessionJson] :as session}]
                                (assoc session
                                       :accounts
                                       (-> sessionJson
                                           types/json->clj
                                           :namespaces
                                           :eip155
                                           :accounts)))
                              sessions)]
     {:fx [(when (= network-status :online)
             [:dispatch [:wallet-connect/fetch-active-sessions]])]
      :db (assoc db :wallet-connect/sessions sessions')})))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions-fail
 (fn [_ [error]]
   (log/info "Wallet Connect fetch persisted sessions failed" error)
   {:fx [[:dispatch [:wallet-connect/fetch-active-sessions]]]}))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions
 (fn [{:keys [now]} _]
   (let [current-timestamp (quot now 1000)]
     {:fx [[:json-rpc/call
            [{:method     "wallet_getWalletConnectActiveSessions"
              ;; NOTE: This is the activeSince timestamp to avoid expired sessions
              :params     [current-timestamp]
              :on-success [:wallet-connect/fetch-persisted-sessions-success]
              :on-error   [:wallet-connect/fetch-persisted-sessions-fail]}]]]})))

(rf/reg-event-fx
 :wallet-connect/persist-session
 (fn [_ [session-info]]
   (let [redirect-url (-> session-info
                          (js->clj :keywordize-keys true)
                          (data-store/get-dapp-redirect-url))]
     {:fx [[:json-rpc/call
            [{:method     "wallet_addWalletConnectSession"
              :params     [(js/JSON.stringify session-info)]
              :on-success (fn []
                            (log/info "Wallet Connect session persisted")
                            (rf/dispatch [:wallet-connect/fetch-persisted-sessions])
                            (rf/dispatch [:wallet-connect/redirect-to-dapp redirect-url]))
              :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]})))

(rf/reg-event-fx
 :wallet-connect/disconnect-persisted-session
 (fn [{:keys [db]} [topic]]
   (log/info "Removing session from persistance and state" topic)
   {:db (update db
                :wallet-connect/sessions
                (fn [sessions]
                  (->> sessions
                       (remove #(= (:topic %) topic))
                       (into []))))
    :fx [[:json-rpc/call
          [{:method     "wallet_disconnectWalletConnectSession"
            :params     [topic]
            :on-success #(log/info "Wallet Connect session disconnected")
            :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]}))
