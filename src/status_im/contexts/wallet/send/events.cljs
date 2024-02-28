(ns status-im.contexts.wallet.send.events
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.money :as money]
    [utils.number]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-event-fx :wallet/clean-send-data
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui] dissoc :send)}))

(rf/reg-event-fx :wallet/select-address-tab
 (fn [{:keys [db]} [tab]]
   {:db (assoc-in db [:wallet :ui :send :select-address-tab] tab)}))

(rf/reg-event-fx :wallet/suggested-routes-success
 (fn [{:keys [db]} [suggested-routes timestamp]]
   (when (= (get-in db [:wallet :ui :send :suggested-routes-call-timestamp]) timestamp)
     (let [suggested-routes-data (cske/transform-keys transforms/->kebab-case-keyword suggested-routes)
           chosen-route          (:best suggested-routes-data)]
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

(rf/reg-event-fx :wallet/clean-send-address
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :recipient :to-address)}))

(rf/reg-event-fx
 :wallet/select-send-address
 (fn [{:keys [db]} [{:keys [address token? recipient stack-id]}]]
   (let [[prefix to-address] (utils/split-prefix-and-address address)
         test-net?           (get-in db [:profile/profile :test-networks-enabled?])
         goerli-enabled?     (get-in db [:profile/profile :is-goerli-enabled?])
         prefix-seq          (string/split prefix #":")
         selected-networks   (->> prefix-seq
                                  (remove string/blank?)
                                  (mapv #(utils/short-name->id (keyword %) test-net? goerli-enabled?)))]
     {:db (-> db
              (assoc-in [:wallet :ui :send :recipient] (or recipient address))
              (assoc-in [:wallet :ui :send :to-address] to-address)
              (assoc-in [:wallet :ui :send :address-prefix] prefix)
              (assoc-in [:wallet :ui :send :selected-networks] selected-networks))
      :fx [[:dispatch
            [:navigate-to-within-stack
             (if token?
               [:screen/wallet.send-input-amount stack-id]
               [:screen/wallet.select-asset stack-id])]]]})))

(rf/reg-event-fx
 :wallet/update-receiver-networks
 (fn [{:keys [db]} [selected-networks]]
   {:db (assoc-in db [:wallet :ui :send :selected-networks] selected-networks)}))

(rf/reg-event-fx :wallet/send-select-token
 (fn [{:keys [db]} [{:keys [token stack-id]}]]
   {:db (-> db
            (update-in [:wallet :ui :send] dissoc :collectible)
            (assoc-in [:wallet :ui :send :token] token))
    :fx [[:dispatch [:wallet/clean-suggested-routes]]
         [:dispatch [:navigate-to-within-stack [:screen/wallet.send-input-amount stack-id]]]]}))

(rf/reg-event-fx
 :wallet/send-select-token-drawer
 (fn [{:keys [db]} [{:keys [token]}]]
   {:db (assoc-in db [:wallet :ui :send :token] token)}))

(rf/reg-event-fx :wallet/clean-selected-token
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :token :tx-type)}))

(rf/reg-event-fx :wallet/clean-selected-collectible
 (fn [{:keys [db]}]
   (let [transaction-type (get-in db [:wallet :ui :send :tx-type])]
     {:db (update-in db
                     [:wallet :ui :send]
                     dissoc
                     :collectible
                     :amount
                     (when (= transaction-type :collecible) :tx-type))})))

(rf/reg-event-fx :wallet/send-select-collectible
 (fn [{:keys [db]} [{:keys [collectible stack-id]}]]
   {:db (-> db
            (update-in [:wallet :ui :send] dissoc :token)
            (assoc-in [:wallet :ui :send :collectible] collectible)
            (assoc-in [:wallet :ui :send :tx-type] :collectible)
            (assoc-in [:wallet :ui :send :amount] 1))
    :fx [[:dispatch [:wallet/get-suggested-routes {:amount 1}]]
         [:navigate-to-within-stack [:screen/wallet.transaction-confirmation stack-id]]]}))

(rf/reg-event-fx :wallet/send-select-amount
 (fn [{:keys [db]} [{:keys [amount stack-id]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)
    :fx [[:dispatch [:navigate-to-within-stack [:screen/wallet.transaction-confirmation stack-id]]]]}))

(rf/reg-event-fx :wallet/get-suggested-routes
 (fn [{:keys [db now]} [{:keys [amount]}]]
   (let [wallet-address          (get-in db [:wallet :current-viewing-account-address])
         token                   (get-in db [:wallet :ui :send :token])
         transaction-type        (get-in db [:wallet :ui :send :tx-type])
         collectible             (get-in db [:wallet :ui :send :collectible])
         to-address              (get-in db [:wallet :ui :send :to-address])
         test-networks-enabled?  (get-in db [:profile/profile :test-networks-enabled?])
         networks                ((if test-networks-enabled? :test :prod)
                                  (get-in db [:wallet :networks]))
         network-chain-ids       (map :chain-id networks)
         bridge-to-chain-id      (get-in db [:wallet :ui :send :bridge-to-chain-id])
         token-decimal           (when token (:decimals token))
         token-id                (if token
                                   (:symbol token)
                                   (str (get-in collectible [:id :contract-id :address])
                                        ":"
                                        (get-in collectible [:id :token-id])))
         network-preferences     (if token [] [(get-in collectible [:id :contract-id :chain-id])])
         gas-rates               constants/gas-rate-medium
         amount-in               (send-utils/amount-in-hex amount (if token token-decimal 0))
         from-address            wallet-address
         disabled-from-chain-ids []
         disabled-to-chain-ids   (if (= transaction-type :bridge)
                                   (filter #(not= % bridge-to-chain-id) network-chain-ids)
                                   [])
         from-locked-amount      {}
         transaction-type-param  (case transaction-type
                                   :collectible constants/send-type-erc-721-transfer
                                   :bridge      constants/send-type-bridge
                                   constants/send-type-transfer)
         request-params          [transaction-type-param
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
   (let [transaction-batch-id (:id transaction)
         transaction-hashes   (:hashes transaction)
         transaction-ids      (flatten (vals transaction-hashes))
         transaction-details  (send-utils/map-multitransaction-by-ids transaction-batch-id
                                                                      transaction-hashes)]
     {:db (-> db
              (assoc-in [:wallet :transactions] transaction-details)
              (assoc-in [:wallet :ui :send :transaction-ids] transaction-ids))
      :fx [[:dispatch
            [:navigate-to-within-stack
             [:screen/wallet.transaction-progress :screen/wallet.transaction-confirmation]]]]})))

(rf/reg-event-fx :wallet/close-transaction-progress-page
 (fn [_]
   {:fx [[:dispatch [:dismiss-modal :screen/wallet.transaction-progress]]]}))

(defn- transaction-data
  [{:keys [from-address to-address token-address route data eth-transfer?]}]
  (let [{:keys [amount-in gas-amount gas-fees]} route
        eip-1559-enabled?                       (:eip-1559-enabled gas-fees)
        {:keys [gas-price max-fee-per-gas-medium
                max-priority-fee-per-gas]}      gas-fees]
    (cond-> {:From  from-address
             :To    (or token-address to-address)
             :Gas   (money/to-hex gas-amount)
             :Value (when eth-transfer? amount-in)
             :Nonce nil
             :Input ""
             :Data  (or data "0x")}
      eip-1559-enabled?       (assoc
                               :TxType "0x02"
                               :MaxFeePerGas
                               (money/to-hex
                                (money/->wei
                                 :gwei
                                 max-fee-per-gas-medium))
                               :MaxPriorityFeePerGas
                               (money/to-hex
                                (money/->wei
                                 :gwei
                                 max-priority-fee-per-gas)))
      (not eip-1559-enabled?) (assoc :TxType "0x00"
                                     :GasPrice
                                     (money/to-hex
                                      (money/->wei
                                       :gwei
                                       gas-price))))))

(defn- transaction-path
  [{:keys [from-address to-address token-id token-address route data eth-transfer?]}]
  (let [{:keys [bridge-name amount-in bonder-fees from
                to]}  route
        tx-data       (transaction-data {:from-address  from-address
                                         :to-address    to-address
                                         :token-address token-address
                                         :route         route
                                         :data          data
                                         :eth-transfer? eth-transfer?})
        to-chain-id   (:chain-id to)
        from-chain-id (:chain-id from)]
    (cond-> {:BridgeName bridge-name
             :ChainID    from-chain-id}

      (= bridge-name constants/bridge-name-erc-721-transfer)
      (assoc :ERC721TransferTx
             (assoc tx-data
                    :Recipient to-address
                    :TokenID   token-id
                    :ChainID   to-chain-id))

      (= bridge-name constants/bridge-name-transfer)
      (assoc :TransferTx tx-data)

      (= bridge-name constants/bridge-name-hop)
      (assoc :HopTx
             (assoc tx-data
                    :ChainID   to-chain-id
                    :Symbol    token-id
                    :Recipient to-address
                    :Amount    amount-in
                    :BonderFee bonder-fees))

      (not (or (= bridge-name constants/bridge-name-erc-721-transfer)
               (= bridge-name constants/bridge-name-transfer)
               (= bridge-name constants/bridge-name-hop)))
      (assoc :CbridgeTx
             (assoc tx-data
                    :ChainID   to-chain-id
                    :Symbol    token-id
                    :Recipient to-address
                    :Amount    amount-in)))))

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
   (let [routes (get-in db [:wallet :ui :send :route])
         first-route (first routes)
         from-address (get-in db [:wallet :current-viewing-account-address])
         transaction-type (get-in db [:wallet :ui :send :tx-type])
         transaction-type-param (case transaction-type
                                  :collectible constants/send-type-erc-721-transfer
                                  :bridge      constants/send-type-bridge
                                  constants/send-type-transfer)
         token (get-in db [:wallet :ui :send :token])
         collectible (get-in db [:wallet :ui :send :collectible])
         first-route-from-chain-id (get-in first-route [:from :chain-id])
         token-id (if token
                    (:symbol token)
                    (get-in collectible [:id :token-id]))
         erc20-transfer? (and token (not= token-id "ETH"))
         eth-transfer? (and token (not erc20-transfer?))
         token-address (cond collectible
                             (get-in collectible
                                     [:id :contract-id :address])
                             erc20-transfer?
                             (get-in token [:balances-per-chain first-route-from-chain-id :address]))
         to-address (get-in db [:wallet :ui :send :to-address])
         transaction-paths (mapv (fn [route]
                                   (let [data (when erc20-transfer?
                                                (native-module/encode-transfer
                                                 (address/normalized-hex to-address)
                                                 (:amount-in route)))]
                                     (transaction-path {:to-address    to-address
                                                        :from-address  from-address
                                                        :route         route
                                                        :token-address token-address
                                                        :token-id      (if collectible
                                                                         (money/to-hex (js/parseInt
                                                                                        token-id))
                                                                         token-id)
                                                        :data          data
                                                        :eth-transfer? eth-transfer?})))
                                 routes)
         request-params
         [(multi-transaction-command
           {:from-address  from-address
            :to-address    to-address
            :from-asset    token-id
            :to-asset      token-id
            :amount-out    (if eth-transfer? (:amount-out first-route) "0x0")
            :transfer-type transaction-type-param})
          transaction-paths
          sha3-pwd]]
     {:json-rpc/call [{:method     "wallet_createMultiTransaction"
                       :params     request-params
                       :on-success (fn [result]
                                     (rf/dispatch [:hide-bottom-sheet])
                                     (rf/dispatch [:wallet/add-authorized-transaction result])
                                     (rf/dispatch [:wallet/clean-scanned-address])
                                     (rf/dispatch [:wallet/clean-local-suggestions])
                                     (rf/dispatch [:wallet/clean-send-address])
                                     (rf/dispatch [:wallet/select-address-tab nil]))
                       :on-error   (fn [error]
                                     (log/error "failed to send transaction"
                                                {:event  :wallet/send-transaction
                                                 :error  error
                                                 :params request-params}))}]})))
