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
 (fn []
   {:fx [[:effects.wallet-connect/init
          {:on-success #(rf/dispatch [:wallet-connect/on-init-success %])
           :on-fail    #(rf/dispatch [:wallet-connect/on-init-fail %])}]]}))

(rf/reg-event-fx
 :wallet-connect/on-init-success
 (fn [{:keys [db]} [web3-wallet]]
   {:db (assoc db :wallet-connect/web3-wallet web3-wallet)
    :fx [[:dispatch [:wallet-connect/register-event-listeners]]
         [:dispatch [:wallet-connect/fetch-persisted-sessions]]]}))

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
   (log/info "Received Wallet Connect session proposal: " {:id (:id proposal)})
   (let [accounts                     (get-in db [:wallet :accounts])
         current-viewing-address      (get-in db [:wallet :current-viewing-account-address])
         available-accounts           (filter #(and (:operable? %)
                                                    (not (:watch-only? %)))
                                              (vals accounts))
         networks                     (wallet-connect-core/get-networks-by-mode db)
         session-networks             (wallet-connect-core/proposal-networks-intersection proposal
                                                                                          networks)
         required-networks-supported? (wallet-connect-core/required-networks-supported? proposal
                                                                                        networks)]
     (if required-networks-supported?
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
              [:wallet-connect/session-networks-unsupported proposal]]]}))))

(rf/reg-event-fx
 :wallet-connect/session-networks-unsupported
 (fn [_ [proposal]]
   (let [{:keys [name]} (wallet-connect-core/get-session-dapp-metadata proposal)]
     {:fx [[:dispatch
            [:toasts/upsert
             {:type  :negative
              :theme :dark
              :text  (i18n/label :t/wallet-connect-networks-not-supported {:dapp name})}]]]})))

(rf/reg-event-fx
 :wallet-connect/on-session-request
 (fn [{:keys [db]} [event]]
   (when (wallet-connect-core/event-should-be-handled? db event)
     {:fx [[:dispatch [:wallet-connect/process-session-request event]]]})))

(rf/reg-event-fx
 :wallet-connect/on-session-delete
 (fn [{:keys [db]} [{:keys [topic] :as event}]]
   (when (wallet-connect-core/event-should-be-handled? db event)
     (log/info "Received Wallet Connect session delete: " event)
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
 (fn [{:keys [db]} [{:keys [pairing-topic on-success on-fail]}]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/disconnect
            {:web3-wallet web3-wallet
             :topic       pairing-topic
             :on-fail     on-fail
             :on-success  (fn []
                            (rf/dispatch [:wallet-connect/disconnect-session pairing-topic])
                            (when on-success
                              (on-success)))}]]})))

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
   (let [web3-wallet          (get db :wallet-connect/web3-wallet)
         current-proposal     (get-in db [:wallet-connect/current-proposal :request])
         session-networks     (->> (get-in db [:wallet-connect/current-proposal :session-networks])
                                   (map wallet-connect-core/chain-id->eip155)
                                   vec)
         current-address      (get-in db [:wallet-connect/current-proposal :address])
         accounts             (-> (partial wallet-connect-core/format-eip155-address current-address)
                                  (map session-networks))
         supported-namespaces (clj->js {:eip155
                                        {:chains   session-networks
                                         :methods  constants/wallet-connect-supported-methods
                                         :events   constants/wallet-connect-supported-events
                                         :accounts accounts}})]
     {:fx [[:effects.wallet-connect/approve-session
            {:web3-wallet          web3-wallet
             :proposal             current-proposal
             :supported-namespaces supported-namespaces
             :on-success           (fn [approved-session]
                                     (log/info "Wallet Connect session approved")
                                     (rf/dispatch [:wallet-connect/reset-current-session-proposal])
                                     (rf/dispatch [:wallet-connect/persist-session approved-session]))
             :on-fail              (fn [error]
                                     (log/error "Wallet Connect session approval failed"
                                                {:error error
                                                 :event :wallet-connect/approve-session})
                                     (rf/dispatch
                                      [:wallet-connect/reset-current-session-proposal]))}]
           [:dispatch [:dismiss-modal :screen/wallet.wallet-connect-session-proposal]]]})))

