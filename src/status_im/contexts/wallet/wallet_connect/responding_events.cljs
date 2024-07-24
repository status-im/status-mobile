(ns status-im.contexts.wallet.wallet-connect.responding-events
  (:require [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

(rf/reg-event-fx
 :wallet-connect/respond-current-session
 (fn [{:keys [db]} [password]]
   (let [event  (get-in db [:wallet-connect/current-request :event])
         method (wallet-connect-core/get-request-method event)]
     {:fx [(condp = method
             constants/wallet-connect-personal-sign-method
             [:dispatch [:wallet-connect/respond-sign-message password :personal-sign]]

             constants/wallet-connect-eth-sign-method
             [:dispatch [:wallet-connect/respond-sign-message password :eth-sign]]

             constants/wallet-connect-eth-send-transaction-method
             [:dispatch [:wallet-connect/respond-send-transaction-data password]]

             constants/wallet-connect-eth-sign-transaction-method
             [:dispatch [:wallet-connect/respond-sign-transaction-data password]]

             constants/wallet-connect-eth-sign-typed-method
             [:dispatch [:wallet-connect/respond-sign-typed-data password :v1]]

             constants/wallet-connect-eth-sign-typed-v4-method
             [:dispatch [:wallet-connect/respond-sign-typed-data password :v4]])]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-message
 (fn [{:keys [db]} [password rpc-method]]
   (let [{:keys [address raw-data]} (get db :wallet-connect/current-request)]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       raw-data
             :rpc-method rpc-method
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/finish-session-request %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-typed-data
 (fn [{:keys [db]} [password typed-data-version]]
   (let [{:keys [address raw-data event]} (get db :wallet-connect/current-request)
         chain-id                         (get-in event [:params :chainId])]
     {:fx [[:effects.wallet-connect/sign-typed-data
            {:password   password
             :address    address
             :data       raw-data
             :chain-id   chain-id
             :version    typed-data-version
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/finish-session-request %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-send-transaction-data
 (fn [{:keys [db]} [password]]
   (let [{:keys [chain-id raw-data address]} (get db :wallet-connect/current-request)
         {:keys [tx-hash tx-args]}           raw-data]
     {:fx [[:effects.wallet-connect/send-transaction
            {:password   password
             :address    address
             :chain-id   chain-id
             :tx-hash    tx-hash
             :tx-args    tx-args
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/finish-session-request %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-transaction-data
 (fn [{:keys [db]} [password]]
   (let [{:keys [chain-id raw-data address]} (get db :wallet-connect/current-request)
         {:keys [tx-hash tx-args]}           raw-data]
     {:fx [[:effects.wallet-connect/sign-transaction
            {:password   password
             :address    address
             :chain-id   chain-id
             :tx-hash    tx-hash
             :tx-params  tx-args
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/finish-session-request %])}]]})))

(rf/reg-event-fx
 :wallet-connect/on-sign-error
 (fn [{:keys [db]} [error]]
   (let [{:keys [raw-data address event]} (get db :wallet-connect/current-request)
         method                           (wallet-connect-core/get-request-method event)]
     (log/error "Failed to sign Wallet Connect request"
                {:error                error
                 :address              address
                 :sign-data            raw-data
                 :method               method
                 :wallet-connect-event event
                 :event                :wallet-connect/on-sign-error})
     {:fx [[:dispatch [:wallet-connect/reject-session-request]]
           [:dispatch [:wallet-connect/dismiss-request-modal]]]})))

(rf/reg-event-fx
 :wallet-connect/send-response
 (fn [{:keys [db]} [{:keys [result error on-success on-error]}]]
   (let [{:keys [id topic] :as event} (get-in db [:wallet-connect/current-request :event])
         method                       (wallet-connect-core/get-request-method event)
         web3-wallet                  (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/respond-session-request
            {:web3-wallet web3-wallet
             :topic       topic
             :id          id
             :result      result
             :error       error
             :on-error    (fn [error]
                            (when on-error
                              (on-error))
                            (log/error "Failed to send Wallet Connect response"
                                       {:error                error
                                        :method               method
                                        :event                :wallet-connect/send-response
                                        :wallet-connect-event event})
                            (rf/dispatch [:wallet-connect/reset-current-request]))
             :on-success  (fn []
                            (when on-success
                              (on-success))
                            (log/info "Successfully sent Wallet Connect response to dApp")
                            (rf/dispatch [:wallet-connect/reset-current-request]))}]]})))

(rf/reg-event-fx
 :wallet-connect/dismiss-request-modal
 (fn [{:keys [db]} _]
   (let [screen (-> db
                    (get-in [:wallet-connect/current-request :event])
                    wallet-connect-core/get-request-method
                    wallet-connect-core/method-to-screen)]
     {:fx [[:dispatch [:dismiss-modal screen]]]})))

(rf/reg-event-fx
 :wallet-connect/finish-session-request
 (fn [{:keys [db]} [result]]
   (let [event      (get-in db [:wallet-connect/current-request :event])
         method     (wallet-connect-core/get-request-method event)
         theme      (:theme db)
         toast-text (condp contains? method
                      constants/wallet-connect-message-signing-methods
                      (i18n/label :t/wallet-connect-message-request-success-toast)

                      constants/wallet-connect-transaction-methods
                      (i18n/label :t/wallet-connect-transaction-request-success-toast))]
     {:fx [[:dispatch
            [:wallet-connect/send-response
             {:result     result
              :on-success #(when toast-text
                             (rf/dispatch [:toasts/upsert
                                           {:type  :positive
                                            :text  toast-text
                                            :theme theme}]))}]]
           [:dispatch [:wallet-connect/dismiss-request-modal]]]})))

(rf/reg-event-fx
 :wallet-connect/reject-session-proposal
 (fn [{:keys [db]} _]
   (let [web3-wallet      (get db :wallet-connect/web3-wallet)
         current-proposal (get-in db [:wallet-connect/current-proposal :request])
         dapp-name        (-> (wallet-connect-core/get-session-dapp-metadata current-proposal)
                              :name)]
     {:fx [[:effects.wallet-connect/reject-session-proposal
            {:web3-wallet web3-wallet
             :proposal    current-proposal
             :on-success  #(log/info "Wallet Connect session proposal rejected")
             :on-error    #(log/error "Wallet Connect unable to reject session proposal")}]
           [:dispatch [:wallet-connect/reset-current-session-proposal]]
           [:dispatch
            [:toasts/upsert
             {:text  (i18n/label :t/wallet-connect-proposal-rejected-toast {:dapp dapp-name})
              :type  :positive
              :theme (:theme db)}]]]})))

;; NOTE: Currently we only reject a session if the user rejected it
;; But this needs to be solidified to ensure other cases:
;; - Unsupported WC version
;; - Invalid params from dapps
;; - Unsupported method
;; - Failed processing of request
;; - Failed "responding" (signing or sending message/transaction)
(rf/reg-event-fx
 :wallet-connect/reject-session-request
 (fn [{:keys [db]} _]
   (let [event              (get-in db [:wallet-connect/current-request :event])
         method             (wallet-connect-core/get-request-method event)
         theme              (:theme db)
         {:keys [name url]} (wallet-connect-core/get-session-by-topic db (:topic event))
         dapp-name          (wallet-connect-core/compute-dapp-name name url)
         toast-text         (condp contains? method
                              constants/wallet-connect-message-signing-methods
                              (i18n/label :t/wallet-connect-message-request-rejected-toast
                                          {:dapp dapp-name})

                              constants/wallet-connect-transaction-methods
                              (i18n/label :t/wallet-connect-transaction-request-rejected-toast
                                          {:dapp dapp-name}))]
     {:fx [[:dispatch
            [:wallet-connect/send-response
             {:error      (wallet-connect/get-sdk-error
                           constants/wallet-connect-user-rejected-error-key)
              :on-success #(when toast-text
                             (rf/dispatch [:toasts/upsert
                                           {:type  :positive
                                            :text  toast-text
                                            :theme theme}]))}]]]})))
