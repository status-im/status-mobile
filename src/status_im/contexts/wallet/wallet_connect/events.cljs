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
            [utils.i18n :as i18n]))

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
         [:effects.wallet-connect/fetch-pairings
          {:web3-wallet web3-wallet
           :on-fail     #(log/error "Failed to get dApp pairings" {:error %})
           :on-success  (fn [data]
                          (rf/dispatch [:wallet-connect/set-pairings
                                        (js->clj data :keywordize-keys true)]))}]]}))

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
             #(rf/dispatch [:wallet-connect/on-session-request %])]]]})))

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
         without-watched              (remove :watch-only? (vals accounts))
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
                    :address                         (-> without-watched
                                                         first
                                                         :address))
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
 (fn [_ [event]]
   (log/info "Received Wallet Connect session request: " event)
   {:fx [[:dispatch [:wallet-connect/process-session-request event]]]}))

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
 :wallet-connect/set-pairings
 (fn [{:keys [db]} [pairings]]
   {:db (assoc db :wallet-connect/pairings pairings)}))

(rf/reg-event-fx
 :wallet-connect/remove-pairing-by-topic
 (fn [{:keys [db]} [topic]]
   {:db (update db
                :wallet-connect/pairings
                (fn [pairings]
                  (remove #(= (:topic %) topic) pairings)))}))

(rf/reg-event-fx
 :wallet-connect/disconnect-dapp
 (fn [{:keys [db]} [{:keys [topic on-success on-fail]}]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/disconnect
            {:web3-wallet web3-wallet
             :topic       topic
             :on-fail     on-fail
             :on-success  on-success}]]})))

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
 :wallet-connect/fetch-active-sessions
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/fetch-active-sessions
            {:web3-wallet web3-wallet
             :on-fail     #(log/error "Failed to get active sessions" {:error %})
             :on-success  #(log/info "Got active sessions successfully" {:sessions %})}]]})))

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

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions-success
 (fn [{:keys [db]} [sessions]]
   {:db (assoc db :wallet-connect/persisted-sessions sessions)}))

(rf/reg-event-fx
 :wallet-connect/fetch-persisted-sessions
 (fn [_ _]
   {:fx [[:json-rpc/call
          [{:method     "wallet_getWalletConnectActiveSessions"
            ;; This is the activeSince timestamp to avoid expired sessions
            ;; 0 means, return everything
            :params     [0]
            :on-success [:wallet-connect/fetch-persisted-sessions-success]
            :on-error   #(log/info "Wallet Connect fetch persisted sessions failed" %)}]]]}))

(rf/reg-event-fx
 :wallet-connect/persist-session
 (fn [_ [session-info]]
   {:fx [[:json-rpc/call
          [{:method     "wallet_addWalletConnectSession"
            :params     [(js/JSON.stringify session-info)]
            :on-success #(log/info "Wallet Connect session persisted")
            :on-error   #(log/info "Wallet Connect session persistence failed" %)}]]]}))
