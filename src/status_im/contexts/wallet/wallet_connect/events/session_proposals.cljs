(ns status-im.contexts.wallet.wallet-connect.events.session-proposals
  (:require [clojure.string :as string]
            [re-frame.core :as rf]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.common.utils.account :as account-utils]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.proposal :as session-proposal]
            [status-im.contexts.wallet.wallet-connect.utils.session :as sessions]
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
   (let [network-status (:network/status db)
         parsed-uri     (uri/parse scanned-text)
         error-toast    (cond
                          (= network-status :offline)
                          (i18n/label :t/wallet-connect-no-internet-warning)

                          (not (uri/valid? parsed-uri))
                          (i18n/label :t/wallet-connect-wrong-qr)

                          (uri/expired? parsed-uri)
                          (i18n/label :t/wallet-connect-qr-expired)

                          (not (uri/version-supported? parsed-uri))
                          (i18n/label :t/wallet-connect-version-not-supported
                                      {:version (:version parsed-uri)}))]
     (if error-toast
       {:fx [[:dispatch
              [:toasts/upsert
               {:type  :negative
                :theme :dark
                :text  error-toast}]]]}
       {:fx [[:dispatch [:wallet-connect/pair scanned-text]]]}))))

(rf/reg-event-fx
 :wallet-connect/on-session-proposal
 (fn [{:keys [db]} [proposal]]
   (log/info "Received Wallet Connect session proposal: " proposal)
   (let [accounts                         (get-in db [:wallet :accounts])
         current-viewing-address          (get-in db [:wallet :current-viewing-account-address])
         sessions                         (get db :wallet-connect/sessions)
         available-accounts               (-> accounts vals account-utils/filter-operable)
         latest-connected-account-address (sessions/latest-connected-account-address sessions)
         networks                         (networks/get-networks-by-mode db)
         session-networks                 (session-proposal/networks-intersection proposal networks)
         required-networks-supported?     (session-proposal/required-networks-supported? proposal
                                                                                         networks)]
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
   {:fx [[:dispatch
          [:toasts/upsert
           {:type  :negative
            :theme (:theme db)
            :text  (i18n/label :t/wallet-connect-networks-not-supported
                               {:dapp (session-proposal/dapp-name proposal)})}]]]}))

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
         network-status   (:network/status db)]
     (if (= network-status :online)
       {:db (assoc-in db [:wallet-connect/current-proposal :response-sent?] true)
        :fx [(if (session-proposal/expired? current-proposal)
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
         networks                         (session-proposal/networks (or proposal request))
         dapp-name                        (session-proposal/dapp-name (or proposal request))
         rejected?                        (nil? proposal)]
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
