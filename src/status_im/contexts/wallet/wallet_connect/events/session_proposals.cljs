(ns status-im.contexts.wallet.wallet-connect.events.session-proposals
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.sessions :as sessions]
            [status-im.contexts.wallet.wallet-connect.utils.uri :as uri]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

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
 :wallet-connect/on-scan-connection
 (fn [{:keys [db]} [scanned-text]]
   (let [network-status     (:network/status db)
         parsed-uri         (wallet-connect/parse-uri scanned-text)
         version            (:version parsed-uri)
         valid-wc-uri?      (uri/valid-wc-uri? parsed-uri)
         expired?           (-> parsed-uri
                                :expiryTimestamp
                                uri/timestamp-expired?)
         version-supported? (uri/version-supported? version)]
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

(rf/reg-event-fx
 :wallet-connect/on-session-proposal
 (fn [{:keys [db]} [proposal]]
   (log/info "Received Wallet Connect session proposal: " proposal)
   (let [accounts                         (get-in db [:wallet :accounts])
         current-viewing-address          (get-in db [:wallet :current-viewing-account-address])
         sessions                         (get db :wallet-connect/sessions)
         available-accounts               (sessions/filter-operable-accounts (vals accounts))
         latest-connected-account-address (sessions/latest-connected-account-address sessions)
         networks                         (networks/get-networks-by-mode db)
         session-networks                 (networks/proposal-networks-intersection proposal networks)
         required-networks-supported?     (networks/required-networks-supported? proposal networks)]
     (if (and (not-empty session-networks) required-networks-supported?)
       {:db (update db
                    :wallet-connect/current-proposal assoc
                    :request                         proposal
                    :session-networks                session-networks
                    :address                         (cond
                                                       (not (string/blank? current-viewing-address))
                                                       current-viewing-address

                                                       (not (string/blank?
                                                             latest-connected-account-address))
                                                       latest-connected-account-address

                                                       :else (-> available-accounts
                                                                 first
                                                                 :address)))
        :fx [[:dispatch [:open-modal :screen/wallet.wallet-connect-session-proposal]]]}
       {:fx [[:dispatch [:wallet-connect/show-session-networks-unsupported-toast proposal]]
             [:dispatch [:wallet-connect/reject-session-proposal proposal]]]}))))

(rf/reg-event-fx
 :wallet-connect/show-session-networks-unsupported-toast
 (fn [{:keys [db]} [proposal]]
   (let [{:keys [name url]} (data-store/get-session-dapp-metadata proposal)]
     {:fx [[:dispatch
            [:toasts/upsert
             {:type  :negative
              :theme (:theme db)
              :text  (i18n/label :t/wallet-connect-networks-not-supported
                                 {:dapp (data-store/compute-dapp-name name url)})}]]]})))

(rf/reg-event-fx
 :wallet-connect/reset-current-session-proposal
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-proposal)}))

(rf/reg-event-fx
 :wallet-connect/set-current-proposal-address
 (fn [{:keys [db]} [address]]
   {:db (assoc-in db [:wallet-connect/current-proposal :address] address)}))

(rf/reg-event-fx
 :wallet-connect/approve-session
 (fn [{:keys [db]}]
   (let [web3-wallet      (get db :wallet-connect/web3-wallet)
         current-proposal (get-in db [:wallet-connect/current-proposal :request])
         session-networks (->> (get-in db [:wallet-connect/current-proposal :session-networks])
                               (map networks/chain-id->eip155)
                               vec)
         current-address  (get-in db [:wallet-connect/current-proposal :address])
         network-status   (:network/status db)
         expired?         (-> current-proposal
                              (get-in [:params :expiryTimestamp])
                              uri/timestamp-expired?)]
     (if (= network-status :online)
       {:db (assoc-in db [:wallet-connect/current-proposal :response-sent?] true)
        :fx [(if expired?
               [:dispatch
                [:toasts/upsert
                 {:id   :wallet-connect-proposal-expired
                  :type :negative
                  :text (i18n/label :t/wallet-connect-proposal-expired)}]]
               [:effects.wallet-connect/approve-session
                {:web3-wallet      web3-wallet
                 :proposal-request current-proposal
                 :session-networks session-networks
                 :address          current-address
                 :on-success       [:wallet-connect/approve-session-success]
                 :on-fail          [:wallet-connect/approve-session-error]}])
             [:dispatch [:dismiss-modal :screen/wallet.wallet-connect-session-proposal]]]}
       {:fx [[:dispatch [:wallet-connect/no-internet-toast]]]}))))

(rf/reg-event-fx :wallet-connect/approve-session-success
 (fn [{:keys [db]} [session]]
   (log/info "Successfully approved WalletConnect session" session)
   (let [total-connected-dapps (data-store/get-total-connected-dapps db)
         dapp-name             (data-store/get-dapp-name session)]
     {:fx [[:dispatch [:wallet-connect/on-new-session session]]
           [:dispatch [:wallet-connect/reset-current-session-proposal]]
           [:dispatch [:wallet-connect/redirect-to-dapp (data-store/get-dapp-redirect-url session)]]
           [:dispatch
            [:toasts/upsert
             {:type :positive
              :text (i18n/label :t/wallet-connect-proposal-approved-toast {:dapp dapp-name})}]]
           [:dispatch
            [:centralized-metrics/track :metric/dapp-session-proposal
             {:action                :approved
              :total_connected_dapps total-connected-dapps}]]]})))

(rf/reg-event-fx :wallet-connect/approve-session-error
 (fn [_ [error]]
   (log/error "Wallet Connect session approval failed"
              {:error error
               :event :wallet-connect/approve-session})
   {:fx [[:dispatch [:wallet-connect/reset-current-session-proposal]]
         [:dispatch
          [:toasts/upsert
           {:type :negative
            :text (i18n/label :t/wallet-connect-something-went-wrong)}]]]}))

(rf/reg-event-fx
 :wallet-connect/reject-session-proposal
 (fn [{:keys [db]} [proposal]]
   (let [web3-wallet                      (get db :wallet-connect/web3-wallet)
         {:keys [request response-sent?]} (:wallet-connect/current-proposal db)
         networks                         (networks/get-proposal-networks (or proposal request))
         rejected?                        (nil? proposal)
         dapp-name                        (-> (data-store/get-session-dapp-metadata (or proposal
                                                                                        request))
                                              :name)]
     {:fx (concat
           (when-not response-sent?
             [[:effects.wallet-connect/reject-session-proposal
               {:web3-wallet web3-wallet
                :proposal    (or proposal request)
                :on-success  (fn []
                               (log/info "Wallet Connect session proposal rejected")
                               (rf/dispatch [:toasts/upsert
                                             {:type :positive
                                              :text (i18n/label :t/wallet-connect-proposal-rejected-toast
                                                                {:dapp dapp-name})}]))
                :on-error    #(log/error "Wallet Connect unable to reject session proposal")}]
              [:dispatch
               [:centralized-metrics/track :metric/dapp-session-proposal
                {:action   (if rejected? :rejected :not_supported)
                 :networks networks}]]])
           [[:dispatch [:wallet-connect/reset-current-session-proposal]]])})))
