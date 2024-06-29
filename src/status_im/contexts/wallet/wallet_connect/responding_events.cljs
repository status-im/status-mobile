(ns status-im.contexts.wallet.wallet-connect.responding-events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [taoensso.timbre :as log]))

(rf/reg-event-fx
 :wallet-connect/respond-current-session
 (fn [{:keys [db]} [password]]
   (let [event  (get-in db [:wallet-connect/current-request :event])
         method (wallet-connect-core/get-request-method event)]
     {:fx [(condp = method
             constants/wallet-connect-personal-sign-method
             [:dispatch [:wallet-connect/respond-personal-sign password]]

             constants/wallet-connect-eth-send-transaction-method
             [:dispatch
              [:wallet-connect/respond-build-transaction
               #(rf/dispatch [:wallet-connect/respond-send-transaction-data password %])]]

             constants/wallet-connect-eth-sign-method
             [:dispatch [:wallet-connect/respond-eth-sign password]]

             constants/wallet-connect-eth-sign-transaction-method
             [:dispatch
              [:wallet-connect/respond-build-transaction
               #(rf/dispatch [:wallet-connect/respond-sign-transaction-data password %])]]

             constants/wallet-connect-eth-sign-typed-method
             [:dispatch [:wallet-connect/respond-sign-typed-data password :v1]]

             constants/wallet-connect-eth-sign-typed-v4-method
             [:dispatch [:wallet-connect/respond-sign-typed-data password :v4]])]})))

(rf/reg-event-fx
 :wallet-connect/respond-eth-sign
 (fn [{:keys [db]} [password]]
   (let [{:keys [address raw-data]} (get db :wallet-connect/current-request)]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       raw-data
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/send-response %])}]]})))


(rf/reg-event-fx
 :wallet-connect/respond-personal-sign
 (fn [{:keys [db]} [password]]
   (let [{:keys [address raw-data]} (get db :wallet-connect/current-request)]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       raw-data
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/send-response %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-typed-data
 (fn [{:keys [db]} [password typed-data-version]]
   (let [{:keys [address raw-data]} (get db :wallet-connect/current-request)]
     {:fx [[:effects.wallet-connect/sign-typed-data
            {:password   password
             :address    address
             :data       raw-data
             :version    typed-data-version
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/send-response %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-build-transaction
 (fn [{:keys [db]} [on-success]]
   (let [{:keys [raw-data]} (get db :wallet-connect/current-request)
         chain-id           (-> raw-data
                                (get-in [:params :chainId])
                                wallet-connect-core/eip155->chain-id)]
     {:fx [[:json-rpc/call
            [{:method     "wallet_buildTransaction"
              :params     [chain-id (js/JSON.stringify raw-data)]
              :on-success on-success
              :on-error   [:wallet-connect/on-sign-error]}]]]})))

(rf/reg-event-fx
 :wallet-connect/respond-send-transaction-data
 (fn [_ [password data]]
   (let [{:keys           [address]
          message-to-sign :messageToSign} data]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       message-to-sign
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/send-response %])}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-transaction-data
 (fn [_ [password data]]
   (let [{:keys           [address]
          message-to-sign :messageToSign} data]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       message-to-sign
             :on-error   #(rf/dispatch [:wallet-connect/on-sign-error %])
             :on-success #(rf/dispatch [:wallet-connect/send-response %])}]]})))

(rf/reg-event-fx
 :wallet-connect/on-sign-error
 (fn [{:keys [db]} [error]]
   (let [event                      (get-in db [:wallet-connect/current-request :event])
         {:keys [raw-data address]} (get db :wallet-connect/current-request)
         method                     (wallet-connect-core/get-request-method event)
         screen                     (wallet-connect-core/method-to-screen method)]
     (log/error "Failed to sign Wallet Connect request"
                {:error                error
                 :address              address
                 :sign-data            raw-data
                 :method               method
                 :wallet-connect-event event
                 :event                :wallet-connect/on-sign-error})
     {:fx [[:dispatch [:dismiss-modal screen]]
           [:dispatch [:wallet-connect/reset-current-request]]]})))


(rf/reg-event-fx
 :wallet-connect/send-response
 (fn [{:keys [db]} [result]]
   (let [{:keys [id topic] :as event} (get-in db [:wallet-connect/current-request :event])
         method                       (wallet-connect-core/get-request-method event)
         screen                       (wallet-connect-core/method-to-screen method)
         web3-wallet                  (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/respond-session-request
            {:web3-wallet web3-wallet
             :topic       topic
             :id          id
             :result      result
             :on-error    (fn [error]
                            (log/error "Failed to send Wallet Connect response"
                                       {:error                error
                                        :method               method
                                        :event                :wallet-connect/send-response
                                        :wallet-connect-event event})
                            (rf/dispatch [:dismiss-modal screen])
                            (rf/dispatch [:wallet-connect/reset-current-request]))
             :on-success  (fn []
                            (log/info "Successfully sent Wallet Connect response to dApp")
                            (rf/dispatch [:dismiss-modal screen])
                            (rf/dispatch [:wallet-connect/reset-current-request]))}]]})))
