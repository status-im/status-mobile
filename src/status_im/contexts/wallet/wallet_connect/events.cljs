(ns status-im.contexts.wallet.wallet-connect.events
  (:require [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            status-im.contexts.wallet.wallet-connect.effects
            status-im.contexts.wallet.wallet-connect.processing-events
            status-im.contexts.wallet.wallet-connect.responding-events
            [status-im.contexts.wallet.wallet-connect.utils :as wc-utils]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.transforms :as types]))

(rf/reg-event-fx
 :wallet-connect/init
 (fn [{:keys [db]}]
   (let [network-status       (:network/status db)
         web3-wallet-missing? (-> db :wallet-connect/web3-wallet boolean not)]
     (if (and (= network-status :online) web3-wallet-missing?)
       (do (log/info "Initialising WalletConnect SDK")
           {:fx [[:effects.wallet-connect/init
                  {:on-success #(rf/dispatch [:wallet-connect/on-init-success %])
                   :on-fail    #(rf/dispatch [:wallet-connect/on-init-fail %])}]]})
       ;; NOTE: when offline, fetching persistent sessions only
       {:fx [[:dispatch [:wallet-connect/fetch-persisted-sessions]]]}))))

(rf/reg-event-fx
 :wallet-connect/on-init-success
 (fn [{:keys [db]} [web3-wallet]]
   (log/info "WalletConnect SDK initialisation successful")
   {:db (assoc db :wallet-connect/web3-wallet web3-wallet)
    :fx [[:dispatch [:wallet-connect/register-event-listeners]]
         [:dispatch [:wallet-connect/fetch-persisted-sessions]]]}))

(rf/reg-event-fx
 :wallet-connect/reload-on-network-change
 (fn [{:keys [db]} [is-connected?]]
   (let [logged-in? (-> db :profile/profile boolean)]
     (when (and is-connected? logged-in?)
       (log/info "Re-Initialising WalletConnect SDK due to network change")
       {:fx [[:dispatch [:wallet-connect/init]]]}))))

(rf/reg-event-fx
 :wallet-connect/register-event-listeners
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-proposal-event
             #(rf/dispatch [:wallet-connect/on-session-proposal %])]]
           [:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-request-event
             #(rf/dispatch [:wallet-connect/on-session-request %])]]
           [:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-delete-event
             #(rf/dispatch [:wallet-connect/on-session-delete %])]]]})))

(rf/reg-event-fx
 :wallet-connect/on-init-fail
 (fn [_ [error]]
   (log/error "Failed to initialize Wallet Connect"
              {:error error
               :event :wallet-connect/on-init-fail})))

(rf/reg-event-fx
 :wallet-connect/on-session-proposal
 (fn [{:keys [db]} [proposal]]
   (log/info "Received Wallet Connect session proposal: " proposal)
   (let [accounts                     (get-in db [:wallet :accounts])
         current-viewing-address      (get-in db [:wallet :current-viewing-account-address])
         available-accounts           (wallet-connect-core/filter-operable-accounts (vals accounts))
         networks                     (wallet-connect-core/get-networks-by-mode db)
         session-networks             (wallet-connect-core/proposal-networks-intersection proposal
                                                                                          networks)
         required-networks-supported? (wallet-connect-core/required-networks-supported? proposal
                                                                                        networks)]
     (if (and (not-empty session-networks) required-networks-supported?)
       {:db (update db
                    :wallet-connect/current-proposal assoc
                    :request                         proposal
                    :session-networks                session-networks
                    :address                         (or current-viewing-address
                                                         (-> available-accounts
                                                             first
                                                             :address)))
        :fx [[:dispatch
              [:open-modal :screen/wallet.wallet-connect-session-proposal]]]}
       {:fx [[:dispatch
              [:wallet-connect/show-session-networks-unsupported-toast proposal]]
             [:dispatch
              [:wallet-connect/reject-session-proposal proposal]]]}))))

(rf/reg-event-fx
 :wallet-connect/show-session-networks-unsupported-toast
 (fn [{:keys [db]} [proposal]]
   (let [{:keys [name url]} (wallet-connect-core/get-session-dapp-metadata proposal)]
     {:fx [[:dispatch
            [:toasts/upsert
             {:type  :negative
              :theme (:theme db)
              :text  (i18n/label :t/wallet-connect-networks-not-supported
                                 {:dapp (wallet-connect-core/compute-dapp-name name url)})}]]]})))

(rf/reg-event-fx
 :wallet-connect/on-session-request
 (fn [{:keys [db]} [event]]
   (if (wallet-connect-core/event-should-be-handled? db event)
     {:fx [[:dispatch [:wallet-connect/process-session-request event]]]}
     {:fx [[:dispatch
            [:wallet-connect/send-response
             {:error (wallet-connect/get-sdk-error
                      constants/wallet-connect-user-rejected-chains-error-key)}]]]})))

(rf/reg-event-fx
 :wallet-connect/on-session-delete
 (fn [{:keys [db]} [{:keys [topic] :as event}]]
   (when (wallet-connect-core/event-should-be-handled? db event)
     (log/info "Received Wallet Connect session delete from the SDK: " event)
     {:fx [[:dispatch [:wallet-connect/disconnect-session topic]]]})))

(rf/reg-event-fx
 :wallet-connect/reset-current-session-proposal
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-proposal)}))

(rf/reg-event-fx
 :wallet-connect/set-current-proposal-address
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet-connect/current-proposal :address] address)}))

(rf/reg-event-fx
 :wallet-connect/reset-current-request
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-request)}))

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
                              (rf/dispatch [:wallet-connect/disconnect-session topic])
                              (when on-success
                                (on-success)))}]]}
       {:fx [[:dispatch [:wallet-connect/no-internet-toast]]]}))))

