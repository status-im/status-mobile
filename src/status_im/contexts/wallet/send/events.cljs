(ns status-im.contexts.wallet.send.events
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as collectible.utils]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
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
     (let [suggested-routes-data         (cske/transform-keys transforms/->kebab-case-keyword
                                                              suggested-routes)
           chosen-route                  (:best suggested-routes-data)
           token                         (get-in db [:wallet :ui :send :token])
           collectible                   (get-in db [:wallet :ui :send :collectible])
           token-display-name            (get-in db [:wallet :ui :send :token-display-name])
           receiver-networks             (get-in db [:wallet :ui :send :receiver-networks])
           receiver-network-values       (get-in db [:wallet :ui :send :receiver-network-values])
           sender-network-values         (get-in db [:wallet :ui :send :sender-network-values])
           tx-type                       (get-in db [:wallet :ui :send :tx-type])
           disabled-from-chain-ids       (get-in db [:wallet :ui :send :disabled-from-chain-ids] [])
           from-locked-amounts           (get-in db [:wallet :ui :send :from-locked-amounts] {})
           token-decimals                (if collectible 0 (:decimals token))
           native-token?                 (and token (= token-display-name "ETH"))
           routes-available?             (pos? (count chosen-route))
           token-networks                (:networks token)
           token-networks-ids            (when token-networks (mapv #(:chain-id %) token-networks))
           from-network-amounts-by-chain (send-utils/network-amounts-by-chain {:route chosen-route
                                                                               :token-decimals
                                                                               token-decimals
                                                                               :native-token?
                                                                               native-token?
                                                                               :receiver? false})
           from-network-values-for-ui    (send-utils/network-values-for-ui from-network-amounts-by-chain)
           to-network-amounts-by-chain   (send-utils/network-amounts-by-chain {:route chosen-route
                                                                               :token-decimals
                                                                               token-decimals
                                                                               :native-token?
                                                                               native-token?
                                                                               :receiver? true})
           to-network-values-for-ui      (send-utils/network-values-for-ui to-network-amounts-by-chain)
           sender-possible-chain-ids     (mapv :chain-id sender-network-values)
           sender-network-values         (if routes-available?
                                           (send-utils/network-amounts
                                            {:network-values
                                             (if (= tx-type :tx/bridge)
                                               from-network-values-for-ui
                                               (send-utils/add-zero-values-to-network-values
                                                from-network-values-for-ui
                                                sender-possible-chain-ids))
                                             :disabled-chain-ids disabled-from-chain-ids
                                             :receiver-networks receiver-networks
                                             :token-networks-ids token-networks-ids
                                             :from-locked-amounts from-locked-amounts
                                             :tx-type tx-type
                                             :receiver? false})
                                           (send-utils/reset-loading-network-amounts-to-zero
                                            sender-network-values))
           receiver-network-values       (if routes-available?
                                           (send-utils/network-amounts
                                            {:network-values     to-network-values-for-ui
                                             :disabled-chain-ids disabled-from-chain-ids
                                             :receiver-networks  receiver-networks
                                             :token-networks-ids token-networks-ids
                                             :tx-type            tx-type
                                             :receiver?          true})
                                           (->
                                             (send-utils/reset-loading-network-amounts-to-zero
                                              receiver-network-values)
                                             vec
                                             (conj {:type :edit})))

           network-links                 (when routes-available?
                                           (send-utils/network-links chosen-route
                                                                     sender-network-values
                                                                     receiver-network-values))]
       {:db (-> db
                (assoc-in [:wallet :ui :send :suggested-routes] suggested-routes-data)
                (assoc-in [:wallet :ui :send :route] chosen-route)
                (assoc-in [:wallet :ui :send :from-values-by-chain] from-network-values-for-ui)
                (assoc-in [:wallet :ui :send :to-values-by-chain] to-network-values-for-ui)
                (assoc-in [:wallet :ui :send :sender-network-values] sender-network-values)
                (assoc-in [:wallet :ui :send :receiver-network-values] receiver-network-values)
                (assoc-in [:wallet :ui :send :network-links] network-links)
                (assoc-in [:wallet :ui :send :loading-suggested-routes?] false))}))))

