(ns status-im.contexts.wallet.wallet-connect.events.session-responses
  (:require [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as data-store]
            [status-im.contexts.wallet.wallet-connect.utils.request :as session-request]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet-connect/respond-current-session
 (fn [{:keys [db]} [password]]
   (let [event (data-store/get-db-current-request-event db)]
     (if (session-request/expired? event)
       {:fx [[:dispatch
              [:toasts/upsert
               {:id   :new-wallet-account-created
                :type :negative
                :text (i18n/label :t/wallet-connect-request-expired)}]]
             [:dispatch [:dismiss-modal (session-request/screen event)]]]}
       {:fx [(condp = (session-request/method event)
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
               [:dispatch [:wallet-connect/respond-sign-typed-data password :v4]])]}))))

(rf/reg-event-fx
 :wallet-connect/respond-sign-message
 (fn [{:keys [db]} [password rpc-method]]
   (let [{:keys [address raw-data]} (get db :wallet-connect/current-request)]
     {:fx [[:effects.wallet-connect/sign-message
            {:password   password
             :address    address
             :data       raw-data
             :rpc-method rpc-method
             :on-error   [:wallet-connect/on-sign-error]
             :on-success [:wallet-connect/finish-session-request]}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-sign-typed-data
 (fn [{:keys [db]} [password typed-data-version]]
   (let [{:keys [address raw-data event]} (get db :wallet-connect/current-request)
         chain-id                         (session-request/chain event)]
     {:fx [[:effects.wallet-connect/sign-typed-data
            {:password   password
             :address    address
             :data       raw-data
             :chain-id   chain-id
             :version    typed-data-version
             :on-error   [:wallet-connect/on-sign-error]
             :on-success [:wallet-connect/finish-session-request]}]]})))

(rf/reg-event-fx
 :wallet-connect/respond-send-transaction-data
 (fn [{:keys [db]} [password]]
   (let [{:keys [raw-data address event]} (get db :wallet-connect/current-request)
         {:keys [tx-hash tx-args]}        raw-data
         chain-id                         (session-request/chain event)]
     {:fx [[:effects.wallet-connect/send-transaction
            {:password   password
             :address    address
             :chain-id   chain-id
             :tx-hash    tx-hash
             :tx-args    tx-args
             :on-error   [:wallet-connect/on-sign-error]
             :on-success [:wallet-connect/finish-session-request]}]]})))

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
             :on-error   [:wallet-connect/on-sign-error]
             :on-success [:wallet-connect/finish-session-request]}]]})))

(rf/reg-event-fx
 :wallet-connect/on-sign-error
 (fn [{:keys [db]} [error]]
   (let [{:keys [raw-data address event]} (get db :wallet-connect/current-request)
         method                           (session-request/method event)]
     (log/error "Failed to sign Wallet Connect request"
                {:error                error
                 :address              address
                 :sign-data            raw-data
                 :method               method
                 :wallet-connect-event event
                 :event                :wallet-connect/on-sign-error})
     {:fx [[:dispatch [:wallet-connect/dismiss-request-modal]]
           [:dispatch
            [:toasts/upsert
             {:type :negative
              :text (i18n/label :t/wallet-connect-something-went-wrong)}]]]})))

(rf/reg-event-fx
 :wallet-connect/send-response
 (fn [{:keys [db]} [{:keys [request result error]}]]
   (when-let [{:keys [id topic] :as event} (or request
                                               (data-store/get-db-current-request-event db))]
     (let [method      (session-request/method event)
           web3-wallet (get db :wallet-connect/web3-wallet)]
       {:db (assoc-in db [:wallet-connect/current-request :response-sent?] true)
        :fx [[:effects.wallet-connect/respond-session-request
              {:web3-wallet web3-wallet
               :topic       topic
               :id          id
               :result      result
               :error       error
               :on-error    (fn [error]
                              (log/error "Failed to send Wallet Connect response"
                                         {:error                error
                                          :method               method
                                          :event                :wallet-connect/send-response
                                          :wallet-connect-event event}))
               :on-success  (fn []
                              (rf/dispatch [:wallet-connect/redirect-to-dapp])
                              (log/info "Successfully sent Wallet Connect response to dApp"))}]]}))))

(rf/reg-event-fx
 :wallet-connect/redirect-to-dapp
 (fn [{:keys [db]} [url]]
   (let [sessions     (get db :wallet-connect/sessions)
         redirect-url (or url
                          (-> db
                              data-store/get-db-current-request-event
                              (data-store/get-current-request-dapp sessions)
                              :sessionJson
                              transforms/json->clj
                              data-store/get-dapp-redirect-url))]
     {:fx [[:effects/open-url redirect-url]]})))

(rf/reg-event-fx
 :wallet-connect/dismiss-request-modal
 (fn [{:keys [db]} _]
   (let [screen (-> db
                    (get-in [:wallet-connect/current-request :event])
                    session-request/screen)]
     {:fx [[:dispatch [:dismiss-modal screen]]]})))

(rf/reg-event-fx
 :wallet-connect/finish-session-request
 (fn [{:keys [db]} [result]]
   (let [event      (get-in db [:wallet-connect/current-request :event])
         method     (session-request/method event)
         toast-text (condp contains? method
                      constants/wallet-connect-message-signing-methods
                      (i18n/label :t/wallet-connect-message-request-success-toast)

                      constants/wallet-connect-transaction-methods
                      (i18n/label :t/wallet-connect-transaction-request-success-toast))]
     {:fx [[:dispatch
            [:centralized-metrics/track :metric/dapp-session-response
             {:method   method
              :approved true}]]
           [:dispatch [:wallet-connect/send-response {:result result}]]
           [:dispatch [:wallet-connect/dismiss-request-modal]]
           [:dispatch
            [:toasts/upsert
             {:type :positive
              :text toast-text}]]]})))

;; NOTE: Currently we only reject a session if the user dismissed a modal
;; without accepting the session first.
;; But this needs to be solidified to ensure other cases:
;; - Unsupported WC version
;; - Invalid params from dapps
;; - Unsupported method
;; - Failed processing of request
;; - Failed "responding" (signing or sending message/transaction)
(rf/reg-event-fx
 :wallet-connect/on-request-modal-dismissed
 (fn [{:keys [db]}]
   (let [{:keys [response-sent? event]} (get db :wallet-connect/current-request)
         method                         (session-request/method event)
         dapp                           (->> (get db :wallet-connect/sessions)
                                             (data-store/get-current-request-dapp event)
                                             :name)]
     {:fx (concat
           (when-not response-sent?
             [[:dispatch
               [:centralized-metrics/track :metric/dapp-session-response
                {:method   method
                 :approved false}]]
              [:dispatch
               [:wallet-connect/send-response
                {:request event
                 :error   (wallet-connect/get-sdk-error
                           constants/wallet-connect-user-rejected-error-key)}]]
              [:dispatch
               [:toasts/upsert
                {:type :positive
                 :text (i18n/label :t/wallet-connect-sign-request-rejected-toast
                                   {:dapp dapp})}]]])
           [[:dispatch [:wallet-connect/reset-current-request]]])})))
