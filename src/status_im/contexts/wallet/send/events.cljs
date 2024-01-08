(ns status-im.contexts.wallet.send.events
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :as cske]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.send.utils :as send-utils]
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
     (let [suggested-routes-data (cske/transform-keys csk/->kebab-case suggested-routes)
           chosen-route          (->> suggested-routes-data
                                      :best
                                      first)]
       {:db (-> db
                (assoc-in [:wallet :ui :send :suggested-routes] suggested-routes-data)
                (assoc-in [:wallet :ui :send :route] chosen-route)
                (assoc-in [:wallet :ui :send :loading-suggested-routes?] false))}))))

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
 (fn [{:keys [db]} [{:keys [amount stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)
    :fx [[:navigate-to-within-stack [:wallet-transaction-confirmation stack-id]]]}))

(rf/reg-event-fx :wallet/get-suggested-routes
 (fn [{:keys [db now]} [amount]]
   (let [wallet-address          (get-in db [:wallet :current-viewing-account-address])
         token                   (get-in db [:wallet :ui :send :token])
         to-address              (get-in db [:wallet :ui :send :to-address])
         token-decimal           (:decimals token)
         token-id                (:symbol token)
         network-preferences     []
         gas-rates               constants/gas-rate-medium
         amount-in               (send-utils/amount-in-hex amount token-decimal)
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

(rf/reg-event-fx :wallet/add-authorized-transaction
 (fn [{:keys [db]} [transaction]]
   (let [transaction-hashes (:hashes transaction)
         chain-id           (key (first transaction-hashes))
         tx-id              (first (val (first transaction-hashes)))
         transaction-detes  {:status   :pending
                             :id       (:id transaction)
                             :chain-id chain-id}]
     {:db (assoc-in db [:wallet :transactions tx-id] transaction-detes)
      :fx [[:dispatch [:navigate-to :wallet-transaction-progress]]]})))

(defn- transaction-bridge
  [{:keys [from-address to-address route]}]
  (let [{:keys [from bridge-name amount-out gas-amount gas-fees]}           route
        {:keys [gas-price max-fee-per-gas-medium max-priority-fee-per-gas]} gas-fees]
    [{:BridgeName bridge-name
      :ChainID    (:chain-id from)
      :TransferTx {:From                 from-address
                   :To                   to-address
                   :Gas                  (money/to-hex gas-amount)
                   :GasPrice             (money/to-hex (money/->wei :gwei gas-price))
                   :Value                amount-out
                   :Nonce                nil
                   :MaxFeePerGas         (money/to-hex (money/->wei :gwei max-fee-per-gas-medium))
                   :MaxPriorityFeePerGas (money/to-hex (money/->wei :gwei max-priority-fee-per-gas))
                   :Input                ""
                   :Data                 "0x"}}]))

(defn- multi-transaction-command
  [{:keys [from-address to-address from-asset to-asset amount-out transfer-type]
    :or   {transfer-type constants/send-type-transfer}}]
  {:fromAddress from-address
   :toAddress   to-address
   :fromAsset   from-asset
   :toAsset     to-asset
   :fromAmount  amount-out
   :type        transfer-type})

(rf/reg-event-fx :wallet/send-transaction
 (fn [{:keys [db]} [sha3-pwd]]
   (let [route          (get-in db [:wallet :ui :send :route])
         from-address   (get-in db [:wallet :current-viewing-account-address])
         to-address     (get-in db [:wallet :ui :send :to-address])
         token          (get-in db [:wallet :ui :send :token])
         token-id       (:symbol token)
         request-params [(multi-transaction-command {:from-address from-address
                                                     :to-address   to-address
                                                     :from-asset   token-id
                                                     :to-asset     token-id
                                                     :amount-out   (:amount-out route)})
                         (transaction-bridge {:to-address   to-address
                                              :from-address from-address
                                              :route        route})
                         sha3-pwd]]
     {:json-rpc/call [{:method     "wallet_createMultiTransaction"
                       :params     request-params
                       :on-success (fn [result]
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (rf/dispatch [:wallet/add-authorized-transaction result]))
                       :on-error   (fn [error]
                                     (log/error "failed to send transaction"
                                                {:event  :wallet/send-transaction
                                                 :error  error
                                                 :params request-params}))}]})))

