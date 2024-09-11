(ns status-im.contexts.wallet.wallet-connect.events.session-proposals
  (:require [re-frame.core :as rf]
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
   (let [accounts                     (get-in db [:wallet :accounts])
         current-viewing-address      (get-in db [:wallet :current-viewing-account-address])
         available-accounts           (sessions/filter-operable-accounts (vals accounts))
         networks                     (networks/get-networks-by-mode db)
         session-networks             (networks/proposal-networks-intersection proposal networks)
         required-networks-supported? (networks/required-networks-supported? proposal networks)]
     (if (and (not-empty session-networks) required-networks-supported?)
       {:db (update db
                    :wallet-connect/current-proposal assoc
                    :request                         proposal
                    :session-networks                session-networks
                    :address                         (or current-viewing-address
                                                         (-> available-accounts
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
         accounts         (-> (partial networks/format-eip155-address current-address)
                              (map session-networks))
         network-status   (:network/status db)
         expiry           (get-in current-proposal [:params :expiryTimestamp])]
     (if (= network-status :online)
       {:db (assoc-in db [:wallet-connect/current-proposal :response-sent?] true)
        :fx [(if (uri/timestamp-expired? expiry)
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
