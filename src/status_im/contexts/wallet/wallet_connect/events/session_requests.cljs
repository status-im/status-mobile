(ns status-im.contexts.wallet.wallet-connect.events.session-requests
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.networks.db :as networks.db]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as
             data-store]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks.utils]
            [status-im.contexts.wallet.wallet-connect.utils.transactions :as transactions]
            [status-im.contexts.wallet.wallet-connect.utils.typed-data :as typed-data]
            [taoensso.timbre :as log]
            [utils.address :as utils-address]
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

               constants/wallet-connect-eth-sign-typed-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-eth-sign-typed-v4-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-personal-sign-method
               [:dispatch [:wallet-connect/process-personal-sign]])]}))))

(rf/reg-event-fx
 :wallet-connect/store-prepared-hash
 (fn [{:keys [db]} [prepared-hash]]
   {:db (assoc-in db
         [:wallet-connect/current-request :prepared-hash]
         prepared-hash)}))

(rf/reg-event-fx
 :wallet-connect/process-personal-sign
 (fn [{:keys [db]}]
   (let [[raw-data address] (data-store/get-db-current-request-params db)
         parsed-data        (if (utils-address/has-hex-prefix? raw-data)
                              (native-module/hex-to-utf8 raw-data)
                              raw-data)
         hex-message        (if (utils-address/has-hex-prefix? raw-data)
                              raw-data
                              (native-module/utf8-to-hex raw-data))]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      (string/lower-case address)
                     :display-data (or parsed-data raw-data))
      :fx [[:effects.wallet-connect/hash-message
            {:message    hex-message
             :on-success #(rf/dispatch [:wallet-connect/store-prepared-hash %])
             :on-fail    #(rf/dispatch [:wallet-connect/on-processing-error %])}]
           [:dispatch [:wallet-connect/show-request-modal]]]})))

(rf/reg-event-fx
 :wallet-connect/prepare-transaction-success
 (fn [{:keys [db]} [prepared-tx chain-id]]
   (let [{:keys [tx-args tx-hash]} prepared-tx
         tx                        (bean/->clj tx-args)
         display-data              (transactions/beautify-transaction tx)]
     (log/info "WC transaction prepared")
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :raw-data     prepared-tx
                     :transaction  tx
                     :chain-id     chain-id
                     :display-data display-data)
      :fx [[:dispatch [:wallet-connect/store-prepared-hash tx-hash]]]})))

(rf/reg-event-fx :wallet-connect/prepare-transaction
 (fn [{:keys [db]} [on-success]]
   (let [event    (data-store/get-db-current-request-event db)
         tx       (-> event data-store/get-request-params first)
         chain-id (-> event
                      (get-in [:params :chainId])
                      networks.utils/eip155->chain-id)]
     (log/info "Preparing WC transaction")
     {:fx [[:effects.wallet-connect/prepare-transaction
            {:tx         tx
             :chain-id   chain-id
             :on-success (fn [data]
                           (rf/dispatch [:wallet-connect/prepare-transaction-success data
                                         chain-id])
                           (when on-success
                             (rf/call-continuation on-success)))
             :on-error   #(rf/dispatch [:wallet-connect/on-processing-error %])}]]})))

(rf/reg-event-fx
 :wallet-connect/process-eth-send-transaction
 (fn [{:keys [db]} [{:keys [on-success]}]]
   (let [event             (data-store/get-db-current-request-event db)
         chain-id          (data-store/get-request-chain-id event)
         tx                (-> event data-store/get-request-params first)
         address           (-> tx :from string/lower-case)
         chain-active?     (networks.db/network-active? db chain-id)
         prepare-tx-effect [:wallet-connect/prepare-transaction on-success]]
     (when tx
       {:db (assoc-in db [:wallet-connect/current-request :address] address)
        :fx [(if chain-active?
               [:dispatch prepare-tx-effect]
               [:dispatch
                [:wallet-connect/show-activate-request-network-sheet
                 {:on-success #(rf/dispatch prepare-tx-effect)}]])]}))))

(rf/reg-event-fx
 :wallet-connect/process-sign-typed
 (fn [{:keys [db]}]
   (try
     (let [[address raw-data] (data-store/get-db-current-request-params db)
           method             (-> db
                                  data-store/get-db-current-request-event
                                  data-store/get-request-method)
           session-chain-id   (-> (data-store/get-db-current-request-event db)
                                  (get-in [:params :chainId])
                                  networks.utils/eip155->chain-id)
           typed-data         (-> raw-data
                                  transforms/js-parse
                                  transforms/js->clj)
           data-chain-id      (typed-data/get-chain-id typed-data)]
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
                         :display-data (typed-data/flatten-typed-data typed-data)
                         :raw-data     raw-data)
          :fx [[:effects.wallet-connect/hash-typed-data
                {:message    raw-data
                 :legacy?    (not= constants/wallet-connect-eth-sign-typed-v4-method
                                   method)
                 :on-success #(rf/dispatch [:wallet-connect/store-prepared-hash %])
                 :on-fail    #(rf/dispatch [:wallet-connect/on-processing-error %])}]
               [:dispatch [:wallet-connect/show-request-modal]]]}))
     (catch js/Error err
       {:fx [[:dispatch
              [:wallet-connect/on-processing-error
               (ex-info "Failed to parse JSON typed data"
                        {:error err
                         :data  (data-store/get-db-current-request-params db)})]]]}))))

(rf/reg-event-fx
 :wallet-connect/wrong-typed-data-chain-id
 (fn [{:keys [db]} [{:keys [expected-chain-id wrong-chain-id]}]]
   (let [wrong-network-name    (->> wrong-chain-id
                                    (networks.db/get-network-details db)
                                    :full-name)
         expected-network-name (->> expected-chain-id
                                    (networks.db/get-network-details db)
                                    :full-name)
         toast-message         (i18n/label :t/wallet-connect-typed-data-wrong-chain-id-warning
                                           {:wrong-chain    (or wrong-network-name
                                                                (networks.utils/chain-id->eip155
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
