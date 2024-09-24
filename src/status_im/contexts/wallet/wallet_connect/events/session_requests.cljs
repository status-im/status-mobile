(ns status-im.contexts.wallet.wallet-connect.events.session-requests
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [status-im.contexts.wallet.wallet-connect.utils.signing :as signing]
            [status-im.contexts.wallet.wallet-connect.utils.transactions :as transactions]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet-connect/show-request-modal
 (fn [{:keys [db]}]
   (let [event  (get-in db [:wallet-connect/current-request :event])
         method (data-store/get-request-method event)
         screen (data-store/method-to-screen method)]
     (if screen
       {:fx [[:dispatch [:open-modal screen]]]}
       (log/error "Didn't find screen for Wallet Connect method"
                  {:method method
                   :event  :wallet-connect/process-session-request})))))
(rf/reg-event-fx
 :wallet-connect/process-session-request
 (fn [{:keys [db]} [event]]
   (let [method         (data-store/get-request-method event)
         existing-event (get-in db [:wallet-connect/current-request :event])]
     (log/info "Processing Wallet Connect session request" method)
     ;; NOTE: make sure we don't show two requests at the same time
     (when-not existing-event
       {:db (-> db
                (assoc-in [:wallet-connect/current-request :event] event)
                (assoc-in [:wallet-connect/current-request :response-sent?] false))
        :fx [(condp = method
               constants/wallet-connect-eth-send-transaction-method
               [:dispatch
                [:wallet-connect/process-eth-send-transaction
                 {:on-success (fn [] (rf/dispatch [:wallet-connect/show-request-modal]))}]]

               constants/wallet-connect-eth-sign-method
               [:dispatch [:wallet-connect/process-eth-sign]]

               constants/wallet-connect-eth-sign-transaction-method
               [:dispatch [:wallet-connect/process-eth-sign-transaction]]

               constants/wallet-connect-eth-sign-typed-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-eth-sign-typed-v4-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-personal-sign-method
               [:dispatch [:wallet-connect/process-personal-sign]])]}))))

(rf/reg-event-fx
 :wallet-connect/process-personal-sign
 (fn [{:keys [db]}]
   (let [[raw-data address] (data-store/get-db-current-request-params db)
         parsed-data        (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      (string/lower-case address)
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))
      :fx [[:dispatch [:wallet-connect/show-request-modal]]]})))

(rf/reg-event-fx
 :wallet-connect/process-eth-sign
 (fn [{:keys [db]}]
   (let [[address raw-data] (data-store/get-db-current-request-params db)
         parsed-data        (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      (string/lower-case address)
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))
      :fx [[:dispatch [:wallet-connect/show-request-modal]]]})))

(rf/reg-event-fx
 :wallet-connect/prepare-transaction-success
 (fn [{:keys [db]} [prepared-tx chain-id]]
   (let [{:keys [tx-args]} prepared-tx
         tx                (bean/->clj tx-args)
         address           (-> tx :from string/lower-case)
         display-data      (transactions/beautify-transaction tx)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     prepared-tx
                     :transaction  tx
                     :chain-id     chain-id
                     :display-data display-data)})))

(rf/reg-event-fx
 :wallet-connect/process-eth-send-transaction
 (fn [{:keys [db]} [{:keys [on-success]}]]
   (let [event    (data-store/get-db-current-request-event db)
         tx       (-> event data-store/get-request-params first)
         chain-id (-> event
                      (get-in [:params :chainId])
                      networks/eip155->chain-id)]
     (when tx
       {:fx [[:effects.wallet-connect/prepare-transaction
              {:tx         tx
               :chain-id   chain-id
               :on-success (fn [data]
                             (rf/dispatch [:wallet-connect/prepare-transaction-success data chain-id])
                             (when on-success
                               (rf/call-continuation on-success)))
               :on-error   #(rf/dispatch [:wallet-connect/on-processing-error %])}]]}))))

(rf/reg-event-fx
 :wallet-connect/process-eth-sign-transaction
 (fn [{:keys [db]}]
   (let [event    (data-store/get-db-current-request-event db)
         tx       (-> event data-store/get-request-params first)
         chain-id (-> event
                      (get-in [:params :chainId])
                      networks/eip155->chain-id)]
     {:fx [[:effects.wallet-connect/prepare-transaction
            {:tx         tx
             :chain-id   chain-id
             :on-success #(rf/dispatch [:wallet-connect/prepare-transaction-success % chain-id])
             :on-error   #(rf/dispatch [:wallet-connect/on-processing-error %])}]]})))

(rf/reg-event-fx
 :wallet-connect/process-sign-typed
 (fn [{:keys [db]}]
   (try
     (let [[address raw-data] (data-store/get-db-current-request-params db)
           parsed-raw-data    (transforms/js-parse raw-data)
           session-chain-id   (-> (data-store/get-db-current-request-event db)
                                  (get-in [:params :chainId])
                                  networks/eip155->chain-id)
           data-chain-id      (-> parsed-raw-data
                                  transforms/js->clj
                                  signing/typed-data-chain-id)
           parsed-data        (-> parsed-raw-data
                                  (transforms/js-dissoc :types :primaryType)
                                  (transforms/js-stringify 2))]
       (if (and data-chain-id
                (not= session-chain-id data-chain-id))
         {:fx [[:dispatch
                [:wallet-connect/wrong-typed-data-chain-id
                 {:expected-chain-id session-chain-id
                  :wrong-chain-id    data-chain-id}]]]}
         {:db (update-in db
                         [:wallet-connect/current-request]
                         assoc
                         :address      (string/lower-case address)
                         :display-data (or parsed-data raw-data)
                         :raw-data     raw-data)
          :fx [[:dispatch [:wallet-connect/show-request-modal]]]}))
     (catch js/Error err
       {:fx [[:dispatch
              [:wallet-connect/on-processing-error
               (ex-info "Failed to parse JSON typed data"
                        {:error err
                         :data  (data-store/get-db-current-request-params db)})]]]}))))

(rf/reg-event-fx
 :wallet-connect/wrong-typed-data-chain-id
 (fn [_ [{:keys [expected-chain-id wrong-chain-id]}]]
   (let [wrong-network-name    (-> wrong-chain-id
                                   networks/chain-id->network-details
                                   :full-name)
         expected-network-name (-> expected-chain-id
                                   networks/chain-id->network-details
                                   :full-name)
         toast-message         (i18n/label :t/wallet-connect-typed-data-wrong-chain-id-warning
                                           {:wrong-chain    (or wrong-network-name
                                                                (networks/chain-id->eip155
                                                                 wrong-chain-id))
                                            :expected-chain expected-network-name})]
     {:fx [[:dispatch
            [:toasts/upsert
             {:type :negative
              :text toast-message}]]
           [:dispatch
            [:wallet-connect/on-processing-error
             (ex-info "Can't proceed signing typed data due to wrong chain-id included in the data"
                      {:expected-chain-id expected-chain-id
                       :wrong-chain-id    wrong-chain-id})]]]})))

(rf/reg-event-fx
 :wallet-connect/on-processing-error
 (fn [{:keys [db]} [error]]
   (let [{:keys [address event]} (get db :wallet-connect/current-request)
         method                  (data-store/get-request-method event)]
     (log/error "Failed to process Wallet Connect request"
                {:error                error
                 :address              address
                 :method               method
                 :wallet-connect-event event
                 :event                :wallet-connect/on-processing-error})
     ;; FIXME(@clauxx/@alwx): rename this event eventually
     {:fx [[:dispatch [:wallet-connect/on-request-modal-dismissed]]]})))