(rf/reg-event-fx :wallet/suggested-routes-error
 (fn [{:keys [db]} [error]]
   (let [cleaned-sender-network-values   (-> (get-in db [:wallet :ui :send :sender-network-values])
                                             (send-utils/reset-loading-network-amounts-to-zero))
         cleaned-receiver-network-values (-> (get-in db [:wallet :ui :send :receiver-network-values])
                                             (send-utils/reset-loading-network-amounts-to-zero))]
     {:db (-> db
              (update-in [:wallet :ui :send]
                         dissoc
                         :route)
              (assoc-in [:wallet :ui :send :sender-network-values] cleaned-sender-network-values)
              (assoc-in [:wallet :ui :send :receiver-network-values] cleaned-receiver-network-values)
              (assoc-in [:wallet :ui :send :loading-suggested-routes?] false)
              (assoc-in [:wallet :ui :send :suggested-routes] {:best []}))
      :fx [[:dispatch
            [:toasts/upsert
             {:id   :send-transaction-error
              :type :negative
              :text (:message error)}]]]})))

(rf/reg-event-fx :wallet/clean-suggested-routes
 (fn [{:keys [db]}]
   {:db (update-in db
                   [:wallet :ui :send]
                   dissoc
                   :suggested-routes
                   :route
                   :amount
                   :from-values-by-chain
                   :to-values-by-chain
                   :sender-network-values
                   :receiver-network-values
                   :network-links
                   :loading-suggested-routes?
                   :suggested-routes-call-timestamp)}))

(rf/reg-event-fx :wallet/clean-send-address
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :recipient :to-address)}))

(rf/reg-event-fx :wallet/clean-send-amount
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :amount)}))

(rf/reg-event-fx :wallet/clean-disabled-from-networks
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :disabled-from-chain-ids)}))

(rf/reg-event-fx :wallet/clean-from-locked-amounts
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :from-locked-amounts)}))

(rf/reg-event-fx
 :wallet/select-send-address
 (fn [{:keys [db]} [{:keys [address recipient stack-id start-flow?]}]]
   (let [[prefix to-address] (utils/split-prefix-and-address address)
         testnet-enabled?    (get-in db [:profile/profile :test-networks-enabled?])
         goerli-enabled?     (get-in db [:profile/profile :is-goerli-enabled?])
         receiver-networks   (network-utils/resolve-receiver-networks
                              {:prefix           prefix
                               :testnet-enabled? testnet-enabled?
                               :goerli-enabled?  goerli-enabled?})
         collectible-tx?     (send-utils/tx-type-collectible?
                              (-> db :wallet :ui :send :tx-type))
         collectible         (when collectible-tx?
                               (-> db :wallet :ui :send :collectible))
         one-collectible?    (when collectible-tx?
                               (= (collectible.utils/collectible-balance collectible) 1))]
     {:db (-> db
              (assoc-in [:wallet :ui :send :recipient] (or recipient address))
              (assoc-in [:wallet :ui :send :to-address] to-address)
              (assoc-in [:wallet :ui :send :address-prefix] prefix)
              (assoc-in [:wallet :ui :send :receiver-preferred-networks] receiver-networks)
              (assoc-in [:wallet :ui :send :receiver-networks] receiver-networks))
      :fx [(when (and collectible-tx? one-collectible?)
             [:dispatch [:wallet/get-suggested-routes {:amount 1}]])
           [:dispatch
            [:wallet/wizard-navigate-forward
             {:current-screen stack-id
              :start-flow?    start-flow?
              :flow-id        :wallet-send-flow}]]]})))

(rf/reg-event-fx
 :wallet/update-receiver-networks
 (fn [{:keys [db]} [selected-networks]]
   (let [amount                           (get-in db [:wallet :ui :send :amount])
         disabled-from-chain-ids          (get-in db [:wallet :ui :send :disabled-from-chain-ids])
         filtered-disabled-from-chain-ids (filter (fn [chain-id]
                                                    (some #(= chain-id %)
                                                          selected-networks))
                                                  disabled-from-chain-ids)]
     {:db (-> db
              (assoc-in [:wallet :ui :send :receiver-networks] selected-networks)
              (assoc-in [:wallet :ui :send :disabled-from-chain-ids] filtered-disabled-from-chain-ids))
      :fx [[:dispatch [:wallet/get-suggested-routes {:amount amount}]]]})))

