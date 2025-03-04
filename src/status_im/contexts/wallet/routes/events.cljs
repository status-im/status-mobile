(ns status-im.contexts.wallet.routes.events
  (:require
    [re-frame.core :as rf]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.routes.route :as route]
    [status-im.contexts.wallet.routes.utils :as utils]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [taoensso.timbre :as log]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(rf/reg-event-fx :wallet.routes/get-routes
 (fn [{:keys [db]} [{:keys [params on-error]}]]
   (let [route-uuid (:uuid params)]
     {:db (update-in db
                     [:wallet :route]
                     assoc
                     :uuid                     route-uuid
                     :state                    :requesting-routes
                     :best-route               nil
                     :transactions-for-signing nil)
      :fx [[:json-rpc/call
            [{:method   "wallet_getSuggestedRoutesAsync"
              :params   [params]
              :on-error (fn [error]
                          (when on-error
                            (on-error error))
                          (rf/dispatch [:wallet.routes/on-error error])
                          (log/error "failed to get suggested routes (async)"
                                     {:event  :wallet.routes/get-routes
                                      :error  (:message error)
                                      :params params}))}]]]})))

(rf/reg-event-fx :wallet.routes/stop-get-routes
 (fn [_]
   (log/info "Stopping getting routes and clearing route data")
   {:fx [[:dispatch [:wallet.routes/clear-route]]
         [:json-rpc/call
          [{:method   "wallet_stopSuggestedRoutesAsyncCalculation"
            :params   []
            :on-error (fn [error]
                        (log/error "failed to stop fetching swap proposals"
                                   {:error error}))}]]]}))

(rf/reg-event-fx :wallet.routes/clear-route
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet] dissoc :route)}))

(rf/reg-event-fx :wallet.routes/on-error
 (fn [{:keys [db]} [error]]
   (let [valid-error? (utils/valid-error? error)]
     (println :valid-error valid-error?)
     (when valid-error?
       {:db (update-in db
                       [:wallet :route]
                       assoc
                       :error error
                       :state :error)}))))

(rf/reg-event-fx :wallet.routes/reset-error
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :route] dissoc :error)}))

(rf/reg-event-fx :wallet.routes/handle-suggested-routes
 (fn [{:keys [db]} [routes]]
   (let [best-route (route/get-best-suggested-route routes)
         error      (route/error-response routes)
         route-uuid (route/routes-uuid routes)]
     (log/info "Getting routes for" route-uuid)
     {:db (update-in db
                     [:wallet :route]
                     assoc
                     :uuid       route-uuid
                     :state      :receiving-routes
                     :best-route best-route)
      :fx [(if error
             [:dispatch [:wallet.routes/on-error error]]
             [:dispatch [:wallet.routes/reset-error]])]})))

(rf/reg-event-fx
 :wallet.routes/build-transactions
 (fn [{:keys [db]} [{:keys [request-uuid slippage] :or {slippage constants/default-slippage}}]]
   {:db (assoc-in db [:wallet :route :state] :building-transactions)
    :fx [[:json-rpc/call
          [{:method   "wallet_buildTransactionsFromRoute"
            :params   [{:uuid               request-uuid
                        :slippagePercentage slippage}]
            :on-error (fn [error]
                        (log/error "failed to build transactions from route"
                                   {:event :wallet/build-transactions-from-route
                                    :error error})
                        (rf/dispatch [:toasts/upsert
                                      {:id   :build-transactions-from-route-error
                                       :type :negative
                                       :text (:message error)}]))}]]]}))

(rf/reg-event-fx
 :wallet.routes/on-transactions-built
 (fn [{:keys [db]} [transactions-for-signing]]
   (let [error                  (-> transactions-for-signing :sendDetails :errorResponse)
         route-uuid             (get-in db [:wallet :route :uuid])
         built-transaction-uuid (-> transactions-for-signing :sendDetails :uuid)]
     (when (= route-uuid built-transaction-uuid)
       {:db (update-in db
                       [:wallet :route]
                       assoc
                       :state                    :transactions-built
                       :transactions-for-signing transactions-for-signing)
        :fx [(when error
               [:dispatch [:wallet.routes/on-error error]])]}))))