(rf/reg-event-fx
 :wallet-connect/on-scan-connection
 (fn [_ [scanned-text]]
   (let [parsed-uri         (wallet-connect/parse-uri scanned-text)
         version            (:version parsed-uri)
         valid-wc-uri?      (wc-utils/valid-wc-uri? parsed-uri)
         expired?           (-> parsed-uri
                                :expiryTimestamp
                                wc-utils/timestamp-expired?)
         version-supported? (wc-utils/version-supported? version)]
     (if (or (not valid-wc-uri?) expired? (not version-supported?))
       {:fx [[:dispatch
              [:toasts/upsert
               {:type  :negative
                :theme :dark
                :text  (cond (not valid-wc-uri?)
                             (i18n/label :t/wallet-connect-wrong-qr)

                             expired?
                             (i18n/label :t/wallet-connect-qr-expired)

                             (not version-supported?)
                             (i18n/label :t/wallet-connect-version-not-supported
                                         {:version version}))}]]]}
       {:fx [[:dispatch [:wallet-connect/pair scanned-text]]]}))))

;; We first load sessions from database, then we initiate a call to Wallet Connect SDK and
;; then replace the list we have stored in the database with the one that came from the SDK.
;; In addition to that, we also update the backend state by marking sessions that are not
;; active anymore by calling `:wallet-connect/disconnect-session`.
(rf/reg-event-fx
 :wallet-connect/fetch-active-sessions-success
 (fn [{:keys [db now]} [sessions]]
   (let [persisted-sessions (:wallet-connect/sessions db)
         sessions           (->> (js->clj sessions :keywordize-keys true)
                                 vals
                                 (map wallet-connect-core/sdk-session->db-session))
         expired-sessions   (remove
                             (fn [{:keys [expiry]}]
                               (> expiry (/ now 1000)))
                             persisted-sessions)]
     {:fx (mapv (fn [{:keys [pairingTopic]}]
                  [:wallet-connect/disconnect-session pairingTopic])
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
   (let [sessions' (mapv (fn [{:keys [sessionJson] :as session}]
                           (assoc session
                                  :accounts
                                  (-> sessionJson
                                      types/json->clj
                                      :namespaces
                                      :eip155
                                      :accounts)))
                         sessions)]
     {:fx [[:dispatch [:wallet-connect/fetch-active-sessions]]]
      :db (assoc db :wallet-connect/sessions sessions')})))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions-fail
 (fn [_ [error]]
   (log/info "Wallet Connect fetch persisted sessions failed" error)
   {:fx [[:dispatch [:wallet-connect/fetch-active-sessions]]]}))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions
 (fn [_ _]
   {:fx [[:json-rpc/call
          [{:method     "wallet_getWalletConnectActiveSessions"
            ;; This is the activeSince timestamp to avoid expired sessions
            ;; 0 means, return everything
            :params     [0]
            :on-success [:wallet-connect/fetch-persisted-sessions-success]
            :on-error   [:wallet-connect/fetch-persisted-sessions-fail]}]]]}))

(rf/reg-event-fx
 :wallet-connect/persist-session
 (fn [_ [session-info]]
   {:fx [[:json-rpc/call
          [{:method     "wallet_addWalletConnectSession"
            :params     [(js/JSON.stringify session-info)]
            :on-success (fn []
                          (log/info "Wallet Connect session persisted")
                          (rf/dispatch [:wallet-connect/fetch-persisted-sessions]))
            :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]}))

(rf/reg-event-fx
 :wallet-connect/disconnect-session
 (fn [{:keys [db]} [pairing-topic]]
   {:db (update db
                :wallet-connect/sessions
                (fn [sessions]
                  (->> sessions
                       (remove #(= (:pairingTopic %) pairing-topic))
                       (into []))))
    :fx [[:json-rpc/call
          [{:method     "wallet_disconnectWalletConnectSession"
            :params     [pairing-topic]
            :on-success #(log/info "Wallet Connect session disconnected")
            :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]}))