(rf/reg-event-fx
 :wallet/set-token-to-send
 (fn [{:keys [db]} [{:keys [token-symbol token stack-id start-flow?]}]]
   ;; `token` is a map extracted from the sender, but in the wallet home page we don't know the
   ;; sender yet, so we only provide the `token-symbol`, later in
   ;; `:wallet/select-from-account` the `token` key will be set.
   (let [{token-networks :networks}                token
         receiver-networks                         (get-in db [:wallet :ui :send :receiver-networks])
         token-networks-ids                        (mapv #(:chain-id %) token-networks)
         token-not-supported-in-receiver-networks? (not-any? (set receiver-networks)
                                                             token-networks-ids)]
     (when (or token token-symbol)
       {:db (cond-> db
              :always      (update-in [:wallet :ui :send] dissoc :collectible)
              :always      (assoc-in
                            [:wallet :ui :send :token-not-supported-in-receiver-networks?]
                            token-not-supported-in-receiver-networks?)
              token        (assoc-in [:wallet :ui :send :token] token)
              token        (assoc-in [:wallet :ui :send :token-display-name]
                            (:symbol token))
              token-symbol (assoc-in [:wallet :ui :send :token-symbol] token-symbol))
        :fx [[:dispatch [:wallet/clean-suggested-routes]]
             [:dispatch
              [:wallet/wizard-navigate-forward
               {:current-screen stack-id
                :start-flow?    start-flow?
                :flow-id        :wallet-send-flow}]]]}))))

(rf/reg-event-fx
 :wallet/edit-token-to-send
 (fn [{:keys [db]} [token]]
   (let [{token-networks :networks
          token-symbol   :symbol}                  token
         receiver-networks                         (get-in db [:wallet :ui :send :receiver-networks])
         token-networks-ids                        (mapv #(:chain-id %) token-networks)
         token-not-supported-in-receiver-networks? (not (some (set receiver-networks)
                                                              token-networks-ids))]
     {:db (-> db
              (assoc-in [:wallet :ui :send :token] token)
              (assoc-in [:wallet :ui :send :token-display-name] token-symbol)
              (assoc-in [:wallet :ui :send :token-not-supported-in-receiver-networks?]
                        token-not-supported-in-receiver-networks?))
      :fx [[:dispatch [:hide-bottom-sheet]]
           [:dispatch [:wallet/clean-suggested-routes]]
           [:dispatch [:wallet/clean-from-locked-amounts]]]})))

(rf/reg-event-fx :wallet/clean-selected-token
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :token :token-display-name :tx-type)}))

(rf/reg-event-fx :wallet/clean-selected-collectible
 (fn [{:keys [db]}]
   (let [transaction-type (get-in db [:wallet :ui :send :tx-type])]
     {:db (update-in db
                     [:wallet :ui :send]
                     dissoc
                     :collectible
                     :token-display-name
                     :amount
                     (when (send-utils/tx-type-collectible? transaction-type)
                       :tx-type))})))

(rf/reg-event-fx
 :wallet/set-collectible-to-send
 (fn [{db :db} [{:keys [collectible current-screen start-flow?]}]]
   (let [collection-data    (:collection-data collectible)
         collectible-data   (:collectible-data collectible)
         contract-type      (:contract-type collectible)
         tx-type            (if (= contract-type constants/wallet-contract-type-erc-1155)
                              :tx/collectible-erc-1155
                              :tx/collectible-erc-721)
         collectible-id     (get-in collectible [:id :token-id])
         one-collectible?   (= (collectible.utils/collectible-balance collectible) 1)
         token-display-name (cond
                              (and collectible
                                   (not (string/blank? (:name collectible-data))))
                              (:name collectible-data)

                              collectible
                              (str (:name collection-data) " #" collectible-id))
         collectible-tx     (-> db
                                (update-in [:wallet :ui :send] dissoc :token)
                                (assoc-in [:wallet :ui :send :collectible] collectible)
                                (assoc-in [:wallet :ui :send :token-display-name] token-display-name)
                                (assoc-in [:wallet :ui :send :tx-type] tx-type))
         recipient-set?     (-> db :wallet :ui :send :recipient)]
     {:db (cond-> collectible-tx
            one-collectible? (assoc-in [:wallet :ui :send :amount] 1))
      :fx [(when (and one-collectible? recipient-set?)
             [:dispatch [:wallet/get-suggested-routes {:amount 1}]])
           [:dispatch
            [:wallet/wizard-navigate-forward
             {:current-screen current-screen
              :start-flow?    start-flow?
              :flow-id        :wallet-send-flow}]]]})))

