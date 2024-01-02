(ns status-im.contexts.wallet.send.events
  (:require
    [status-im.constants :as constants]
    [taoensso.timbre :as log]
    [utils.money :as money]
    [utils.number]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :wallet/select-address-tab
 (fn [{:keys [db]} [tab]]
   {:db (assoc-in db [:wallet :ui :send :select-address-tab] tab)}))

(rf/reg-event-fx :wallet/select-send-account-address
 (fn [{:keys [db]} [address]]
   {:db (assoc db [:wallet :ui :send :send-account-address] address)}))

(rf/reg-event-fx :wallet/suggested-routes-success
 (fn [{:keys [db]} [suggested-routes timestamp]]
   (when (= (get-in db [:wallet :ui :send :suggested-routes-call-timestamp]) timestamp)
     {:db (-> db
              (assoc-in [:wallet :ui :send :suggested-routes] suggested-routes)
              (assoc-in [:wallet :ui :send :route] (first (:Best suggested-routes)))
              (assoc-in [:wallet :ui :send :loading-suggested-routes?] false))})))

(rf/reg-event-fx :wallet/suggested-routes-error
 (fn [{:keys [db]} [_error]]
   {:db (-> db
            (update-in [:wallet :ui :send] dissoc :suggested-routes)
            (update-in [:wallet :ui :send] dissoc :route)
            (assoc-in [:wallet :ui :send :loading-suggested-routes?] false))}))

(rf/reg-event-fx :wallet/clean-suggested-routes
 (fn [{:keys [db]}]
   {:db (-> db
            (update-in [:wallet :ui :send] dissoc :suggested-routes)
            (update-in [:wallet :ui :send] dissoc :route)
            (update-in [:wallet :ui :send] dissoc :loading-suggested-routes?))}))

(rf/reg-event-fx :wallet/select-send-address
 (fn [{:keys [db]} [{:keys [address stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :to-address] address)
    :fx [[:navigate-to-within-stack [:wallet-select-asset stack-id]]]}))

(rf/reg-event-fx :wallet/send-select-token
 (fn [{:keys [db]} [{:keys [token stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :token] token)
    :fx [[:navigate-to-within-stack [:wallet-send-input-amount stack-id]]]}))

(rf/reg-event-fx :wallet/send-select-amount
 (fn [{:keys [db]} [{:keys [amount]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)}))

(rf/reg-event-fx :wallet/get-suggested-routes
 (fn [{:keys [db now]} [amount]]
   (let [wallet-address          (get-in db [:wallet :current-viewing-account-address])
         token                   (get-in db [:wallet :ui :send :token])
         to-address              (get-in db [:wallet :ui :send :to-address])
         token-decimal           (:decimals token)
         token-id                (:symbol token)
         network-preferences     []
         gas-rates               constants/gas-rate-medium
         amount-in               (money/amount-in-hex amount token-decimal)
         from-address            wallet-address
         disabled-from-chain-ids []
         disabled-to-chain-ids   []
         from-locked-amount      {}
         request-params          [constants/send-type-transfer
                                  from-address
                                  to-address
                                  amount-in
                                  token-id
                                  disabled-from-chain-ids
                                  disabled-to-chain-ids
                                  network-preferences
                                  gas-rates
                                  from-locked-amount]]
     {:db            (-> db
                         (assoc-in [:wallet :ui :send :loading-suggested-routes?] true)
                         (assoc-in [:wallet :ui :send :suggested-routes-call-timestamp] now))
      :json-rpc/call [{:method     "wallet_getSuggestedRoutes"
                       :params     request-params
                       :on-success (fn [suggested-routes]
                                     (rf/dispatch [:wallet/suggested-routes-success suggested-routes
                                                   now]))
                       :on-error   (fn [error]
                                     (rf/dispatch [:wallet/suggested-routes-error error])
                                     (log/error "failed to get suggested routes"
                                                {:event  :wallet/get-suggested-routes
                                                 :error  error
                                                 :params request-params}))}]})))