(rf/reg-event-fx
 :wallet.routes/sign-transactions
 (fn [{:keys [db]} [sha3-pwd]]
   (let [{:keys [hashes address signOnKeycard]} (get-in db
                                                        [:wallet :route :transactions-for-signing
                                                         :signingDetails])
         on-success                             (fn [signatures]
                                                  (rf/dispatch
                                                   [:wallet.routes/send-transactions signatures]))
         on-error                               (fn [error]
                                                  (log/error
                                                   "failed to prepare signatures for transactions"
                                                   {:event :wallet/prepare-signatures-for-transactions
                                                    :error error})
                                                  (rf/dispatch
                                                   [:toasts/upsert
                                                    {:id   :prepare-signatures-for-transactions-error
                                                     :type :negative
                                                     :text (:message error)}]))]
     (if signOnKeycard
       {:fx [[:dispatch
              [:standard-auth/authorize-with-keycard
               {:on-complete #(rf/dispatch [:keycard/connect-and-sign-hashes
                                            {:keycard-pin %
                                             :address     address
                                             :hashes      hashes
                                             :on-success  on-success
                                             :on-failure  on-error}])}]]]}
       {:fx [[:effects.wallet/sign-transaction-hashes
              {:hashes     hashes
               :address    address
               :password   (security/safe-unmask-data sha3-pwd)
               :on-success on-success
               :on-error   on-error}]]}))))

(rf/reg-event-fx
 :wallet.routes/send-transactions
 (fn [{:keys [db]} [signatures]]
   (let [transactions-for-signing (get-in db [:wallet :route :transactions-for-signing])
         signatures-map           (reduce (fn [acc {:keys [message signature]}]
                                            (assoc acc
                                                   message
                                                   (send-utils/signature-rsv signature)))
                                          {}
                                          signatures)]
     (log/info "Sending transactions with signatures")
     {:db (assoc-in db [:wallet :route :state] :sending-transactions)
      :fx [[:json-rpc/call
            [{:method     "wallet_sendRouterTransactionsWithSignatures"
              :params     [{:uuid       (get-in transactions-for-signing [:sendDetails :uuid])
                            :signatures signatures-map}]
              :on-success (fn [] (rf/dispatch [:hide-bottom-sheet]))
              :on-error   (fn [error]
                            (log/error "failed to send router transactions with signatures"
                                       {:event :wallet/send-router-transactions-with-signatures
                                        :error error})
                            (rf/dispatch [:toasts/upsert
                                          {:id   :send-router-transactions-with-signatures-error
                                           :type :negative
                                           :text (:message error)}]))}]]]})))

(rf/reg-event-fx
 :wallet.routes/on-transactions-sent
 (fn [{:keys [db]} [{:keys [sentTransactions sendDetails]}]]
   (let [error              (:errorResponse sendDetails)
         state-transactions (get-in db [:wallet :route :transactions] {})
         sent-transactions  (route/sent-transactions-map sentTransactions)]
     {:db (update-in db
                     [:wallet :route]
                     assoc
                     :state        :transactions-sent
                     :transactions (merge state-transactions sent-transactions))
      :fx [(when error
             [:dispatch [:wallet.routes/on-error error]])]})))

(rf/reg-event-fx
 :wallet.routes/pending-transaction-status-changed
 (fn [{:keys [db]} [data]]
   (let [tx-message (-> data :message transforms/json->clj)
         tx-status  (:status tx-message)
         tx-hash    (:hash tx-message)
         new-status (cond
                      (= tx-status constants/transaction-status-success)
                      :confirmed
                      (= tx-status constants/transaction-status-pending)
                      :pending
                      (= tx-status constants/transaction-status-failed)
                      :failed)]
     (log/info "Transaction status changed" tx-hash new-status)
     {:db (update-in db [:wallet :route :transactions tx-hash] assoc :status new-status)})))