(rf/reg-event-fx
 :wallet/set-collectible-amount-to-send
 (fn [{db :db} [{:keys [stack-id amount]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)
    :fx [[:dispatch [:wallet/get-suggested-routes {:amount amount}]]
         [:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :flow-id        :wallet-send-flow}]]]}))

(rf/reg-event-fx
 :wallet/set-token-amount-to-send
 (fn [{:keys [db]} [{:keys [amount stack-id start-flow?]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :start-flow?    start-flow?
            :flow-id        :wallet-send-flow}]]]}))

(rf/reg-event-fx
 :wallet/set-token-amount-to-bridge
 (fn [{:keys [db]} [{:keys [amount stack-id start-flow?]}]]
   {:db (assoc-in db [:wallet :ui :send :amount] amount)
    :fx [[:dispatch
          [:wallet/wizard-navigate-forward
           {:current-screen stack-id
            :start-flow?    start-flow?
            :flow-id        :wallet-bridge-flow}]]]}))

(rf/reg-event-fx
 :wallet/clean-bridge-to-selection
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :bridge-to-chain-id)}))

(rf/reg-event-fx
 :wallet/clean-routes-calculation
 (fn [{:keys [db]}]
   (let [keys-to-remove [:to-values-by-chain :network-links :sender-network-values :route
                         :receiver-network-values :suggested-routes :from-values-by-chain
                         :loading-suggested-routes? :suggested-routes-call-timestamp]]
     {:db (update-in db [:wallet :ui :send] #(apply dissoc % keys-to-remove))})))

(rf/reg-event-fx :wallet/disable-from-networks
 (fn [{:keys [db]} [chain-ids]]
   {:db (assoc-in db [:wallet :ui :send :disabled-from-chain-ids] chain-ids)}))

(rf/reg-event-fx :wallet/lock-from-amount
 (fn [{:keys [db]} [chain-id amount]]
   {:db (assoc-in db [:wallet :ui :send :from-locked-amounts chain-id] amount)}))

(rf/reg-event-fx :wallet/unlock-from-amount
 (fn [{:keys [db]} [chain-id]]
   {:db (update-in db [:wallet :ui :send :from-locked-amounts] dissoc chain-id)}))

(rf/reg-event-fx :wallet/reset-network-amounts-to-zero
 (fn [{:keys [db]}]
   (let [sender-network-values   (get-in db [:wallet :ui :send :sender-network-values])
         receiver-network-values (get-in db [:wallet :ui :send :receiver-network-values])
         disabled-from-chain-ids (get-in db [:wallet :ui :send :disabled-from-chain-ids])
         sender-network-values   (send-utils/reset-network-amounts-to-zero
                                  {:network-amounts    sender-network-values
                                   :disabled-chain-ids disabled-from-chain-ids})
         receiver-network-values (send-utils/reset-network-amounts-to-zero
                                  {:network-amounts    receiver-network-values
                                   :disabled-chain-ids []})]
     {:db (-> db
              (assoc-in [:wallet :ui :send :sender-network-values] sender-network-values)
              (assoc-in [:wallet :ui :send :receiver-network-values] receiver-network-values)
              (update-in [:wallet :ui :send]
                         dissoc
                         :network-links
                         (when (empty? sender-network-values) :sender-network-values)
                         (when (empty? receiver-network-values) :receiver-network-values)))})))

(rf/reg-event-fx :wallet/get-suggested-routes
 (fn [{:keys [db now]} [{:keys [amount updated-token]}]]
   (let [wallet-address (get-in db [:wallet :current-viewing-account-address])
         token (or updated-token (get-in db [:wallet :ui :send :token]))
         transaction-type (get-in db [:wallet :ui :send :tx-type])
         collectible (get-in db [:wallet :ui :send :collectible])
         to-address (get-in db [:wallet :ui :send :to-address])
         receiver-networks (get-in db [:wallet :ui :send :receiver-networks])
         disabled-from-chain-ids (or (get-in db [:wallet :ui :send :disabled-from-chain-ids]) [])
         from-locked-amounts (or (get-in db [:wallet :ui :send :from-locked-amounts]) {})
         test-networks-enabled? (get-in db [:profile/profile :test-networks-enabled?])
         networks ((if test-networks-enabled? :test :prod)
                   (get-in db [:wallet :networks]))
         network-chain-ids (map :chain-id networks)
         bridge-to-chain-id (get-in db [:wallet :ui :send :bridge-to-chain-id])
         token-decimal (when token (:decimals token))
         token-id (utils/format-token-id token collectible)
         to-token-id ""
         network-preferences (if token [] [(get-in collectible [:id :contract-id :chain-id])])
         gas-rates constants/gas-rate-medium
         to-hex (fn [v] (send-utils/amount-in-hex v (if token token-decimal 0)))
         amount-in (to-hex amount)
         from-address wallet-address
         disabled-from-chain-ids disabled-from-chain-ids
         disabled-to-chain-ids (if (= transaction-type :tx/bridge)
                                 (filter #(not= % bridge-to-chain-id) network-chain-ids)
                                 (filter (fn [chain-id]
                                           (not (some #(= chain-id %)
                                                      receiver-networks)))
                                         network-chain-ids))
         from-locked-amount (update-vals from-locked-amounts to-hex)
         transaction-type-param (case transaction-type
                                  :tx/collectible-erc-721  constants/send-type-erc-721-transfer
                                  :tx/collectible-erc-1155 constants/send-type-erc-1155-transfer
                                  :tx/bridge               constants/send-type-bridge
                                  constants/send-type-transfer)
         balances-per-chain (when token (:balances-per-chain token))
         sender-token-available-networks-for-suggested-routes
         (when token
           (send-utils/token-available-networks-for-suggested-routes {:balances-per-chain
                                                                      balances-per-chain
                                                                      :disabled-chain-ids
                                                                      disabled-from-chain-ids
                                                                      :only-with-balance? true}))
         receiver-token-available-networks-for-suggested-routes
         (when token
           (send-utils/token-available-networks-for-suggested-routes {:balances-per-chain
                                                                      balances-per-chain
                                                                      :disabled-chain-ids
                                                                      disabled-from-chain-ids
                                                                      :only-with-balance? false}))
         token-networks-ids (when token (mapv #(:chain-id %) (:networks token)))
         sender-network-values (when sender-token-available-networks-for-suggested-routes
                                 (send-utils/loading-network-amounts
                                  {:valid-networks
                                   (if (= transaction-type :tx/bridge)
                                     (remove #(= bridge-to-chain-id %)
                                             sender-token-available-networks-for-suggested-routes)
                                     sender-token-available-networks-for-suggested-routes)
                                   :disabled-chain-ids disabled-from-chain-ids
                                   :receiver-networks receiver-networks
                                   :token-networks-ids token-networks-ids
                                   :tx-type transaction-type
                                   :receiver? false}))
         receiver-network-values (when receiver-token-available-networks-for-suggested-routes
                                   (send-utils/loading-network-amounts
                                    {:valid-networks
                                     (if (= transaction-type :tx/bridge)
                                       (filter
                                        #(= bridge-to-chain-id %)
                                        receiver-token-available-networks-for-suggested-routes)
                                       receiver-token-available-networks-for-suggested-routes)
                                     :disabled-chain-ids disabled-from-chain-ids
                                     :receiver-networks receiver-networks
                                     :token-networks-ids token-networks-ids
                                     :tx-type transaction-type
                                     :receiver? true}))
         request-params [transaction-type-param
                         from-address
                         to-address
                         amount-in
                         token-id
                         to-token-id
                         disabled-from-chain-ids
                         disabled-to-chain-ids
                         network-preferences
                         gas-rates
                         from-locked-amount]]
     {:db            (cond-> db
                       :always (assoc-in [:wallet :ui :send :amount] amount)
                       :always (assoc-in [:wallet :ui :send :loading-suggested-routes?] true)
                       :always (assoc-in [:wallet :ui :send :sender-network-values]
                                sender-network-values)
                       :always (assoc-in [:wallet :ui :send :receiver-network-values]
                                receiver-network-values)
                       :always (assoc-in [:wallet :ui :send :suggested-routes-call-timestamp] now)
                       :always (update-in [:wallet :ui :send] dissoc :network-links)
                       token   (assoc-in [:wallet :ui :send :token] token))
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
              (assoc-in [:wallet :ui :send :just-completed-transaction?] true)
              (assoc-in [:wallet :transactions] transaction-details)
              (assoc-in [:wallet :ui :send :transaction-ids] transaction-ids))
      :fx [[:dispatch
            [:wallet/end-transaction-flow]]
           [:dispatch-later
            [{:ms       2000
              :dispatch [:wallet/clean-just-completed-transaction]}]]]})))

(rf/reg-event-fx :wallet/clean-just-completed-transaction
 (fn [{:keys [db]}]
   {:db (update-in db [:wallet :ui :send] dissoc :just-completed-transaction?)}))

(rf/reg-event-fx :wallet/clean-up-transaction-flow
 (fn [_]
   {:fx [[:dispatch [:dismiss-modal :screen/wallet.transaction-confirmation]]
         [:dispatch [:wallet/clean-scanned-address]]
         [:dispatch [:wallet/clean-local-suggestions]]
         [:dispatch [:wallet/clean-send-address]]
         [:dispatch [:wallet/clean-disabled-from-networks]]
         [:dispatch [:wallet/select-address-tab nil]]]}))

(rf/reg-event-fx :wallet/end-transaction-flow
 (fn [{:keys [db]}]
   (let [address (get-in db [:wallet :current-viewing-account-address])]
     {:fx [[:dispatch [:wallet/navigate-to-account-within-stack address]]
           [:dispatch [:wallet/fetch-activities-for-current-account]]
           [:dispatch [:wallet/select-account-tab :activity]]
           [:dispatch-later
            [{:ms       20
              :dispatch [:wallet/clean-up-transaction-flow]}]]]})))

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

      (= bridge-name constants/bridge-name-erc-1155-transfer)
      (assoc :ERC1155TransferTx
             (assoc tx-data
                    :Recipient to-address
                    :TokenID   token-id
                    :ChainID   to-chain-id
                    :Amount    amount-in))

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
                                  :tx/collectible-erc-721  constants/send-type-erc-721-transfer
                                  :tx/collectible-erc-1155 constants/send-type-erc-1155-transfer
                                  :tx/bridge               constants/send-type-bridge
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
                                     (rf/dispatch [:wallet/add-authorized-transaction result])
                                     (rf/dispatch [:hide-bottom-sheet]))
                       :on-error   (fn [error]
                                     (log/error "failed to send transaction"
                                                {:event  :wallet/send-transaction
                                                 :error  error
                                                 :params request-params})
                                     (rf/dispatch [:toasts/upsert
                                                   {:id   :send-transaction-error
                                                    :type :negative
                                                    :text (:message error)}]))}]})))

(rf/reg-event-fx
 :wallet/select-from-account
 (fn [{db :db} [{:keys [address stack-id network-details start-flow?]}]]
   (let [{:keys [token-symbol
                 tx-type]} (-> db :wallet :ui :send)
         token             (when token-symbol
                             ;; When this flow has started in the wallet home page, we know the
                             ;; token or collectible to send, but we don't know from which
                             ;; account, so we extract the token data from the picked account.
                             (let [token (utils/get-token-from-account db token-symbol address)]
                               (assoc token
                                      :networks      (network-utils/network-list token network-details)
                                      :total-balance (utils/calculate-total-token-balance token))))
         bridge-tx?        (= tx-type :tx/bridge)
         flow-id           (if bridge-tx?
                             :wallet-bridge-flow
                             :wallet-send-flow)]
     {:db (cond-> db
            token-symbol (assoc-in [:wallet :ui :send :token] token)
            bridge-tx?   (assoc-in [:wallet :ui :send :to-address] address))
      :fx [[:dispatch [:wallet/switch-current-viewing-account address]]
           [:dispatch
            [:wallet/wizard-navigate-forward
             {:current-screen stack-id
              :start-flow?    start-flow?
              :flow-id        flow-id}]]]})))