(rf/reg-event-fx
 :wallet-connect/pair
 (fn [{:keys [db]} [url]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/pair
            {:web3-wallet web3-wallet
             :url         url
             :on-fail     #(log/error "Failed to pair with dApp" {:error %})
             :on-success  #(log/info "dApp paired successfully")}]]})))

(rf/reg-event-fx
 :wallet-connect/approve-session
 (fn [{:keys [db]}]
   (let [web3-wallet      (get db :wallet-connect/web3-wallet)
         current-proposal (get-in db [:wallet-connect/current-proposal :request])
         session-networks (->> (get-in db [:wallet-connect/current-proposal :session-networks])
                               (map wallet-connect-core/chain-id->eip155)
                               vec)
         current-address  (get-in db [:wallet-connect/current-proposal :address])
         accounts         (-> (partial wallet-connect-core/format-eip155-address current-address)
                              (map session-networks))
         network-status   (:network/status db)
         expiry           (get-in current-proposal [:params :expiryTimestamp])]
     (if (= network-status :online)
       {:db (assoc-in db [:wallet-connect/current-proposal :response-sent?] true)
        :fx [(if (wc-utils/timestamp-expired? expiry)
               [:dispatch
                [:toasts/upsert
                 {:id   :wallet-connect-proposal-expired
                  :type :negative
                  :text (i18n/label :t/wallet-connect-proposal-expired)}]]
               [:effects.wallet-connect/approve-session
                {:web3-wallet web3-wallet
                 :proposal    current-proposal
                 :networks    session-networks
                 :accounts    accounts
                 :on-success  (fn [approved-session]
                                (log/info "Wallet Connect session approved")
                                (rf/dispatch [:wallet-connect/reset-current-session-proposal])
                                (rf/dispatch [:wallet-connect/persist-session
                                              approved-session]))
                 :on-fail     (fn [error]
                                (log/error "Wallet Connect session approval failed"
                                           {:error error
                                            :event :wallet-connect/approve-session})
                                (rf/dispatch
                                 [:wallet-connect/reset-current-session-proposal]))}])
             [:dispatch [:dismiss-modal :screen/wallet.wallet-connect-session-proposal]]]}
       {:fx [[:dispatch [:wallet-connect/no-internet-toast]]]}))))

(rf/reg-event-fx
 :wallet-connect/on-scan-connection
 (fn [{:keys [db]} [scanned-text]]
   (let [network-status     (:network/status db)
         parsed-uri         (wallet-connect/parse-uri scanned-text)
         version            (:version parsed-uri)
         valid-wc-uri?      (wc-utils/valid-wc-uri? parsed-uri)
         expired?           (-> parsed-uri
                                :expiryTimestamp
                                wc-utils/timestamp-expired?)
         version-supported? (wc-utils/version-supported? version)]
     (if (or (not valid-wc-uri?)
             (not version-supported?)
             (= network-status :offline)
             expired?)
       {:fx [[:dispatch
              [:toasts/upsert
               {:type  :negative
                :theme :dark
                :text  (cond (= network-status :offline)
                             (i18n/label :t/wallet-connect-no-internet-warning)

                             (not valid-wc-uri?)
                             (i18n/label :t/wallet-connect-wrong-qr)

                             expired?
                             (i18n/label :t/wallet-connect-qr-expired)

                             (not version-supported?)
                             (i18n/label :t/wallet-connect-version-not-supported
                                         {:version version})

                             :else
                             (i18n/label :t/something-went-wrong))}]]]}
       {:fx [[:dispatch [:wallet-connect/pair scanned-text]]]}))))

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
                                 wallet-connect-core/filter-operable-accounts
                                 (map :address))
         sessions           (->> (js->clj sessions :keywordize-keys true)
                                 vals
                                 (map wallet-connect-core/sdk-session->db-session)
                                 (wallet-connect-core/filter-sessions-for-account-addresses
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
                  [:dispatch [:wallet-connect/disconnect-session topic]])
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
   {:fx [[:json-rpc/call
          [{:method     "wallet_addWalletConnectSession"
            :params     [(js/JSON.stringify session-info)]
            :on-success [:wallet-connect/persist-session-success]
            :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]}))

(rf/reg-event-fx
 :wallet-connect/persist-session-success
 (fn [_ [session-info]]
   (let [redirect-url (-> session-info
                          (js->clj :keywordize-keys true)
                          (wallet-connect-core/get-dapp-redirect-url))]
     (log/info "Wallet Connect session persisted")
     {:fx [[:dispatch [:wallet-connect/fetch-active-sessions]]
           [:activity-center.notifications/fetch-unread-count]
           [:activity-center/update-seen-state]
           [[:dispatch [:wallet-connect/redirect-to-dapp redirect-url]]]]})))

(rf/reg-event-fx
 :wallet-connect/disconnect-session
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

(rf/reg-event-fx
 :wallet-connect/no-internet-toast
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:toasts/upsert
           {:type  :negative
            :theme (:theme db)
            :text  (i18n/label :t/wallet-connect-no-internet-warning)}]]]}))
